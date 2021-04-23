package bot.canoeScenarios.scenarioMtl

import canoe.api.{Scenario, TelegramClient, chatApi}
import canoe.models.Chat
import canoe.syntax._
import cats.ApplicativeError

trait ScenariosErrorHandler[F[_], E <: Throwable] {
  def handle(scenario: Scenario[F, Unit], chat: Chat): Scenario[F, Unit]
}

sealed trait ProfitCalculatorError                               extends Exception
case class TelegramParameterExist(param: String)                 extends ProfitCalculatorError
case class TelegramParameterCorrect(param: String)               extends ProfitCalculatorError
case class TelegramParameterUsefulForMarketTicker(param: String) extends ProfitCalculatorError

class ProfitCalculatorScenarioErrorHandler[F[_]: TelegramClient : ApplicativeError[*[_], ProfitCalculatorError]]
  extends ScenariosErrorHandler[F, ProfitCalculatorError] {

  private def handler(chat: Chat): Throwable => Scenario[F, Unit] = {
    case TelegramParameterExist(param)                 =>
      Scenario.eval(chat.send(s"$param This command need parameter to be exist like - /p ticker_name")) >> Scenario.done
    case TelegramParameterCorrect(param)               => Scenario.eval(chat.send(s" $param text should not have empty spaces")) >> Scenario.done
    case TelegramParameterUsefulForMarketTicker(param) => Scenario.eval(chat.send(s" $param - unknown ticker")) >> Scenario.done
  }

  override def handle(scenario: Scenario[F, Unit], chat: Chat): Scenario[F, Unit] =
    ScenarioErrorHandler(scenario)(handler(chat))
}

object ScenarioErrorHandler {
  type Scenarios[F[_]] = Scenario[F, Unit]
  def apply[F[_]: ApplicativeError[*[_], E], E <: Throwable](scenario: Scenarios[F])(handler: Throwable => Scenarios[F]): Scenarios[F] =
    scenario.handleErrorWith(e => handler(e))
}

object ScenariosErrorHandler {
  def apply[F[_], E <: Throwable](implicit ev: ScenariosErrorHandler[F, E]): ScenariosErrorHandler[F, E] = ev
}