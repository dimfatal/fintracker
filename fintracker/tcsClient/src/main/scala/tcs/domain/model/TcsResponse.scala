package tcs.domain.model

import io.circe._
import io.circe.generic.semiauto._
import tcs.domain.model.user.operations.Operation
import tcs.domain.model.user.portfolio.Position
import tcs.domain.model.market.MarketInstrument

final case class TcsResponse[A](trackingId: String, status: String, payload: Option[A])
final case class Operations(operations: List[Operation])
final case class Positions(positions: List[Position])
final case class MarketInstruments(total: Option[Int], instruments: List[MarketInstrument])


object TcsResponse {
  implicit def decoder[A: Decoder]: Decoder[TcsResponse[A]] = deriveDecoder[TcsResponse[A]]

  //implicit val decoderOperations: Decoder[Operations]               = deriveDecoder[Operations]
  //implicit val decoderPositions: Decoder[Positions]                 = deriveDecoder[Positions]
  //implicit val decoderMarketInstruments: Decoder[MarketInstruments] = deriveDecoder[MarketInstruments]
  implicit val decoderTcsError : Decoder[TcsError] = deriveDecoder[TcsError]

}
