package bot.tinkoff

import bot.inMemoryStorage.InMemoryAccountsStorage
import bot.{BotLogic, BotLogicInterpreter}
import cats.Functor
import cats.effect.Sync
import fs2.Stream
import org.http4s.client.Client
import tcs4sclient.api.client.{Http4sTinkoffClientBuilder, TinkoffClient}
import tcs4sclient.model.domain.market.{MarketInstrument, Ticker}
import tcs4sclient.model.domain.user.AccountType
import tcsInterpreters.AccountId
import tcsInterpreters.portfolioInfo.PeriodQuery

final case class AccountsMap(a: Map[AccountType, String])
final case class PositionsList(a: List[String])
final case class StockProfitMap(items: Map[MarketInstrument, String]) //todo should make it extend anyval ??
final case class StockPrices(a: String)

object TinkoffInvestPrograms {

  trait TinkoffInvestLogic[G] extends {
    def run[F[_]](F: BotLogic[F]): Stream[F, G]
  }

  object TinkoffInvestLogic {
    implicit def accounts: TinkoffInvestLogic[AccountsMap] = new TinkoffInvestLogic[AccountsMap] {
      override def run[F[_]](F: BotLogic[F]): Stream[F, AccountsMap] = F.accounts
    }

    implicit def positionsList(account: AccountId): TinkoffInvestLogic[PositionsList] =
      new TinkoffInvestLogic[PositionsList] {
        override def run[F[_]](F: BotLogic[F]): Stream[F, PositionsList] = F.portfolioPositions(account)
      }

    implicit def stockProfit(account: AccountId, ticker: Ticker, periodQuery: PeriodQuery): TinkoffInvestLogic[StockProfitMap] =
      new TinkoffInvestLogic[StockProfitMap] {
        override def run[F[_]](F: BotLogic[F]): Stream[F, StockProfitMap] = F.stockProfit(account, ticker, periodQuery)
      }

    implicit def stockPrices(account: AccountId, ticker: Ticker): TinkoffInvestLogic[StockPrices] =
      new TinkoffInvestLogic[StockPrices] {
        override def run[F[_]](F: BotLogic[F]): Stream[F, StockPrices] = F.stockPrices(account, ticker)
      }

    def apply[F: TinkoffInvestLogic]: TinkoffInvestLogic[F] = implicitly[TinkoffInvestLogic[F]]
  }

  class TinkoffService[G: TinkoffInvestLogic] {
    def run[F[_]: TinkoffClient: InMemoryAccountsStorage: Functor]: Stream[F, G] = TinkoffInvestLogic[G].run(BotLogicInterpreter[F])
  }
}
