package tcsInterpreters

import cats.Monad
import fs2._
import tcs4sclient.api.client._
import tcs4sclient.api.methods.{ MarketInstrumentsApi, TinkoffInvestApi }
import tcs4sclient.model.domain.MarketInstruments
import tcs4sclient.model.domain.market.Ticker

trait TinkoffInvestPrograms[F[_]] {
  def accountsMap: AccountsMap[F]
  def positionsInfo: PositionsInfoString[F]
  def historyProfit: StockProfit[F]
}

trait TinkoffMarketInfo[F[_]] {
  def searchInstrumentsByTicker(ticker: Ticker): Stream[F, MarketInstruments]
}

object TinkoffInvest {

  def dsl[F[_]: TinkoffClient: Monad]: TinkoffInvestPrograms[F] =
    new TinkoffInvestPrograms[F] {

      import tcs4sclient.api.methods.TinkoffInvestApi._

      override def accountsMap: AccountsMap[F] = new AccountsMap[F]

      override def positionsInfo: PositionsInfoString[F] = new PositionsInfoString[F]

      override def historyProfit: StockProfit[F] = new StockProfit[F]
    }

  implicit def marketInfo[F[_]: TinkoffClient]: TinkoffMarketInfo[F] = new TinkoffMarketInfo[F] {

    val marketInstrumentsApi: MarketInstrumentsApi[F] =
      TinkoffInvestApi.marketInstrumentsInstance //todo should it used like syntax ops type class for ticker or figi

    override def searchInstrumentsByTicker(ticker: Ticker): Stream[F, MarketInstruments] =
      Stream.emit(ticker.name).evalMap(marketInstrumentsApi.searchByTicker)
  }

}
