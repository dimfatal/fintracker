package tcsInterpreters

import cats.Monad
import cats.implicits._
import tcs4sclient.api.methods.{MarketInstrumentsApi, PortfolioApi, TradeHistoryApi}
import tcs4sclient.model.domain.market.{MarketInstrument, Ticker}
import tcs4sclient.model.domain.user.operations.Operation
import tcs4sclient.model.domain.user.portfolio.Position

class StockProfit[F[_]: Monad](implicit tradeHistoryApi: TradeHistoryApi[F], portfolioApi: PortfolioApi[F], market: MarketInstrumentsApi[F]) {

  def calculate(account: AccountId, ticker: Ticker): F[Map[MarketInstrument, BigDecimal]] = {
    import DateTimePeriod.allTime._

    market
      .searchByTicker(ticker.name)
      .map(_.instruments)
      .map { marketInstruments =>

        val tradeHistories = marketInstruments.map(item =>
          tradeHistoryApi.operations(from, to, item.figi, account.id)
            .map(_.operations.filter(_.status == "Done"))
            .map(op =>
              buyOperationsPrices(op).sum + sellOperationsPrices(op).sum + commissions(op).sum
            )
            .map(historyProfit => (item, historyProfit)))
          .sequence
          .map(_.toMap)

        for {
          stockWithHistoryPriceProfit <- tradeHistories
          stockWithCurrentPriceProfit <- stockWithHistoryPriceProfit.keys.toList.map(item =>
            portfolioApi.portfolio(account.id)
              .map(_.positions.filter(_.figi == item.figi))
              .map(_
                .map(profitForPortfolioPosition)
                .map(currentProfit => (item, currentProfit))
              )
          )
            .sequence
            .map(_.flatten.toMap)
        } yield stockWithHistoryPriceProfit.map { item =>
          item.copy(item._1, (Option(item._2) |+| stockWithCurrentPriceProfit.get(item._1)).get)
        }
      }
      .flatten
  }

  private def profitForPortfolioPosition(p: Position) = {
    val stockDeposit  = p.averagePositionPrice.map(_.value).map(_ * p.lots)
    val expectedYield = p.expectedYield.map(_.value)
    (stockDeposit |+| expectedYield).getOrElse(BigDecimal.decimal(0))
  }
  private def buyOperationsPrices: List[Operation] => List[BigDecimal] = _.filter(operation =>
    (operation.operationType == "Buy" || operation.operationType == "BuyCard") &&
      operation.status == "Done"
  )
    .map(_.payment)

  private def sellOperationsPrices: List[Operation] => List[BigDecimal] = _.filter(operation =>
    operation.operationType.contains("Sell") &&
      operation.status == "Done"
  )
    .map(_.payment)

  private def commissions: List[Operation] => List[BigDecimal] = _.filter(operation => operation.status == "Done")
    .flatMap(_.commission.map(_.value))
}
