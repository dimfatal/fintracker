package bot.canoeScenarios

import bot.canoeScenarios.validation.{CommandParameter, CommandParameterValidation, CommandParameterValidator, CommandParameterValidatorInterpreter}
import canoe.api.{Scenario, TelegramClient, chatApi}
import canoe.models.Chat
import canoe.syntax._
import cats.effect.Sync
import cats.implicits._
import tcs4sclient.model.domain.user.AccountType

trait ScenarioLogic[F[_]] {
  def start: Scenario[F, Unit]
  def accounts: Scenario[F, Unit]
  def stockProfit: Scenario[F, Unit]
  def displayPositions(account: AccountType): Scenario[F, Unit]
  def displayStockPrices(account: AccountType): Scenario[F, Unit]
  def findMarketInstrumentsByTicker: Scenario[F, Unit]
}

object ScenariosLogicInterpreter {
  // def apply[F[_]: ConcurrentEffect: TelegramClient: TinkoffClient](
  //   semaphore: Semaphore[F]
  // )(implicit accountsStorage: InMemoryAccountsStorage[F]): ScenarioLogic[F] = new ScenarioLogic[F] {

  //   override def start: Scenario[F, Unit] = ???

  //   override def accounts: Scenario[F, Unit] = ???

  //   override def stockProfit: Scenario[F, Unit] = ???

  //   override def displayPositions(account: AccountType): Scenario[F, Unit] = ???

  //   override def displayStockPrices(account: AccountType): Scenario[F, Unit] = ???

  //   override def findMarketInstrumentsByTicker: Scenario[F, Unit] = ???
  // }

  private implicit val commandValidator: CommandParameterValidator[Either[CommandParameterValidation, *]] =
    CommandParameterValidatorInterpreter.commandValidator[Either[CommandParameterValidation, *], CommandParameterValidation](identity)

  def checkParam[F[_]: Sync: TelegramClient](chat: Chat, param: Array[String]): F[Either[CommandParameterValidation, CommandParameter]] =
    if (param.nonEmpty) {
      Sync[F].delay(CommandParameterValidator.validate(CommandParameter(param, 1)))
    } else
      chat
        .send("enter ticker which you want to check")
        .map(tickerMessage => CommandParameterValidator.validate(CommandParameter(tickerMessage.text.split(" "), expectedCount = 1)))

}
