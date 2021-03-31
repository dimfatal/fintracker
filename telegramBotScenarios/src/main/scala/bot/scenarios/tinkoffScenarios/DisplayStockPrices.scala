package bot.scenarios
package tinkoffScenarios

import bot.memoryStorage.AccountTypeStorageSyntax.AccountTypeIdOps
import bot.scenarios.ScenariosLogicInterpreter.checkParam
import bot.memoryStorage.{ InMemoryAccountsStorage, TinkoffTokenStorage }
import bot.scenarios.tinkoffProgramsService.ScenarioService.{ TinkoffService, TinkoffServiceLogic }
import bot.scenarios.tinkoffProgramsService.StockPrices
import bot.scenarios.tinkoffScenarios.validation._
import canoe.api.{ chatApi, Scenario, TelegramClient }
import canoe.models.Chat
import canoe.syntax._
import cats.effect.Sync
import cats.effect.concurrent.Semaphore
import cats.implicits._
import fs2._
import org.http4s.client.Client
import tcs4sclient.model.domain.market.Ticker
import tcs4sclient.model.domain.user.{ AccountType, Tinkoff }

object DisplayStockPrices {

  private implicit val tickerValidator: TickerValidator[Either[TickerValidation, *]] =
    TickerValidatorInterpreter.tickerValidator[Either[TickerValidation, *], TickerValidation](identity)

  def run[F[_]: Sync: TelegramClient: InMemoryAccountsStorage: Client](
    semaphore: Semaphore[F],
    tokenStore: TinkoffTokenStorage[F],
    account: AccountType = Tinkoff
  ): Scenario[F, Unit] = {

    def serviceFromTicker: Ticker => F[TinkoffServiceLogic[StockPrices]] =
      ticker => account.id.map(TinkoffServiceLogic.stockPrices(_, ticker))

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
    service: Ticker => F[TinkoffServiceLogic[StockPrices]]
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
    service: Ticker => F[TinkoffServiceLogic[StockPrices]]
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
    service: F[TinkoffServiceLogic[StockPrices]]
  ) = Stream.eval(service).map(implicit s => new TinkoffService[StockPrices].run(token).map(_.a)).flatten

}
