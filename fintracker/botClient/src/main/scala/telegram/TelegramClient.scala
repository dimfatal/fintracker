//package telegram
//
//import canoe.api.{Bot, Scenario, TelegramClient, chatApi}
//import canoe.syntax._
//import cats.Applicative
//import cats.effect.{ConcurrentEffect, ContextShift, IO, Timer}
//import fs2.Stream
//import tcs.api.client.TcsClient
//import tcs.broker.tracker.PositionsOper
//
//object TelegramClient {
//
//
//  trait Canoe[F[_]: ConcurrentEffect: Applicative: ContextShift] {
//    implicit def canoeClient : TelegramClient[F]
//
//    def positionInfo : Scenario[F, Unit] = {
//      for {
//        msg      <- Scenario.expect(command("p"))
//        cc <- Scenario.eval(tinkoffClientRunner())
//        _        <- Scenario.eval(msg.chat.send(textContent(cc.flatten.mkString("\n"))))
//      } yield ()
//    }
//
//    private def client(tcsToken : String)  = {
//      Stream
//        .resource(TcsClient.global[IO](tcsToken))
//      //.flatMap { implicit client => client }
//    }
//
//    def tinkoffClientRunner(): F[List[String]]  = {
//
//      val tcsToken = "t.dZIHJ-nk1gzpK7L5d8lrAX-uYQxvRm5Ppsum022OeDUTWO6_YLnKKXHkj2n8nwoTqtpJLLL79QuMhXfrla6v8w" //todo use sys.env
//
//      val ss = client(tcsToken)
//        .flatMap { implicit client =>
//          for {
//            res <- PositionsOper.positionsProfit[IO]
//            s = println(res)
//          } yield res
//        }.covary[IO].compile.toList
//    }
//  }
//}
//
//trait TelegramClient[F[_]] {
//  val token : String
//  val canoeClient : Stream[F, Client[F]]
//  val scenarios: Stream[F, Scenarios]
//}
//
//object CanoeClient {
//  def make[F[_] : ConcurrentEffect : Applicative: Timer] : TelegramClient[F] = new TelegramClient[F] {
//    val token : String = ""
//    override val canoeClient: Stream[F, Client[F]] = Stream.resource(Client.global[F](token))
//    override val scenarios: Stream[F, Any] = ???
//
//    scenarios.flatMap(scenarios =>
//      canoeClient.flatMap{ implicit client =>
//        Bot
//          .polling[F]
//          .follow(scenarios )
//
//      }
//    )
//  }
//}