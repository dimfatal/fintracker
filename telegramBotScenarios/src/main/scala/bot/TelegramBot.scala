package bot

import bot.memoryStorage.TokenStorage
import bot.scenarios.tinkoffScenarios.{ Accounts, CheckStockProfitFromPeriod, DisplayPositions, DisplayStockPrices, Start }
import canoe.api.{ Bot, TelegramClient }
import cats.effect.concurrent.Semaphore
import cats.effect.{ ExitCode, IO, IOApp }
import fs2.Stream
import org.http4s.client.blaze.BlazeClientBuilder
import tcsInterpreters.InMemoryAccountsStorage

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

object TelegramBot extends IOApp {
  private def telegramBotToken: String = System.getenv("BOT_TOKEN") //todo get it from env param or config file

  override def run(args: List[String]): IO[ExitCode] = {

    val ec1                   = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(4))
    val canoeClient           = Stream.resource(TelegramClient.apply[IO](telegramBotToken, ec1))
    val http4sClientStreaming = BlazeClientBuilder[IO](ec1).stream

    val botProgram =
      canoeClient.map { implicit telegramClient =>
        http4sClientStreaming.map { implicit http4sClient =>
          Stream.eval(Semaphore[IO](0)).flatMap { sem =>
            Stream.eval(TokenStorage.make[IO]).flatMap { tokenStore =>
              Stream.eval(InMemoryAccountsStorage.make[IO]).flatMap { implicit accountStore =>
                Bot
                  .polling[IO]
                  .follow(
                    Start.startUpScenario(sem, tokenStore),
                    Accounts.sendAvailableAccounts(sem, accountStore),
                    CheckStockProfitFromPeriod.run(sem, tokenStore),
                    DisplayPositions.displayPositions(sem, tokenStore),
                    DisplayStockPrices.run(sem, tokenStore)
                  )
              }
            }
          }
        }
      }

    botProgram
      .flatten
      .flatten
      .compile
      .drain
      .as(ExitCode.Success)
  }

}
