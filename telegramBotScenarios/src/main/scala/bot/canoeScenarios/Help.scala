package bot.canoeScenarios

import canoe.api.{ chatApi, Scenario, TelegramClient }
import canoe.syntax._
import cats.effect.Sync

object Help {
  def run[F[_]: Sync: TelegramClient]: Scenario[F, Unit] =
    for {
      chat <- Scenario.expect(command("h").chat)
      _    <- Scenario.eval(chat.send(helpText))
    } yield ()

  val helpText: String =
    """
      |/h Shows help menu
      |/a Shows available accounts
      |/p [ticker] Check current profit of stock
      |/l List current stock positions in your profile
      |""".stripMargin
}
