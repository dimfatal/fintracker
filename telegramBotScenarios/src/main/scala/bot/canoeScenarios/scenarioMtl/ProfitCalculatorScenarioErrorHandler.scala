package bot.canoeScenarios.scenarioMtl

import canoe.api.{ chatApi, Scenario, TelegramClient }
import canoe.models.Chat
import canoe.syntax._
import cats.ApplicativeError
import tcs4sclient.model.domain.user.AccountType

sealed trait ProfitCalculatorError                               extends Exception
case class TelegramParameterExist(param: String)                 extends ProfitCalculatorError
case class TelegramParameterCorrect(param: String)               extends ProfitCalculatorError
case class TelegramParameterUsefulForMarketTicker(param: String) extends ProfitCalculatorError
case class AccountNotFountError(account: AccountType)            extends ProfitCalculatorError

class ProfitCalculatorScenarioErrorHandler[F[_]: TelegramClient: ApplicativeError[*[_], ProfitCalculatorError]]
    extends ScenariosErrorHandler[F, ProfitCalculatorError] {

  private def handler(chat: Chat): Throwable => Scenario[F, Unit] = {
    case TelegramParameterExist(param)                 =>
      Scenario.eval(chat.send(s"$param This command need parameter to be exist like - /p ticker_name")) >> Scenario.done
    case TelegramParameterCorrect(param)               => Scenario.eval(chat.send(s" $param text should not have empty spaces")) >> Scenario.done
    case TelegramParameterUsefulForMarketTicker(param) => Scenario.eval(chat.send(s" $param - unknown ticker")) >> Scenario.done
    case AccountNotFountError(account)                 => Scenario.eval(chat.send(s" $account - can't found this type account")) >> Scenario.done
  }

  override def handle(scenario: Scenario[F, Unit], chat: Chat): Scenario[F, Unit] =
    ScenarioErrorHandler(scenario)(handler(chat))
}
