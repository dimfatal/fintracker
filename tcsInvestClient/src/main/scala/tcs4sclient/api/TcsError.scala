package tcs4sclient.api

import tcs4sclient.model.domain.{ TcsErrorResponse, TcsResponse }

sealed trait TcsError extends Throwable

final case class ResponseDecodingError(json: String) extends TcsError

final case class FailedMethod[A](path: String, response: TcsResponse[A])  extends TcsError
final case class FailedResponse(path: String, response: TcsErrorResponse) extends TcsError
