package bot.canoeScenarios

import canoe.api.Scenario
import tcs4sclient.model.domain.user.AccountType

trait ScenarioLogic[F[_]] {
  def start: Scenario[F, Unit]
  def accounts: Scenario[F, Unit]
  def stockProfit: Scenario[F, Unit]
  def displayPositions(account: AccountType): Scenario[F, Unit]
}

object ScenariosLogicInterpreter {
  //todo
}
