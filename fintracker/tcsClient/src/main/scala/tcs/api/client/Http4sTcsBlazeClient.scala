package tcs.api.client

import cats.Id
import cats.effect.{ConcurrentEffect, IO, Resource, Sync}

import scala.concurrent.ExecutionContext
import io.circe.Decoder
import org.http4s.client.blaze.BlazeClientBuilder
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s.{EntityDecoder, Method}
import org.http4s.client.Client
import fs2.Stream
import tcs.domain.model.user.Accounts

//trait ApiService[F[_]]{
//  def accounts : F[Accounts]
//}

//trait TcsApiService[A] extends HttpClient[A]
//
//object TcsApiServiceAccounts {
//  val account : TcsApiService[Accounts] = new TcsApiService[Accounts] {
//    override def execute[F[_] : EntityDecoder[F, Accounts]](F : ApiService[F]): F[Accounts] = F.accounts
//  }
//}

//trait Http4sTcsClientAlg[F[_], S[_[_], _]] extends Http4sClientAlg[F, S[F, Client[F]]]{}

//visitor pattern instead tagless final
trait Http4sClientAlgVer1[F[_]] {
  def stream[S[_[_], _]]( ec: ExecutionContext): S[F, Client[F]]//Stream[F, HttpClient]
  def resource[S[_[_], _]]( ec: ExecutionContext): S[F, Client[F]] //Resource[F, Client[F]]
}

trait HttpClientVer1[S[_[_], _]] {
  def run[F[_] ](F: Http4sClientAlgVer1[F]): S[F, Client[F]]
}

object HttpClientVer1 {
  def stream(ec: ExecutionContext): HttpClientVer1[Stream] = new HttpClientVer1[Stream] {
    override def run[F[_]](F: Http4sClientAlgVer1[F]): Stream[F, Client[F]] = F.stream[Stream]( ec)
  }
}
//trait HttpClient1[F[_], S[_[_], _]] {
//  def run(F: Http4sClientAlg[F, S[F, Client[F]]]): S[F, Client[F]]
//}

//object HttpClientStream extends HttpClient1[Id, Stream] {
//  override def run(F: Http4sClientAlg[Id, Stream[Id, Client[Id]]]): Stream[Id, Client[Id]] = ???
//}

//trait HttpClientStream extends HttpClient {
//  def runS[F[_]](F : Http4sClientAlg[F, Stream[F, Client[F]]]): Stream[F, Client[F]] = run[F, Stream[F, Client[F]]](F)
//}
//(F: Http4sClientAlg[F, Stream[F, Client[F]]])
//
//trait HttpClientStream[F[_]] extends HttpClient {
//  def runStream = run[F, Stream[F, *]]
//}


trait Http4sClientAlg[S[_[_], _]]{
  def build [F[_]: ConcurrentEffect](ec: ExecutionContext): S[F, Client[F]]//Stream[F, HttpClient]
  // def resource[S[_[_], _]]( ec: ExecutionContext): S[F, Client[F]] //Resource[F, Client[F]]
}

//haskel mtl style
object Http4sClientAlg  {
  def apply[F[_[_], _]](implicit F: Http4sClientAlg[F]): Http4sClientAlg[F] = F

  implicit def http4sClientBuilderStream : Http4sClientAlg[Stream] = new Http4sClientAlg[Stream] {
    override def build[F[_]: ConcurrentEffect](ec: ExecutionContext): Stream[F, Client[F]] = BlazeClientBuilder[F](ec).stream
  }

  implicit def http4sClientBuilderResource : Http4sClientAlg[Resource] = new Http4sClientAlg[Resource] {
    override def build[F[_]: ConcurrentEffect](ec: ExecutionContext): Resource[F, Client[F]] = BlazeClientBuilder[F](ec).resource
  }
}

//Scala tagless final style
class Http4sTcsBlazeClientImpl[S[_[_], _] : Http4sClientAlg] {
  def build[F[_] : ConcurrentEffect](ec: ExecutionContext): S[F, Client[F]] = Http4sClientAlg[S].build(ec)
}

object Http4sTcsBlazeClientImplResource extends Http4sTcsBlazeClientImpl[Resource]{
  override def build[F[_] : ConcurrentEffect](ec: ExecutionContext): Resource[F, Client[F]] = BlazeClientBuilder[F](ec).resource
}

object Http4sTcsBlazeClient {

  private implicit def defaultLogger[F[_]: Sync]: Logger[F] = Slf4jLogger.getLogger[F]

  /**
   * Creates an authorized asynchronous Tinkoff invest API client wrapped in Resource.
   * After it is used, client is going to be released.
   *
   * @param token Tinkoff invest token
   * @param ec    Dedicated ExecutionContext
   */
//  def apply[F[_]: ConcurrentEffect](token: String, ec: ExecutionContext): Resource[F, HttpClient[F]] =
//    BlazeClientBuilder[F](ec).resource.map(new Http4sTcsClient[F](token, _))
// apply(scala.concurrent.ExecutionContext.global).resource.map(new Http4sTcsClient[F](token, _))
//apply(scala.concurrent.ExecutionContext.global).stream.map(new Http4sTcsClient[F](token,_))
  def apply[F[_]: ConcurrentEffect](ec: ExecutionContext): BlazeClientBuilder[F] =
    BlazeClientBuilder[F](ec)
  /**
   * Creates an authorized asynchronous Tinkoff invest API client wrapped in Resource,
   * which works on `global` ExecutionContext.
   *
   * @param token Tinkoff invest token
   */
  def resource[F[_]: ConcurrentEffect](token: String)(ec: ExecutionContext): Resource[F, Http4sTcsClient[F]] =
    Http4sTcsBlazeClientImplResource.build(ec).map(new Http4sTcsClient[F](token,_))
//    implicitly[Http4sClientAlg[Resource]].build(ec)
//      .map(new Http4sTcsClient[F](token, _))

  import Http4sClientAlg._ //: Http4sClientAlg[Stream]
  def stream[F[_]: ConcurrentEffect](token: String)(ec: ExecutionContext): Stream[F, Http4sTcsClient[F]] =
    http4sClientBuilderStream.build(ec).map(new Http4sTcsClient[F](token, _))
    //implicitly[Http4sClientAlg[Stream]].build(ec).map(new Http4sTcsClient[F](token, _))


  /**
   * Creates an authorized asynchronous Telegram Bot API out of http4s Client.
   *
   * @param token Tinkoff invest token
   */
  def fromHttp4sClient[F[_]: Sync](token: String)(client: Client[F]): HttpClient[F] =
    new Http4sTcsClient[F](token, client)

}
