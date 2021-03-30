package tcs4sclient.api.client

import cats.effect.{ ConcurrentEffect, Resource, Sync }
import fs2.Stream
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder

import scala.concurrent.ExecutionContext

trait Http4sBlazeClientAlg[S[_[_], _]] {
  def build[F[_]: ConcurrentEffect](ec: ExecutionContext): S[F, Client[F]]
}

object Http4sBlazeClientResource extends Http4sBlazeClientAlg[Resource] {
  override def build[F[_]: ConcurrentEffect](ec: ExecutionContext): Resource[F, Client[F]] = BlazeClientBuilder[F](ec).resource
}

object Http4sBlazeClientStream extends Http4sBlazeClientAlg[Stream] {
  override def build[F[_]: ConcurrentEffect](ec: ExecutionContext): Stream[F, Client[F]] = BlazeClientBuilder[F](ec).stream
}

object Http4sTinkoffClientBuilder {

  private implicit def defaultLogger[F[_]: Sync]: Logger[F] = Slf4jLogger.getLogger[F]

  /**
   * Creates an authorized asynchronous Tinkoff invest API client wrapped in Resource   *
   * @param token Tinkoff invest token
   * @param ec Execution context
   */
  def resource[F[_]: ConcurrentEffect](token: String, ec: ExecutionContext): Resource[F, TinkoffClient[F]] =
    Http4sBlazeClientResource.build(ec).map(new Tinkoff4STcsClient[F](token, _))

  /**
   * Creates an authorized asynchronous Tinkoff invest API client wrapped in Stream,
   * which works on `global` ExecutionContext.
   *
   * @param token Tinkoff invest token
   * @param ec Execution context
   */
  def stream[F[_]: ConcurrentEffect](token: String)(ec: ExecutionContext): Stream[F, TinkoffClient[F]] =
    Http4sBlazeClientStream.build(ec).map(new Tinkoff4STcsClient[F](token, _))

  /**
   * Creates an authorized asynchronous Telegram Bot API out of http4s Client.
   *
   * @param token Tinkoff invest token
   * @param client http4s client
   */
  def fromHttp4sClient[F[_]: Sync](token: String)(client: Client[F]): TinkoffClient[F] =
    new Tinkoff4STcsClient[F](token, client)
}
