package tcs.domain.model

import io.circe.generic.semiauto.deriveDecoder

//final case class TcsServerError(
//  trackingId: String,
//  status: String,
//  payload: Error
//)

final case class TcsError(message: String, code: String)

object TcsErrorResponseDecoder {

  val tcsErrorDecoder = deriveDecoder[TcsError]
}
