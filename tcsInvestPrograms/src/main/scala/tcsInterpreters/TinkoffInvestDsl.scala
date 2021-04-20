package tcsInterpreters

import cats.Functor
import fs2._
import tcs4sclient.api.client._
import tcs4sclient.api.methods.{ MarketInstrumentsApi, TinkoffInvestApi }
import tcs4sclient.model.domain.MarketInstruments
import tcs4sclient.model.domain.market.{ Figi, Ticker }
import tcs4sclient.model.domain.user.AccountType
import tcsInterpreters.portfolioInfo.{ AccountsMap, PeriodQuery, PortfolioStockPrices, PortfolioStockProfit }

trait TinkoffInvestPrograms[F[_], S[_]] {
  def accountsMap: S[Map[AccountType, String]]
  def positionsInfo(account: AccountId): S[List[String]]
  def portfolioStockPrices(account: AccountId, ticker: Ticker): S[String]
  def profitFromPeriod(account: AccountId, figi: Figi, periodQuery: PeriodQuery): S[String]
}

trait StreamingTinkoffInvest[F[_]] extends TinkoffInvestPrograms[F, Stream[F, *]]

trait TinkoffMarketInfo[F[_]] {
  def searchInstrumentByTicker(ticker: Ticker): Stream[F, MarketInstruments]
}

object TinkoffInvest {

  def dsl[F[_]: TinkoffClient: Functor]: StreamingTinkoffInvest[F] =
    new StreamingTinkoffInvest[F] {

      import tcs4sclient.api.methods.TinkoffInvestApi._

      override def positionsInfo(account: AccountId): Stream[F, List[String]] =
        Stream.eval(new PositionsInfoString[F].display(account))

      override def portfolioStockPrices(account: AccountId, ticker: Ticker): Stream[F, String] =
        new PortfolioStockPrices[F].calculate(account, ticker)

      override def profitFromPeriod(account: AccountId, figi: Figi, periodQuery: PeriodQuery): Stream[F, String] =
        new PortfolioStockProfit[F].calculate(account, figi, periodQuery)

      override def accountsMap: Stream[F, Map[AccountType, String]] = AccountsMap[F]().make()
    }

  implicit def marketInfo[F[_]: TinkoffClient]: TinkoffMarketInfo[F] = new TinkoffMarketInfo[F] {

    val marketInstrumentsApi: MarketInstrumentsApi[F] =
      TinkoffInvestApi.marketInstrumentsInstance //todo should it used like syntax ops type class for ticker or figi

    override def searchInstrumentByTicker(ticker: Ticker): Stream[F, MarketInstruments] =
      Stream.emit(ticker.id).evalMap(marketInstrumentsApi.searchByTicker)
  }

}
