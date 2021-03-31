package bot.scenarios
package tinkoffScenarios

import bot.memoryStorage.AccountTypeStorageSyntax.AccountTypeIdOps
import bot.scenarios.ScenariosLogicInterpreter.checkParam
import bot.memoryStorage.{ InMemoryAccountsStorage, TinkoffTokenStorage }
import bot.scenarios.tinkoffProgramsService.ScenarioService.{ TinkoffService, TinkoffServiceLogic }
import bot.scenarios.tinkoffProgramsService.StockProfitMap
import bot.scenarios.tinkoffScenarios.validation._
import canoe.api._
import canoe.models.Chat
import canoe.syntax._
import cats.effect.concurrent.Semaphore
import cats.effect.{ ConcurrentEffect, Sync }
import cats.implicits._
import org.http4s.client.Client
import tcs4sclient.model.domain.market.{ MarketInstrument, Ticker }
import tcs4sclient.model.domain.user.{ AccountType, Tinkoff }
import tcsInterpreters.portfolioInfo.DateTimePeriod._
import tcsInterpreters.portfolioInfo.PeriodQuery

object CheckStockProfitFromPeriod {

  private implicit val tickerValidator: TickerValidator[Either[TickerValidation, *]] =
    TickerValidatorInterpreter.tickerValidator[Either[TickerValidation, *], TickerValidation](identity)

  def run[F[_]: ConcurrentEffect: TelegramClient: InMemoryAccountsStorage: Client](
    semaphore: Semaphore[F],
    tokenStore: TinkoffTokenStorage[F],
    periodQuery: PeriodQuery = allTime,
    account: AccountType = Tinkoff
  ): Scenario[F, Unit] = {

    def serviceFromTicker: Ticker => F[TinkoffServiceLogic[StockProfitMap]] =
      ticker => account.id.map(TinkoffServiceLogic.stockProfit(_, ticker, periodQuery))

    Scenario.eval(semaphore.available).flatMap { i =>
      if (i > 0) {
        Scenario.expect(command("h").andThen(x => (x.chat, x.text.split(" ").tail))).flatMap { case (chat, param) =>
          for {
            token <- Scenario.eval(tokenStore.get)
            _     <- Scenario.eval(calculate(chat, param, token)(serviceFromTicker))
          } yield ()
        }
      } else Scenario.expect(command("h").chat).flatMap(chat => Scenario.eval(chat.send("run /s - command"))) >> Scenario.done
    }
  }

  private def profitCalculator[F[_]: Sync: Client: InMemoryAccountsStorage](
    token: String,
    service: F[TinkoffServiceLogic[StockProfitMap]]
  ): fs2.Stream[F, Map[MarketInstrument, String]] =
    fs2
      .Stream
      .eval(service)
      .map(implicit s => new TinkoffService[StockProfitMap].run(token).map(_.a))
      .flatten

  private def runService[F[_]: Sync: TelegramClient: Client: InMemoryAccountsStorage](chat: Chat, ticker: Ticker, token: String)(
    service: Ticker => F[TinkoffServiceLogic[StockProfitMap]]
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
    service: Ticker => F[TinkoffServiceLogic[StockProfitMap]]
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
