package tcs.api.client

import canoe.api.TelegramClient
import cats.effect.{ExitCode, IO, IOApp}
import org.http4s.Method
import org.http4s.client.blaze.BlazeClientBuilder
import tcs.domain.model.user.Accounts

import scala.concurrent.ExecutionContext.global


  object MainStr extends IOApp {

    override def run(args: List[String]): IO[ExitCode] = ???
  }
//{

//      import tcs.domain.model.user.AccountsDecoder.accountsDecoder
//      val token: String      = "t.dZIHJ-nk1gzpK7L5d8lrAX-uYQxvRm5Ppsum022OeDUTWO6_YLnKKXHkj2n8nwoTqtpJLLL79QuMhXfrla6v8w" //todo use sys.env
//
//      BlazeClientBuilder[IO](global).stream.map { client =>
//        HttpClient.fromHttp4sClient[IO](token)(client).execute[Accounts](Method.GET, "/user/account", Map.empty)
//      }
//
//      val ss1 = for {
//        cc <- BlazeClientBuilder[IO](global).stream
//        xx = HttpClient.fromHttp4sClient[IO](token)(cc).execute[Accounts](Method.GET, "/user/account", Map.empty)
//        s = println(xx.unsafeRunSync())
//      } yield xx
////      val ss = for {
////        x <- new TcsStream[IO].execute[Accounts](Method.GET, "/user/account", Map.empty)
////        s = println(x)
////
////      } yield x
//
//      ss1.compile.drain
   // }.as(ExitCode.Success)

   // }


