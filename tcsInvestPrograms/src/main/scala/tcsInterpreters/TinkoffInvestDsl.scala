package tcsInterpreters

import cats.effect.Sync
import fs2._
import tcs4sclient.api.client._
import tcs4sclient.api.methods.{ MarketInstrumentsApi, TinkoffInvestApi }
import tcs4sclient.model.domain.MarketInstruments
import tcs4sclient.model.domain.market.{ Figi, Ticker }
import tcs4sclient.model.domain.user.AccountType
import tcsInterpreters.portfolioInfo.{ AccountsMap, PeriodQuery, PortfolioStockPrices, PortfolioStockProfit }

trait TinkoffInvestPrograms[F[_], S[_]] {
  def accountsMap: S[Map[AccountType, String]]
  def positionsInfo(accountType: AccountType)(implicit store: InMemoryAccountsStorage[F]): S[List[String]]
  def portfolioStockPrices(accountType: AccountType, ticker: Ticker)(implicit store: InMemoryAccountsStorage[F]): S[String]
  def profitFromPeriod(accountType: AccountType, figi: Figi, periodQuery: PeriodQuery)(implicit store: InMemoryAccountsStorage[F]): S[String]
}

trait StreamingTinkoffInvest[F[_]] extends TinkoffInvestPrograms[F, Stream[F, *]]

trait TinkoffMarketInfo[F[_]] {
  def searchInstrumentByTicker(ticker: Ticker): Stream[F, MarketInstruments]
}

object TinkoffInvest {

  def dsl[F[_]: TinkoffClient: Sync]: StreamingTinkoffInvest[F] =
    new StreamingTinkoffInvest[F] {

      import AccountTypeStorageSyntax._
      import tcs4sclient.api.methods.TinkoffInvestApi._

      override def positionsInfo(account: AccountType)(implicit store: InMemoryAccountsStorage[F]): Stream[F, List[String]] =
        Stream
          .eval(account.id)
          .evalMap(new PositionsInfoString[F].display)

      override def portfolioStockPrices(account: AccountType, ticker: Ticker)(implicit store: InMemoryAccountsStorage[F]): Stream[F, String] =
        Stream
          .eval(account.id)
          .map(new PortfolioStockPrices[F].calculate(_, ticker))
          .flatten

      override def profitFromPeriod(account: AccountType, figi: Figi, periodQuery: PeriodQuery)(implicit
        store: InMemoryAccountsStorage[F]
      ): Stream[F, String] =
        Stream
          .eval(account.id)
          .map(new PortfolioStockProfit[F].calculate(_, figi, periodQuery))
          .flatten

      override def accountsMap: Stream[F, Map[AccountType, String]] = AccountsMap[F]().make()

    }

  implicit def marketInfo[F[_]: TinkoffClient]: TinkoffMarketInfo[F] = new TinkoffMarketInfo[F] {

    val marketInstrumentsApi: MarketInstrumentsApi[F] =
      TinkoffInvestApi.marketInstrumentsInstance //todo should it used like syntax ops type class for ticker or figi

    override def searchInstrumentByTicker(ticker: Ticker): Stream[F, MarketInstruments] =
      Stream.emit(ticker.id).evalMap(marketInstrumentsApi.searchByTicker)
  }

}
