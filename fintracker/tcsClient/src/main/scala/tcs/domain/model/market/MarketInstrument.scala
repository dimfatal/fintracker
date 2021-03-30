package tcs.domain.model
package market

import io.circe.Decoder
import io.circe.generic.semiauto._

final case class MarketInstrument(
  figi: String,
  ticker: String,
  isin: Option[String],
  minPriceIncrement: Option[Double],
  lot: Double,
  currency: String,
  name: String,
  `type`: String
)

sealed trait InstrumentId
final case class Figi(id: String)   extends InstrumentId
final case class Ticker(id: String) extends InstrumentId

object MarketInstrument {
  implicit val decoderMarketInstrument: Decoder[MarketInstrument] = deriveDecoder[MarketInstrument]
  implicit val decoderMarketInstruments: Decoder[MarketInstruments] = deriveDecoder[MarketInstruments]
}
