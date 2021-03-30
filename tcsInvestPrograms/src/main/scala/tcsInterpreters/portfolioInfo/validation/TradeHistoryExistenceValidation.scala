package tcsInterpreters.portfolioInfo.validation

import cats.ApplicativeError
import cats.implicits._
import tcs4sclient.model.domain.TradeHistory
import tcs4sclient.model.domain.user.operations.Operation

case object TradeHistoryExistenceValidation extends StockValidation {
  def errorMessage: String = "There is no trade history exist with this stock."
}

sealed trait TradeHistoryExistenceValidator[F[_]] {
  def hasOperationHistory(operations: TradeHistory): F[TradeHistory]
}

object TradeHistoryExistenceValidator {
  def apply[F[_]](implicit tradeHistoryValidator: TradeHistoryExistenceValidator[F]): TradeHistoryExistenceValidator[F] = tradeHistoryValidator

  def validate[F[_]: TradeHistoryExistenceValidator, E](op: TradeHistory): F[TradeHistory] =
    TradeHistoryExistenceValidator[F].hasOperationHistory(op)
}

object TradeHistoryExistenceValidatorInterpreter {
  def operationHistoryExistenceValidator[F[_], E](
    mkError: StockValidation => E
  )(implicit A: ApplicativeError[F, E]): TradeHistoryExistenceValidator[F] =
    new TradeHistoryExistenceValidator[F] {
      override def hasOperationHistory(tradeHistory: TradeHistory): F[TradeHistory] =
        validateTradeHistoryNotEmpty(tradeHistory.operations).map(TradeHistory)

      def validateTradeHistoryNotEmpty(operations: List[Operation]): F[List[Operation]] =
        if (operations.nonEmpty) operations.pure[F]
        else A.raiseError(mkError(TradeHistoryExistenceValidation))
    }

}
