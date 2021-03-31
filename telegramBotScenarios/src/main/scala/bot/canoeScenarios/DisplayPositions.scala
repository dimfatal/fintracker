package bot.canoeScenarios

import bot.inMemoryStorage.AccountTypeStorageSyntax.AccountTypeIdOps
import bot.inMemoryStorage.{InMemoryAccountsStorage, TinkoffTokenStorage}
import bot.tinkoff.{PositionsList, TinkoffInvestPrograms}
import bot.tinkoff.TinkoffInvestPrograms.TinkoffService
import canoe.api.{Scenario, TelegramClient, chatApi}
import canoe.syntax._
import cats.effect.Sync
import cats.effect.concurrent.Semaphore
import cats.implicits._
import fs2.Stream
import org.http4s.client.Client
import tcs4sclient.model.domain.user.{AccountType, Tinkoff}

object DisplayPositions {

  def displayPositions[F[_]: Sync: TelegramClient: InMemoryAccountsStorage: Client](
    semaphore: Semaphore[F],
    tokenStore: TinkoffTokenStorage[F],
    account: AccountType = Tinkoff
  ): Scenario[F, Unit] = {

    def positions = (token: String) =>
      Stream
        .eval(account.id)
        .map(TinkoffInvestPrograms.TinkoffInvestLogic.positionsList)
        .map(implicit logic => new TinkoffService[PositionsList].run(token).map(_.a))
        .flatten

    Scenario.eval(semaphore.available).flatMap { i =>
      if (i > 0) {
        for {
          chat      <- Scenario.expect(command("p").chat)
          token     <- Scenario.eval(tokenStore.get)
          positions <- Scenario.eval(
                         positions(token)
                           .compile
                           .foldMonoid
                       )
          _         <- Scenario.eval(chat.send(positions.mkString("\n")))
        } yield ()
      } else Scenario.expect(command("i").chat).flatMap(chat => Scenario.eval(chat.send("run /s - command"))) >> Scenario.done
    }

  }
}
