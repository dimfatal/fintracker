package bot.scenarios
package tinkoffScenarios

import bot.memoryStorage.TinkoffTokenStorage
import bot.scenarios.tinkoffProgramsService.ScenarioService.TinkoffService
import bot.scenarios.tinkoffProgramsService.ScenarioService.TinkoffServiceLogic._
import bot.scenarios.tinkoffProgramsService.{ PositionsList, ScenarioService }
import canoe.api.{ chatApi, Scenario, TelegramClient }
import canoe.syntax._
import cats.effect.Sync
import cats.effect.concurrent.Semaphore
import cats.implicits._
import org.http4s.client.Client
import tcs4sclient.model.domain.user.{ AccountType, Tinkoff }
import tcsInterpreters.InMemoryAccountsStorage

object DisplayPositions {

  def displayPositions[F[_]: Sync: TelegramClient: InMemoryAccountsStorage: Client](
    semaphore: Semaphore[F],
    tokenStore: TinkoffTokenStorage[F],
    account: AccountType = Tinkoff
  ): Scenario[F, Unit] = {

    implicit val serviceLogic: ScenarioService.TinkoffServiceLogic[PositionsList] = positionsList(account)

    def positions = (token: String) => new TinkoffService[PositionsList].run(token).map(_.a)

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
