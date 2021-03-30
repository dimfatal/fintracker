package tcs.domain.model
package user.operations
import io.circe._
import io.circe.generic.semiauto._
import tinkoffInvest.model.domain.MoneyAmount

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
  operationType: Option[String]
)

object OperationDecoder {
  implicit val decoderOperation: Decoder[Operation] = deriveDecoder[Operation]
  implicit val decoderOperations: Decoder[Operations]               = deriveDecoder[Operations]
  // implicit val encoderOperation: Encoder.AsObject[Operation] = deriveEncoder[Operation]
}
