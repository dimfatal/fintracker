package bot.canoeScenarios

import bot.inMemoryStorage.AccountTypeStorageSyntax.AccountTypeIdOps
import bot.inMemoryStorage.{ InMemoryAccountsStorage, TinkoffTokenStorage }
import bot.tinkoff.{ PositionsList, TinkoffInvestPrograms }
import bot.tinkoff.TinkoffInvestPrograms.TinkoffService
import canoe.api.{ chatApi, Scenario, TelegramClient }
import canoe.syntax._
import cats.effect.Sync
import cats.effect.concurrent.Semaphore
import cats.implicits._
import fs2.Stream
import org.http4s.client.Client
import tcs4sclient.api.client.{ Http4sTinkoffClientBuilder, TinkoffClient }
import tcs4sclient.model.domain.user.{ AccountType, Tinkoff }

object DisplayPositions {

  def displayPositions[F[_]: Sync: TelegramClient: InMemoryAccountsStorage: Client](
    semaphore: Semaphore[F],
    tokenStore: TinkoffTokenStorage[F],
    account: AccountType = Tinkoff
  ): Scenario[F, Unit] = {

    def tinkoffClient(token: String): TinkoffClient[F]      = Http4sTinkoffClientBuilder.fromHttp4sClient(token)(implicitly[Client[F]])
    def positions(implicit tinkoffClient: TinkoffClient[F]) =
      Stream
        .eval(account.id)
        .map(TinkoffInvestPrograms.TinkoffInvestLogic.positionsList)
        .map(implicit logic => new TinkoffService[PositionsList].run.map(_.a))
        .flatten

    Scenario.eval(semaphore.available).flatMap { i =>
      if (i > 0) {
        for {
          chat      <- Scenario.expect(command("l").chat)
          positions <- Scenario.eval(
                         Stream
                           .eval(tokenStore.get)
                           .map(tinkoffClient)
                           .map(implicit c => positions)
                           .flatten
                           .compile
                           .foldMonoid
                       )
          _         <- Scenario.eval(chat.send(positions.mkString("\n")))
        } yield ()
      } else Scenario.expect(command("l").chat).flatMap(chat => Scenario.eval(chat.send("run /s - command"))) >> Scenario.done
    }

  }
}
