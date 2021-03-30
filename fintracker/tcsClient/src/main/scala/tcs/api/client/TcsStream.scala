package tcs.api.client


import cats.effect.{ConcurrentEffect, ContextShift}
import fs2.{Pipe, Stream}
import io.circe.{Decoder, Json}
import jawnfs2._
import org.http4s._
import org.http4s.client.blaze._
import org.http4s.headers.{Authorization, `Content-Type`}
import org.http4s.implicits.http4sLiteralsSyntax
import tcs.domain.model.TcsResponse
import tcs.domain.model.TcsResponse.decoder

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.global

class TcsStream[F[_] : ConcurrentEffect:ContextShift] {

  implicit val f = new io.circe.jawn.CirceSupportParser(None, false).facade
  private def tcsToken: String = "t.dZIHJ-nk1gzpK7L5d8lrAX-uYQxvRm5Ppsum022OeDUTWO6_YLnKKXHkj2n8nwoTqtpJLLL79QuMhXfrla6v8w" //todo use sys.env

  private def urlBuilder(path: String, params: Map[String, String] = Map.empty): Uri =
    uri"https://api-invest.tinkoff.ru" / "openapi" / s"$path" =?
      params.map { case (key, value) =>
        (key, List(value))
      }



  def execute[A : Decoder](method: Method, path: String, params: Map[String, String]): Stream[F, TcsResponse[A]] = {
    val req = Request[F]()
      .withMethod(method)
      .withUri(urlBuilder(path, params))
      .withHeaders(Authorization(Credentials.Token(AuthScheme.Bearer, tcsToken)))
      .withContentType(`Content-Type`(MediaType.application.json))

    jsonStream(global)(req).through(streamDecoder)
  }

  def jsonStream(ec: ExecutionContext)(req: Request[F]): Stream[F, Json] = {
    for {
      client <- BlazeClientBuilder(ec).stream
      json <-  client.stream(req).flatMap(_.body.chunks.parseJsonStream)
    } yield json
  }


  def streamDecoder[A:Decoder]: Pipe[F, Json, TcsResponse[A]] = _.map( //todo handle error
    //todo handle fail request

    decoder.decodeJson(_).getOrElse(TcsResponse("", "", None)))


}


