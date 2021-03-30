package tcs4sclient.model.domain

import io.circe.Encoder
import io.circe.generic.semiauto._

final case class MoneyAmount(
  currency: String,
  value: BigDecimal
)

object MoneyAmount {
  implicit val decoderMoneyAmount                       = deriveDecoder[MoneyAmount]
  implicit val encoderMoneyAmount: Encoder[MoneyAmount] = deriveEncoder[MoneyAmount]
}
