package telegram.bot.interpreter

import canoe.api.{TelegramClient => TgClient}
import cats.effect.{IO, Sync}
import tcs.api.client.HttpClient
import telegram.bot.dsl.ScenariosProgram.Scenarios1
import telegram.bot.dsl.{BotScenarios, Scenarios, TinkoffBroker1}

object TelegramBot {
  def scenarios(tg: TgClient[IO], tcs: HttpClient[IO]): BotScenarios[IO] = new Scenarios {
    override implicit def canoeClient: TgClient[IO] = tg
    override implicit def tcsClient: HttpClient[IO]  = tcs
  }.scenarios
}


trait TelegramBot1[F[_]] {
  def run :  BotScenarios[F]
}

object TelegramBot1 {
  object TelegramBot1Program {
    def dsl[F[_]: Sync: TgClient: TinkoffBroker1] : TelegramBot1[F] = new TelegramBot1[F] {
      override def run: BotScenarios[F] = Scenarios1.dsl.scenariosProgram
    }
  }
}