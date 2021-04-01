package bot.canoeScenarios

import bot.inMemoryStorage.AccountTypeStorageSyntax.AccountTypeIdOps
import bot.inMemoryStorage.{InMemoryAccountsStorage, TinkoffTokenStorage}
import bot.tinkoff.StockProfitMap
import ScenariosLogicInterpreter.checkParam
import bot.canoeScenarios.validation.{TickerValidation, TickerValidator, TickerValidatorInterpreter}
import bot.tinkoff.TinkoffInvestPrograms.{TinkoffInvestLogic, TinkoffService}
import canoe.api._
import canoe.models.Chat
import canoe.syntax._
import cats.effect.concurrent.Semaphore
import cats.effect.{ConcurrentEffect, Sync}
import org.http4s.client.Client
import tcs4sclient.model.domain.market.{MarketInstrument, Ticker}
import tcs4sclient.model.domain.user.{AccountType, Tinkoff}
import tcsInterpreters.portfolioInfo.DateTimePeriod.allTime
import tcsInterpreters.portfolioInfo.PeriodQuery
import cats.implicits._

object CheckStockProfitFromPeriod {

  private implicit val tickerValidator: TickerValidator[Either[TickerValidation, *]] =
    TickerValidatorInterpreter.tickerValidator[Either[TickerValidation, *], TickerValidation](identity)

  def run[F[_]: ConcurrentEffect: TelegramClient: InMemoryAccountsStorage: Client](
    semaphore: Semaphore[F],
    tokenStore: TinkoffTokenStorage[F],
    periodQuery: PeriodQuery = allTime,
    account: AccountType = Tinkoff
  ): Scenario[F, Unit] = {

    def serviceFromTicker: Ticker => F[TinkoffInvestLogic[StockProfitMap]] =
      ticker => account.id.map(TinkoffInvestLogic.stockProfit(_, ticker, periodQuery))

    Scenario.eval(semaphore.available).flatMap { i =>
      if (i > 0) {
        Scenario.expect(command("p").andThen(x => (x.chat, x.text.split(" ").tail))).flatMap { case (chat, param) =>
          for {
            token <- Scenario.eval(tokenStore.get)
            _     <- Scenario.eval(calculate(chat, param, token)(serviceFromTicker))
          } yield ()
        }
      } else Scenario.expect(command("p").chat).flatMap(chat => Scenario.eval(chat.send("run /s - command"))) >> Scenario.done
    }
  }

  private def profitCalculator[F[_]: Sync: Client: InMemoryAccountsStorage](
    token: String,
    service: F[TinkoffInvestLogic[StockProfitMap]]
  ): fs2.Stream[F, Map[MarketInstrument, String]] =
    fs2
      .Stream
      .eval(service)
      .map(implicit s => new TinkoffService[StockProfitMap].run(token).map(_.a))
      .flatten

  private def runService[F[_]: Sync: TelegramClient: Client: InMemoryAccountsStorage](chat: Chat, ticker: Ticker, token: String)(
    service: Ticker => F[TinkoffInvestLogic[StockProfitMap]]
  ): F[Unit] =
    TickerValidator
      .validate(ticker, token)
      .value
      .map(
        _.fold(
          e => chat.send(s"invalid ticker - ${e.errorMessages}").map(_ => ()),
          validTicker =>
            profitCalculator(token, service(validTicker))
              .evalMap(askWhichInstrumentShouldBeTaken(_, chat))
              .evalMap(m => chat.send(m))
              .compile
              .drain
        )
      )
      .flatten

  def askWhichInstrumentShouldBeTaken[F[_]: Sync: TelegramClient](instruments: Map[MarketInstrument, String], chat: Chat): F[String] =
    if (instruments.keys.size > 1) {
      chat
        .send(s"send index of instrument you want to check - ${instruments.keys.map(_.name).zipWithIndex.map { case (element, index) =>
          s" $index -> $element"
        }}")
        .map(message =>
          message
            .text
            .toIntOption
            .flatMap(index => instruments.keys.toList.get(index))
            .map(instruments)
            .getOrElse("error profit")
        )
    } else Sync[F].delay(instruments.last._2)

  private def calculate[F[_]: Sync: TelegramClient: Client: InMemoryAccountsStorage](chat: Chat, userInput: Array[String], token: String)(
    service: Ticker => F[TinkoffInvestLogic[StockProfitMap]]
  ): F[Unit] =
    checkParam(chat, userInput)
      .map(
        _.fold(
          e => chat.send(s"invalid parameter ${e.errorMessage}").map(_ => ()),
          cmd => runService(chat, Ticker(cmd.args.last), token)(service)
        )
      )
      .flatten

}
