package tcs4sclient.model.domain.user.operations

import io.circe._
import io.circe.generic.semiauto._
import tcs4sclient.model.domain.{ MoneyAmount, TradeHistory }

case class Operation(
  id: String,
  status: String,
  trades: Option[List[OperationTrade]],
  commission: Option[MoneyAmount],
  currency: String,
  payment: BigDecimal,
  price: Option[BigDecimal],
  quantity: Option[Double],
  figi: Option[String],
  instrumentType: Option[String],
  isMarginCall: Boolean,
  date: String,
  operationType: String
)

object OperationDecoder {
  implicit val decoderOperation: Decoder[Operation]     = deriveDecoder[Operation]
  implicit val decoderOperations: Decoder[TradeHistory] = deriveDecoder[TradeHistory]
  implicit val encoderOperations: Encoder[TradeHistory] = deriveEncoder[TradeHistory]
  implicit val encoderOperation: Encoder[Operation]     = deriveEncoder[Operation]

}
