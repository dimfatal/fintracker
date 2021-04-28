package bot.canoeScenarios

import bot.inMemoryStorage.AccountTypeStorageSyntax._
import bot.inMemoryStorage._
import bot.tinkoff.TinkoffInvestService
import canoe.api.{ chatApi, Scenario, TelegramClient }
import canoe.syntax._
import cats.effect.Sync
import cats.effect.concurrent.Semaphore
import cats.implicits._
import fs2.Stream
import org.http4s.client.Client
import tcs4sclient.api.client.{ Http4sTinkoffClientBuilder, TinkoffClient }
import tcs4sclient.model.domain.user.{ AccountType, Tinkoff }
import tcsInterpreters.PositionsInfo

object DisplayPositions {

  def displayPositions[F[_]: Sync: TelegramClient: InMemoryAccountsStorage: Client](
    semaphore: Semaphore[F],
    tokenStore: TinkoffTokenStorage[F],
    account: AccountType = Tinkoff
  ): Scenario[F, Unit] = {

    def tinkoffClient(token: String): TinkoffClient[F]      = Http4sTinkoffClientBuilder.fromHttp4sClient(token)(implicitly[Client[F]])
    def positions(implicit tinkoffClient: TinkoffClient[F]) =
      account
        .findId
        .map(id => new TinkoffInvestService[PositionsInfo].make.display(id))

    Scenario.eval(semaphore.available).flatMap { i =>
      if (i > 0) {
        for {
          chat      <- Scenario.expect(command("l").chat)
          positions <- Scenario.eval(
                         Stream //todo refactor needed
                           .eval(tokenStore.get)
                           .map(tinkoffClient)
                           .map { d =>
                             positions(d).value.map(_.sequence).flatten
                           }
                           .compile
                           .toList
                           .map(_.sequence)
                           .flatten
                           .map(_.sequence.map(_.flatten))
                       )
          _         <- Scenario.eval(chat.send(positions.fold("")(_.mkString("\n"))))
        } yield ()
      } else Scenario.expect(command("l").chat).flatMap(chat => Scenario.eval(chat.send("run /s - command"))) >> Scenario.done
    }

  }
}
