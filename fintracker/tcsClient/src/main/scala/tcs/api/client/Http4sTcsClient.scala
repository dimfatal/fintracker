package tcs.api.client

import org.http4s._
import org.http4s.implicits.http4sLiteralsSyntax
import org.http4s.client.Client
import cats.effect.Sync
import cats.syntax.all._
import io.chrisdavenport.log4cats.Logger
import io.circe.Decoder
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.headers.{Authorization, `Content-Type`}
import tcs.api._
import tcs.domain.model.TcsResponse
import tcs.domain.model.TcsResponse.decoder


trait HttpClient[F[_]]{
  def execute[Res: Decoder](method: Method, path: String, params: Map[String, String]): F[Res]
}

private[client] class Http4sTcsClient[F[_]: Sync: Logger](token: String, client: Client[F]) extends HttpClient[F] {

  private def urlBuilder(path: String, params: Map[String, String] = Map.empty): Uri =
    uri"https://api-invest.tinkoff.ru" / "openapi" / s"$path" =?
      params.map { case (key, value) =>
        (key, List(value))
      }

  override def execute[Res: Decoder](method: Method, path: String, params: Map[String, String]): F[Res] = {

    val req = Request[F]()
      .withMethod(method)
      .withUri(urlBuilder(path, params))
      .withHeaders(Authorization(Credentials.Token(AuthScheme.Bearer, token)))
      .withContentType(`Content-Type`(MediaType.application.json))

    Logger[F].info(s"Executing '$path' Tinkoff method.") *>
      client
        .expect[TcsResponse[Res]](req)
        .recoverWith { case error: InvalidMessageBodyFailure => handleUnknownEntity(path, error) }
        .flatMap(handleTcsResponse(path))
  }



  private def handleUnknownEntity[I, A](method: String, error: InvalidMessageBodyFailure): F[A] =
    Logger[F].error(
      s"Received unknown Tinkoff invest entity during execution of '$method' method. \n${error.details}"
    ) *>
      ResponseDecodingError(error.details.dropWhile(_ != '{')).raiseError[F, A]

  private def handleTcsResponse[A, I, C](path: String)(response: TcsResponse[A]): F[A] =
    response match {
      case TcsResponse(_, _, Some(payload)) => payload.pure[F]

      case failed =>
        Logger[F].error(s"Received failed response from Tcs: $failed. Method name: ${path}") *>
          FailedMethod(path, failed).raiseError[F, A]
    }
}
