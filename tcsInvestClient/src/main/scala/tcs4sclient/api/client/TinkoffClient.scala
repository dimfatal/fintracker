package tcs4sclient.api.client

import cats.effect.Sync
import cats.syntax.all._
import io.chrisdavenport.log4cats.Logger
import io.circe.Decoder
import org.http4s._
import org.http4s.circe.jsonOf
import org.http4s.client.Client
import org.http4s.headers.{ `Content-Type`, Authorization }
import org.http4s.implicits.http4sLiteralsSyntax
import tcs4sclient.api.{ FailedMethod, FailedResponse, ResponseDecodingError }
import tcs4sclient.model.domain.EitherResponse.TcsResponseOrError
import tcs4sclient.model.domain.TcsResponse

trait TinkoffClient[F[_]] {
  def execute[A: Decoder](method: Method, path: String, params: Map[String, String]): F[A]
}

private[client] class Tinkoff4STcsClient[F[_]: Sync: Logger](token: String, client: Client[F]) extends TinkoffClient[F] {

  private def urlBuilder(path: String, params: Map[String, String]): Uri =
    uri"https://api-invest.tinkoff.ru" / "openapi" / s"$path" =?
      params.map { case (key, value) =>
        (key, List(value))
      }

  override def execute[A: Decoder](method: Method, path: String, params: Map[String, String]): F[A] = {

    import tcs4sclient.model.domain.EitherResponse.eitherDecoder
    implicit val decoder: EntityDecoder[F, TcsResponseOrError[A]] =
      jsonOf(Sync[F], eitherDecoder[A])

    val req = Request[F]()
      .withMethod(method)
      .withUri(urlBuilder(path, params))
      .withHeaders(Authorization(Credentials.Token(AuthScheme.Bearer, token)))
      .withContentType(`Content-Type`(MediaType.application.json))

    Logger[F].info(s"Executing '$path' Tinkoff method. ${req.uri}") *>
      client
        .expect[TcsResponseOrError[A]](req)
        .recoverWith { case error: InvalidMessageBodyFailure => handleUnknownEntity(path, error) }
        .flatMap(handleTcsResponse(path))
  }

  private def handleUnknownEntity[A](method: String, error: InvalidMessageBodyFailure): F[A] =
    Logger[F].error(s"Received unknown 'Tinkoff invest' entity during execution of '$method' method. \n${error.details}") *>
      ResponseDecodingError(error.details.dropWhile(_ != '{')).raiseError[F, A]

  private def handleTcsResponse[A](path: String)(response: TcsResponseOrError[A]): F[A] =
    response match {
      case Right(response) =>
        response match {
          case TcsResponse(_, _, Some(payload)) => payload.pure[F]

          case failed =>
            Logger[F].error(s"Received failed response from Tcs: $failed. Method name: ${path}") *>
              FailedMethod(path, failed).raiseError[F, A]
        }

      case Left(response) =>
        response match {
          case TcsResponse(_, _, Some(payload)) =>
            Logger[F].error(s"Received tcs error response - ${payload}. Method name: $path") *>
              FailedResponse(path, payload).raiseError[F, A]

          case failed =>
            Logger[F].error(s"Received failed response from Tcs: $failed. Method name: $path") *>
              FailedMethod(path, failed).raiseError[F, A]
        }

    }
}
