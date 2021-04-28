package bot.canoeScenarios.scenarioMtl

import canoe.api.Scenario
import canoe.models.Chat
import cats.ApplicativeError

trait ScenariosErrorHandler[F[_], E <: Throwable] {
  def handle(scenario: Scenario[F, Unit], chat: Chat): Scenario[F, Unit]
}

object ScenarioErrorHandler {
  type Scenarios[F[_]] = Scenario[F, Unit]
  def apply[F[_]: ApplicativeError[*[_], E], E <: Throwable](scenario: Scenarios[F])(handler: Throwable => Scenarios[F]): Scenarios[F] =
    scenario.handleErrorWith(e => handler(e))
}

object ScenariosErrorHandler {
  def apply[F[_], E <: Throwable](implicit ev: ScenariosErrorHandler[F, E]): ScenariosErrorHandler[F, E] = ev
}