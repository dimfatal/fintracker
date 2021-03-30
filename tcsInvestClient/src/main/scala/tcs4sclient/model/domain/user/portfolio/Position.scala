package tcs4sclient.model.domain.user.portfolio

import io.circe.Decoder
import io.circe.generic.semiauto._
import tcs4sclient.model.domain.{ MoneyAmount, Portfolio }

sealed case class Position(
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
  implicit val decoderPosition: Decoder[Position]   = deriveDecoder[Position]
  implicit val decoderPositions: Decoder[Portfolio] = deriveDecoder[Portfolio]
}
