package bot.canoeScenarios

import bot.inMemoryStorage.AccountTypeStorageSyntax.AccountTypeIdOps
import bot.inMemoryStorage.{InMemoryAccountsStorage, TinkoffTokenStorage}
import bot.tinkoff.StockPrices
import ScenariosLogicInterpreter.checkParam
import bot.canoeScenarios.validation.{TickerValidation, TickerValidator, TickerValidatorInterpreter}
import bot.tinkoff.TinkoffInvestPrograms.{TinkoffInvestLogic, TinkoffService}
import canoe.api._
import canoe.models.Chat
import canoe.syntax._
import cats.effect.Sync
import cats.effect.concurrent.Semaphore
import fs2.Stream
import org.http4s.client.Client
import tcs4sclient.model.domain.market.Ticker
import tcs4sclient.model.domain.user.{AccountType, Tinkoff}
import cats.implicits._

object DisplayStockPrices {

  private implicit val tickerValidator: TickerValidator[Either[TickerValidation, *]] =
    TickerValidatorInterpreter.tickerValidator[Either[TickerValidation, *], TickerValidation](identity)

  def run[F[_]: Sync: TelegramClient: InMemoryAccountsStorage: Client](
    semaphore: Semaphore[F],
    tokenStore: TinkoffTokenStorage[F],
    account: AccountType = Tinkoff
  ): Scenario[F, Unit] = {

    def serviceFromTicker: Ticker => F[TinkoffInvestLogic[StockPrices]] =
      ticker => account.id.map(TinkoffInvestLogic.stockPrices(_, ticker))

    Scenario.eval(semaphore.available).flatMap { i =>
      if (i > 0) {
        Scenario.expect(command("i").andThen(x => (x.chat, x.text.split(" ").tail))).flatMap { case (chat, param) =>
          for {
            token <- Scenario.eval(tokenStore.get)
            _     <- Scenario.eval(calculate(chat, param, token)(serviceFromTicker))
          } yield ()
        }
      } else Scenario.expect(command("i").chat).flatMap(chat => Scenario.eval(chat.send("run /s - command"))) >> Scenario.done
    }
  }

  private def calculate[F[_]: Sync: TelegramClient: Client: InMemoryAccountsStorage](chat: Chat, userInput: Array[String], token: String)(
    service: Ticker => F[TinkoffInvestLogic[StockPrices]]
  ): F[Unit] =
    checkParam(chat, userInput)
      .map(
        _.fold(
          e => chat.send(s"invalid parameter ${e.errorMessage}").map(_ => ()),
          cmd => runService(chat, Ticker(cmd.args.last), token)(service)
        )
      )
      .flatten

  private def runService[F[_]: Sync: TelegramClient: Client: InMemoryAccountsStorage](chat: Chat, ticker: Ticker, token: String)(
    service: Ticker => F[TinkoffInvestLogic[StockPrices]]
  ): F[Unit] =
    TickerValidator
      .validate(ticker, token)
      .value
      .map(
        _.fold(
          e => chat.send(s"invalid ticker - ${e.errorMessages}").map(_ => ()),
          validTicker =>
            stockPrices(token, service(validTicker))
              .evalMap(m => chat.send(m))
              .compile
              .drain
        )
      )
      .flatten

  private def stockPrices[F[_]: Sync: Client: InMemoryAccountsStorage](
    token: String,
    service: F[TinkoffInvestLogic[StockPrices]]
  ) = Stream.eval(service).map(implicit s => new TinkoffService[StockPrices].run(token).map(_.a)).flatten

}
