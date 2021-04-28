package bot.canoeScenarios.scenarioMtl

import bot.inMemoryStorage._
import canoe.api.{ chatApi, Scenario, TelegramClient }
import canoe.syntax._
import cats.effect.Sync
import cats.effect.concurrent.Semaphore
import cats.syntax.all._
import org.http4s.client.Client
import tcs4sclient.api.client.{ Http4sTinkoffClientBuilder, TinkoffClient }
import tcs4sclient.model.domain.market.MarketInstrument
import tcs4sclient.model.domain.user.{ AccountType, Tinkoff }
import com.olegpy.meow.hierarchy._

trait ProfitCalculator[F[_]] {
  def calculate(tickerName: String, account: AccountType): fs2.Stream[F, Map[MarketInstrument, BigDecimal]]
}

class CheckStockProfitScenario[F[_]: Sync: TelegramClient: InMemoryAccountsStorage: Client](
  tokenStore: TinkoffTokenStorage[F],
  account: AccountType = Tinkoff
) {

  private val handler = new ProfitCalculatorScenarioErrorHandler()

  val tinkoff: F[TinkoffClient[F]] = tokenStore.get.map(Http4sTinkoffClientBuilder.fromHttp4sClient(_)(implicitly[Client[F]]))

  def runIfAvailable(semaphore: Semaphore[F]): Scenario[F, Unit] =
    Scenario.eval(semaphore.available).flatMap { i =>
      if (i > 0) {
        scenario
      } else Scenario.expect(command("p").chat).flatMap(chat => Scenario.eval(chat.send("run /s - command"))) >> Scenario.done
    }

  private def scenario: Scenario[F, Unit] =
    Scenario.expect(command("p").andThen(x => (x.chat, x.text.split(" ").tail.mkString(" ")))).flatMap { case (chat, param) =>
      handler.handle(
        for {
          service       <- Scenario.eval(tinkoff.map(implicit c => ProfitCalculatorInterpreter.makeScenario))
          serviceAnswer <- Scenario.eval(service.calculate(param, account).compile.toList.map(_.head))
          _             <- Scenario.eval(chat.send(serviceAnswer.mkString("\n"))) >> Scenario.done
        } yield (),
        chat
      )
    }
}
