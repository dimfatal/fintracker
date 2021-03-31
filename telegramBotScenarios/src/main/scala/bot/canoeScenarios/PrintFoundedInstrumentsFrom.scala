package bot.canoeScenarios

import canoe.api._
import canoe.syntax._
import cats.effect.Sync
import tcs4sclient.model.domain.market.Ticker
import tcsInterpreters.TinkoffMarketInfo
import cats.implicits._

object PrintFoundedInstrumentsFrom {
  def printFoundedInstrumentsFrom[F[_]: Sync: TelegramClient](marketInfo: TinkoffMarketInfo[F]): Scenario[F, Unit] =
    for {
      chatAndParam   <- Scenario.expect(command("t").andThen(x => (x.chat, x.text.split(" ").tail)))
      (chat, param)   = chatAndParam
      validParameter <- Scenario.eval(ScenariosLogicInterpreter.checkParam(chat, param)).map(_.map(cmd => Ticker(cmd.args.head))) //todo need review
      s               =
        validParameter.map(marketInfo.searchInstrumentByTicker).map {
          _.map(_.instruments)
            .map(_.map(i => (i.ticker, i.name, i.figi)))
            .map(_.mkString("\n"))
            .compile
            .toList
            .map(_.last)
        }
      _              <- s.fold(
                          e => Scenario.eval(chat.send(e.errorMessage)),
                          res => Scenario.eval(res).flatMap(d => Scenario.eval(chat.send(d)))
                        )

    } yield ()

}
