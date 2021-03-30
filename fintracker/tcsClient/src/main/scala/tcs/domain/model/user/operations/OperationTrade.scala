package tcs.domain.model
package user.operations

import io.circe.Decoder
import io.circe.generic.semiauto._

final case class OperationTrade(
  tradeId: String,
  date: String,
  price: BigDecimal ,
  quantity: Int
)

object OperationTrade {
  implicit val decoderOperationTrade: Decoder[OperationTrade] = deriveDecoder[OperationTrade]
}
