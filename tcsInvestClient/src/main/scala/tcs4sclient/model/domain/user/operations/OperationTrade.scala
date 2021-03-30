package tcs4sclient.model.domain.user.operations

import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto._

final case class OperationTrade(
  tradeId: String,
  date: String,
  price: BigDecimal,
  quantity: Int
)

object OperationTrade {
  implicit val decoderOperationTrade: Decoder[OperationTrade] = deriveDecoder[OperationTrade]
  implicit val encoderOperationTrade: Encoder[OperationTrade] = deriveEncoder[OperationTrade]
}
