package bot.canoeScenarios.scenarioMtl

import bot.inMemoryStorage.AccountTypeStorageSyntax.AccountTypeIdOps
import bot.inMemoryStorage.{InMemoryAccountsStorage, TinkoffTokenStorage}
import bot.tinkoff.StockProfitMap
import bot.tinkoff.TinkoffInvestPrograms.{TinkoffInvestLogic, TinkoffService}
import canoe.api.{Scenario, TelegramClient, chatApi}
import canoe.syntax._
import cats.MonadError
import cats.data.OptionT
import cats.effect.Sync
import cats.effect.concurrent.Semaphore
import cats.syntax.all._
import org.http4s.client.Client
import tcs4sclient.api.client.{Http4sTinkoffClientBuilder, TinkoffClient}
import tcs4sclient.api.methods.TinkoffInvestApi
import tcs4sclient.model.domain.market.Ticker
import tcs4sclient.model.domain.user.{AccountType, Tinkoff}
import tcsInterpreters.portfolioInfo.DateTimePeriod.allTime

class CheckStockProfitMtl[F[_]: Sync: TelegramClient: InMemoryAccountsStorage: Client](tokenStore: TinkoffTokenStorage[F],
                                                                                       account: AccountType = Tinkoff) {
  import com.olegpy.meow.hierarchy._

  private val handler = new ProfitCalculatorScenarioErrorHandler()

  val tinkoff: F[TinkoffClient[F]] = tokenStore.get.map(Http4sTinkoffClientBuilder.fromHttp4sClient(_)(implicitly[Client[F]]))

  private def scenario =
    Scenario.expect(command("p").andThen(x => (x.chat, x.text.split(" ").tail.mkString(" ")))).flatMap { case (chat, param) =>
      handler.handle(
        for {
          service       <- Scenario.eval(tinkoff.map(implicit c => ProfitCalculatorInterpreter.makeScenario))
          serviceAnswer <- Scenario.eval(service.calculate(param, account).compile.toList.map(_.head))
          _             <- Scenario.eval(chat.send(serviceAnswer.items.head._2)) >> Scenario.done
        } yield (), chat
      )
    }

  def runIfAvailable(semaphore: Semaphore[F]): Scenario[F, Unit] =
    Scenario.eval(semaphore.available).flatMap { i =>
      if (i > 0) {
        scenario
      } else Scenario.expect(command("p").chat).flatMap(chat => Scenario.eval(chat.send("run /s - command"))) >> Scenario.done
    }
}

trait ProfitCalculator[F[_]] {
  def calculate(tickerName: String, account: AccountType): fs2.Stream[F, StockProfitMap]
}

object ProfitCalculatorInterpreter {
  def makeScenario[F[_]: TinkoffClient: TelegramClient: InMemoryAccountsStorage](implicit
    ae: MonadError[F, ProfitCalculatorError]
  ): ProfitCalculator[F] =
    new ProfitCalculator[F] {

      private def validateTickerNotEmpty(tickerName: String): F[Unit] = //todo may use RegExp instead
        if (tickerName.isEmpty) ae.raiseError(TelegramParameterExist(tickerName))
        else ae.unit

      private def validateTickerNoSpacesContain(tickerName: String): F[Unit] = //todo may use RegExp instead
        if (tickerName.contains(" ")) ae.raiseError(TelegramParameterCorrect(tickerName))
        else ae.unit

      private def validateTicker(tickerName: String): F[Unit] =
        OptionT(TinkoffInvestApi
          .marketInstrumentsInstance
          .searchByTicker(tickerName)
          .map (_.total)
          .map {
            case i if i > 0 => Some(i)
            case _ => None
          })
          .fold(ae.raiseError[Unit](TelegramParameterUsefulForMarketTicker(tickerName)))(_ => ae.unit).flatten

      override def calculate(tickerName: String, account: AccountType): fs2.Stream[F, StockProfitMap] = {
        val i = validateTickerNotEmpty(tickerName) *> validateTickerNoSpacesContain(tickerName) *> validateTicker(tickerName) *>
          account
            .id
            .map(TinkoffInvestLogic.stockProfit(_, Ticker(tickerName), allTime))
            .map(implicit i => new TinkoffService[StockProfitMap].run)

        fs2.Stream.eval(i).flatten
      }
    }
}
