package bot.scenarios.tinkoffProgramsService

import bot.memoryStorage.InMemoryAccountsStorage
import cats.effect.Sync
import fs2.Stream
import org.http4s.client.Client
import tcs4sclient.api.client.{ Http4sTinkoffClientBuilder, TinkoffClient }
import tcs4sclient.model.domain.market.{ MarketInstrument, Ticker }
import tcs4sclient.model.domain.user.AccountType
import tcsInterpreters.AccountId
import tcsInterpreters.portfolioInfo.PeriodQuery

final case class AccountsMap(a: Map[AccountType, String])
final case class PositionsList(a: List[String])
final case class StockProfitMap(a: Map[MarketInstrument, String]) //todo should make it extend anyval ??
final case class StockPrices(a: String)

object ScenarioService {

  trait TinkoffServiceLogic[G] extends {
    def run[F[_]](F: TinkoffBotLogic[F]): Stream[F, G]
  }

  object TinkoffServiceLogic {
    implicit def accounts: TinkoffServiceLogic[AccountsMap] = new TinkoffServiceLogic[AccountsMap] {
      override def run[F[_]](F: TinkoffBotLogic[F]): Stream[F, AccountsMap] = F.accounts
    }

    implicit def positionsList(account: AccountId): TinkoffServiceLogic[PositionsList] =
      new TinkoffServiceLogic[PositionsList] {
        override def run[F[_]](F: TinkoffBotLogic[F]): Stream[F, PositionsList] = F.portfolioPositions(account)
      }

    implicit def stockProfit(account: AccountId, ticker: Ticker, periodQuery: PeriodQuery): TinkoffServiceLogic[StockProfitMap] =
      new TinkoffServiceLogic[StockProfitMap] {
        override def run[F[_]](F: TinkoffBotLogic[F]): Stream[F, StockProfitMap] = F.stockProfit(account, ticker, periodQuery)
      }

    implicit def stockPrices(account: AccountId, ticker: Ticker): TinkoffServiceLogic[StockPrices] =
      new TinkoffServiceLogic[StockPrices] {
        override def run[F[_]](F: TinkoffBotLogic[F]): Stream[F, StockPrices] = F.stockPrices(account, ticker)
      }

    def apply[F: TinkoffServiceLogic]: TinkoffServiceLogic[F] = implicitly[TinkoffServiceLogic[F]]
  }

  class TinkoffService[G: TinkoffServiceLogic] {
    def run[F[_]: Client: Sync: InMemoryAccountsStorage](token: String): Stream[F, G] = {
      implicit val c: TinkoffClient[F] = Http4sTinkoffClientBuilder.fromHttp4sClient[F](token)(implicitly[Client[F]])
      TinkoffServiceLogic[G].run(TinkoffBotLogicInterpreter[F])
    }
  }
}
