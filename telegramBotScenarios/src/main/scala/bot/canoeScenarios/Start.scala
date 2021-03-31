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
      saved         = accountsMap(tokenMessage.text).evalMap(accounts => accountStore.save(accounts.a)).compile.drain
      _            <- Scenario.eval(saved) >> Scenario.eval(tokenMessage.delete) >> Scenario.eval(semaphore.release)
    } yield ()
  }
}
