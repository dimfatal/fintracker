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
          er => Scenario.eval(chat.send("problem while trying to authorize, make sure the token you provided is valid ")) >> Scenario.done,// todo logging this error
          acc => Scenario.eval(accountStore.save(acc.a)) >> Scenario.eval(tokenMessage.delete) >> Scenario.eval(semaphore.release)
        )
      )
        .compile
        .toList.map(_.head)).flatten


    } yield ()
  }
}
