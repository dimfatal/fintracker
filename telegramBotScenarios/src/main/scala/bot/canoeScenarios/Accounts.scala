package bot.canoeScenarios

import bot.inMemoryStorage.InMemoryAccountsStorage
import canoe.syntax.command
import canoe.api.{ chatApi, Scenario, TelegramClient }
import canoe.syntax._
import cats.Functor
import cats.effect.concurrent.Semaphore
import cats.implicits._

object Accounts {
  def sendAvailableAccounts[F[_]: Functor: TelegramClient](semaphore: Semaphore[F], accStore: InMemoryAccountsStorage[F]): Scenario[F, Unit] =
    Scenario.eval(semaphore.available).flatMap { i =>
      if (i > 0) {
        for {
          chat              <- Scenario.expect(command("a").chat)
          accountsMapByType <- Scenario.eval(accStore.getAllAccounts.map(_.keys.mkString("\n")))
          _                 <- Scenario.eval(chat.send(accountsMapByType))
        } yield ()
      } else Scenario.expect(command("a").chat).flatMap(chat => Scenario.eval(chat.send("run /s - command"))) >> Scenario.done
    }
}
