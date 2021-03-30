package tcs.api

import tcs.domain.model.TcsResponse

sealed trait TcsError extends Throwable

final case class ResponseDecodingError(json: String) extends TcsError

final case class FailedMethod[I, A](path: String, response: TcsResponse[A]) extends TcsError
