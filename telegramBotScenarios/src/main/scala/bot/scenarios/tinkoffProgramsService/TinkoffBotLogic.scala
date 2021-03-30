package bot.scenarios.tinkoffProgramsService

import canoe.api.Scenario
import cats.effect.Sync
import tcs4sclient.api.client.TinkoffClient
import tcs4sclient.model.domain.market.{ Figi, Ticker }
import tcs4sclient.model.domain.user.AccountType
import tcsInterpreters.InMemoryAccountsStorage
import tcsInterpreters.TinkoffInvest.{ dsl, marketInfo }
import tcsInterpreters.portfolioInfo.DateTimePeriod.allTime
import tcsInterpreters.portfolioInfo.PeriodQuery
import fs2.Stream

trait TinkoffBotLogic[F[_]] {
  def start: Scenario[F, Unit]                         // todo make it
  def accounts: Stream[F, AccountsMap]
  def stockProfit(account: AccountType, ticker: Ticker, periodQuery: PeriodQuery = allTime): Stream[F, StockProfitMap]
  def portfolioPositions(account: AccountType): Stream[F, PositionsList]
  def stockPrices(account: AccountType, ticker: Ticker): Stream[F, StockPrices]
  def findMarketInstrumentsByTicker: Scenario[F, Unit] // todo make it
}

object TinkoffBotLogicInterpreter {
  def apply[F[_]: Sync: TinkoffClient: InMemoryAccountsStorage]: TinkoffBotLogic[F] = new TinkoffBotLogic[F] {

    import cats.implicits._
    import fs2.Stream
    override def start: Scenario[F, Unit] = ???

    override def accounts: Stream[F, AccountsMap] =
      dsl.accountsMap.map(AccountsMap)

    override def stockProfit(account: AccountType, ticker: Ticker, periodQuery: PeriodQuery = allTime): Stream[F, StockProfitMap] =
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

    override def portfolioPositions(account: AccountType): Stream[F, PositionsList] =
      dsl.positionsInfo(account).map(PositionsList)

    override def stockPrices(account: AccountType, ticker: Ticker): Stream[F, StockPrices] =
      dsl.portfolioStockPrices(account, ticker).map(StockPrices)

    override def findMarketInstrumentsByTicker: Scenario[F, Unit] = ???
  }
}
