package bot.canoeScenarios.validation

import cats.ApplicativeError
import cats.implicits._

trait CommandParameterValidation {
  def errorMessage: String
}

case object CommandParameterExistValidation extends CommandParameterValidation {
  def errorMessage: String = "This command need parameter to be exist like '/yourCommand parameter'"
}

case object CommandParameterExpectedCount extends CommandParameterValidation {
  def errorMessage: String = "Please provide only one parameter for this command"
}

case class CommandParameter(args: Array[String], expectedCount: Int)

object CommandParameterValidator {
  def apply[F[_]](implicit cm: CommandParameterValidator[F]): CommandParameterValidator[F] = cm

  def validate[F[_]: CommandParameterValidator, E](commandParameter: CommandParameter): F[CommandParameter] =
    CommandParameterValidator[F].parameterValid(commandParameter)
}

sealed trait CommandParameterValidator[F[_]] {
  def parameterValid(commandParameter: CommandParameter): F[CommandParameter]
}

object CommandParameterValidatorInterpreter {
  def commandValidator[F[_], E](mkError: CommandParameterValidation => E)(implicit A: ApplicativeError[F, E]): CommandParameterValidator[F] =
    new CommandParameterValidator[F] {
      override def parameterValid(commandParameter: CommandParameter): F[CommandParameter] =
        (CommandParameter.apply _).curried.pure[F] <*>
          parameterNotEmpty(commandParameter) <*>
          commandParameterCountAsExpected(commandParameter)

      private def parameterNotEmpty(commandParameter: CommandParameter): F[Array[String]] =
        if (commandParameter.args.nonEmpty) commandParameter.args.pure[F]
        else A.raiseError(mkError(CommandParameterExistValidation))

      private def commandParameterCountAsExpected(commandParameter: CommandParameter): F[Int] =
        if (commandParameter.expectedCount == commandParameter.args.length) commandParameter.expectedCount.pure[F]
        else A.raiseError(mkError(CommandParameterExpectedCount))
    }

}
