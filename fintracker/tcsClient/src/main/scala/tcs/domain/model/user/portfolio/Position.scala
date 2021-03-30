package tcs.domain.model
package user.portfolio

import io.circe.Decoder
import io.circe.generic.semiauto._
import tinkoffInvest.model.domain.MoneyAmount

final case class Position(
  figi: String,
  ticker: Option[String],
  isin: Option[String],
  instrumentType: String,
  balance: Double,
  blocked: Option[Double],
  expectedYield: Option[MoneyAmount],
  lots: Int,
  averagePositionPrice: Option[MoneyAmount],
  averagePositionPriceNoNkd: Option[MoneyAmount],
  name: String
)

object Position {
  implicit val decoderPosition = deriveDecoder[Position]
  implicit val decoderPositions: Decoder[Positions]                 = deriveDecoder[Positions]
}
