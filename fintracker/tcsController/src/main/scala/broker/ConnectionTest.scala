package broker

import broker.interpreters.TcsBroker.UserAccountOperations
import cats.effect.{ExitCode, IO, IOApp, Resource}
import fs2.Stream
import io.chrisdavenport.log4cats.SelfAwareStructuredLogger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import tcs.api.client.HttpClient
import tcs.domain.model.user.Tinkoff

import scala.concurrent.ExecutionContext.global

object ConnectionTest extends IOApp {

  val clientResource: Resource[IO, Client[IO]] = BlazeClientBuilder[IO](global).resource

  val token = "t.dZIHJ-nk1gzpK7L5d8lrAX-uYQxvRm5Ppsum022OeDUTWO6_YLnKKXHkj2n8nwoTqtpJLLL79QuMhXfrla6v8w"

  private implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  override def run(args: List[String]): IO[ExitCode] =
    Stream
      .resource(HttpClient.global[IO](token))
      .flatMap(implicit client => Tinkoff.positions) //BBG003PHHZT1
      .compile
      .toVector
      .map(_.map(s => println(s)))
      .as(ExitCode.Success)
  IO.pure(ExitCode.Success)

}
