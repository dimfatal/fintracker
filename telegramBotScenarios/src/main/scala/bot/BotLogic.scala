package bot

import bot.inMemoryStorage.InMemoryAccountsStorage
import bot.tinkoff.{AccountsMap, PositionsList, StockPrices, StockProfitMap}
import canoe.api.Scenario
import cats.effect.Sync
import cats.implicits._
import fs2.Stream
import tcs4sclient.api.client.TinkoffClient
import tcs4sclient.model.domain.market.{Figi, Ticker}
import tcsInterpreters.AccountId
import tcsInterpreters.TinkoffInvest.{dsl, marketInfo}
import tcsInterpreters.portfolioInfo.DateTimePeriod.allTime
import tcsInterpreters.portfolioInfo.PeriodQuery

trait BotLogic[F[_]] {
  def start: Scenario[F, Unit]                         // todo make it
  def accounts: Stream[F, AccountsMap]
  def stockProfit(account: AccountId, ticker: Ticker, periodQuery: PeriodQuery = allTime): Stream[F, StockProfitMap]
  def portfolioPositions(account: AccountId): Stream[F, PositionsList]
  def stockPrices(account: AccountId, ticker: Ticker): Stream[F, StockPrices]
  def findMarketInstrumentsByTicker: Scenario[F, Unit] // todo make it
}

object BotLogicInterpreter {
  def apply[F[_]: Sync: TinkoffClient: InMemoryAccountsStorage]: BotLogic[F] = new BotLogic[F] {

    import fs2.Stream
    override def start: Scenario[F, Unit] = ???

    override def accounts: Stream[F, AccountsMap] =
      dsl.accountsMap.map(AccountsMap)

    override def stockProfit(account: AccountId, ticker: Ticker, periodQuery: PeriodQuery = allTime): Stream[F, StockProfitMap] =
      marketInfo
        .searchInstrumentByTicker(ticker)
        .map(_.instruments)
        .map(
          _.map(instrument =>
            dsl
              .profitFromPeriod(account, Figi(instrument.figi), allTime)
              .map(profit => instrument -> profit)
          ).sequence
            .map(_.toMap)
        )
        .flatten
        .map(StockProfitMap)

    override def portfolioPositions(account: AccountId): Stream[F, PositionsList] =
      dsl.positionsInfo(account).map(PositionsList)

    override def stockPrices(account: AccountId, ticker: Ticker): Stream[F, StockPrices] =
      dsl.portfolioStockPrices(account, ticker).map(StockPrices)

    override def findMarketInstrumentsByTicker: Scenario[F, Unit] = ???
  }
}
