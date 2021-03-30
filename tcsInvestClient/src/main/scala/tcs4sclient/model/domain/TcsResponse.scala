package tcs4sclient.model.domain

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import tcs4sclient.model.domain.market.MarketInstrument
import tcs4sclient.model.domain.user.operations.Operation
import tcs4sclient.model.domain.user.portfolio.Position

final case class TcsResponse[A](trackingId: String, status: String, payload: Option[A])

final case class TradeHistory(operations: List[Operation])
final case class Portfolio(positions: List[Position])
final case class MarketInstruments(total: Int, instruments: List[MarketInstrument])

object TcsResponse {
  implicit def tcsResponse[A: Decoder]: Decoder[TcsResponse[A]] = deriveDecoder[TcsResponse[A]]
}

object EitherResponse {

  type TcsResponseOrError[A] = Either[TcsResponse[TcsErrorResponse], TcsResponse[A]]

  implicit def eitherDecoder[A: Decoder]: Decoder[TcsResponseOrError[A]] = {

    import TcsResponse.tcsResponse
    import TcsErrorResponseDecoder.tcsErrorDecoder

    val left: Decoder[TcsResponseOrError[A]]  = tcsResponse[TcsErrorResponse].map(Left.apply)
    val right: Decoder[TcsResponseOrError[A]] = deriveDecoder[TcsResponse[A]].map(Right.apply)
    left or right
  }
}
