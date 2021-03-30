package telegram.bot.dsl

import canoe.api.{Scenario, chatApi, TelegramClient => TgClient}
import canoe.syntax.{command, textContent, _}
import cats.FlatMap
import cats.effect.{IO, Sync}
import cats.implicits._
import tcs.api.client.HttpClient

trait BotScenarios[F[_]] {
  def start: Scenario[F, Unit]
  def help: Scenario[F, Unit]

  def positionsInfo: Scenario[F, Unit]
  //    def del: Scenario[F, Unit]
  //    def list: Scenario[F, Unit]
}

trait Scenarios extends Broker {
  private def tcs: HttpClient[IO] = tcsClient

  implicit def canoeClient: TgClient[IO]

  def broker: TinkoffBroker[IO] = new Broker {
    override implicit def tcsClient: HttpClient[IO] = tcs
  }.tinkoffBroker

  def scenarios: BotScenarios[IO] = new BotScenarios[IO] {

    override def start: Scenario[IO, Unit] = ???

    override def help: Scenario[IO, Unit] = ???

    override def positionsInfo: Scenario[IO, Unit] =
      for {
        chat <- Scenario.expect(command("p").chat)
        cc  <- Scenario.eval(IO(broker.positionsProfit))
       // res  = cc.compile.foldMonoid.unsafeRunSync()
        _   <- {
          val res = //chat.send("Listing your stocks:") *>
            cc.compile.foldMonoid.flatMap(
              i => chat.send(textContent(i.mkString("\n")))
            )
          Scenario.eval(res)
        }

      } yield ()


  }

}


trait ScenariosProgram[F[_]] {
  def scenariosProgram: BotScenarios[F]
}

object ScenariosProgram {
  object Scenarios1 {
    def dsl[F[_]: Sync : TgClient](implicit
                                  B: TinkoffBroker1[F]) : ScenariosProgram[F] =
    new ScenariosProgram[F] {

      import B._

      val F: Sync[F] = implicitly[Sync[F]]

      def scenariosProgram: BotScenarios[F] = new BotScenarios[F] {

        override def start: Scenario[F, Unit] = ???

        override def help: Scenario[F, Unit] = ???

        override def positionsInfo: Scenario[F, Unit] = {
          for {
            chat <- Scenario.expect(command("p").chat)
            posProfit  <- Scenario.eval(F.delay(positionsProfit1))
            _   <- {
              val res = FlatMap[F].flatMap(posProfit.compile.foldMonoid)(i =>chat.send(textContent(i.mkString("\n"))))
              Scenario.eval(res)
            }
          } yield ()
        }
      }
    }
  }
}

