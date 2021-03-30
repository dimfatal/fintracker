package tcs.broker.tracker

import broker.interpreters.BrokerApi._
import canoe.api.{Bot, Scenario, TelegramClient}
import cats.Applicative
import cats.effect.{ConcurrentEffect, ExitCode, IO, IOApp}
import fs2._
import org.http4s.client.blaze.BlazeClientBuilder
import tcs.api.client.HttpClient
import tcs.domain.model.user.Tinkoff
import telegram.bot.dsl.ProgramBroker1._
import telegram.bot.dsl.{ProgramBroker1, TinkoffBroker1}
import telegram.bot.interpreter.TelegramBot1._
import telegram.bot.interpreter.{TelegramBot, TelegramBot1}

import scala.concurrent.ExecutionContext.global

object Main extends IOApp {
  private def telegramBotToken: String = "1498629531:AAFzR4eASsMmk0ia8h3j7QWJbVAOqL4585A"
  private def tcsBotToken: String      = "t.dZIHJ-nk1gzpK7L5d8lrAX-uYQxvRm5Ppsum022OeDUTWO6_YLnKKXHkj2n8nwoTqtpJLLL79QuMhXfrla6v8w" //todo use sys.env

  override def run(args: List[String]): IO[ExitCode] = {

    val program = for {
      tinkoffBrokerClient <- makeTcsClient[IO](tcsBotToken)
      broker              = makeBroker(tinkoffBrokerClient).tinkoffBroker
      canoeClient         <- makeCanoeClient[IO](telegramBotToken)
      botScenarios        = makeBotForTcsBroker(canoeClient, broker).run
      scenario            <- Stream(botScenarios.positionsInfo)
      _                   <- Stream.emit(canoeClient)
                             .flatMap(implicit client => Bot.polling[IO].follow(scenario))
    } yield ()

    program
      .compile
      .drain
      .as(ExitCode.Success)

  }

  private def makeTcsClient[F[_]: ConcurrentEffect: Applicative](token: String): Stream[F, HttpClient[F]] = HttpClient.streamGlobal[F](token)
    //Stream.resource(HttpClient.global[F](token))

  private def makeCanoeClient[F[_]: ConcurrentEffect: Applicative](token: String): Stream[F, TelegramClient[F]] =
    BlazeClientBuilder[F](global).stream.map { client =>
      TelegramClient.fromHttp4sClient[F](token)(client)
    }
//    Stream.resource(TelegramClient.global[F](token))

  private def makeBot(tg: TelegramClient[IO], tcs: HttpClient[IO]): Scenario[IO, Unit] =
    TelegramBot.scenarios(tg, tcs).positionsInfo

  private def makeBroker(implicit tcs:HttpClient[IO]): ProgramBroker1[IO] = Broker1.dsl(UserAccountOperations.dsl(Tinkoff))

  private def makeBotForTcsBroker(implicit tg: TelegramClient[IO], broker : TinkoffBroker1[IO]): TelegramBot1[IO] =
    TelegramBot1Program.dsl[IO]

}
