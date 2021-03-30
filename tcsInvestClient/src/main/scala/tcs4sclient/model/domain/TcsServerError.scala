package tcs4sclient.model.domain

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

final case class TcsErrorResponse(message: String, code: String)

object TcsErrorResponseDecoder {
  implicit val tcsErrorDecoder: Decoder[TcsErrorResponse] = deriveDecoder[TcsErrorResponse]
}
