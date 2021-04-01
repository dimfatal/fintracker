package bot.canoeScenarios

import bot.inMemoryStorage.{InMemoryAccountsStorage, TinkoffTokenStorage}
import bot.tinkoff.AccountsMap
import bot.tinkoff.TinkoffInvestPrograms.TinkoffService
import canoe.syntax.{command, textMessage}
import canoe.api.{Scenario, TelegramClient, chatApi, messageApi}
import canoe.syntax._
import cats.effect.Sync
import cats.effect.concurrent.Semaphore
import org.http4s.client.Client
import cats.implicits._

object Start {

  def startUpScenario[F[_]: Sync: TelegramClient: Client](semaphore: Semaphore[F], tokenStore: TinkoffTokenStorage[F])(implicit
    accountStore: InMemoryAccountsStorage[F]
  ): Scenario[F, Unit] = {

    def accountsMap(token: String) = new TinkoffService[AccountsMap].run(token)

    for {
      chat         <- Scenario.expect(command("s").chat)
      _            <- Scenario.eval(chat.send("enter tinkoff api token (after you send the message it will be deleted)"))
      tokenMessage <- Scenario.expect(textMessage)
      _            <- Scenario.eval(tokenStore.save(tokenMessage.text))
      _            <- Scenario.eval(accountsMap(tokenMessage.text).attempt.map(
        _.fold(
          _ => Scenario.eval(chat.send("problem while trying to authorize, make sure the token you provided is valid ")) >> Scenario.done,// todo logging this error
          acc => Scenario.eval(accountStore.save(acc.a)) >> Scenario.eval(tokenMessage.delete) >> Scenario.eval(semaphore.release)
        )
      )
        .compile
        .toList.map(_.head)).flatten


    } yield ()
  }
}

object Help {
  def run[F[_]: Sync: TelegramClient](): Scenario[F, Unit] =
    for {
      chat <- Scenario.expect(command("h").chat)
      _    <- Scenario.eval(chat.send(helpText))
    } yield ()

  val helpText: String =
    """
      |/h Shows help menu
      |/a Shows available accounts
      |/p [ticker] Check current profit of stock
      |/l List current stock positions in your profile
      |/i [ticker] List prices of stock in from your portfolio
      |/t [ticker] Search instruments by ticker
      |""".stripMargin
}
