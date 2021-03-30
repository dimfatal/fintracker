package tcsInterpreters.portfolioInfo.validation

import cats.ApplicativeError
import cats.implicits._
import tcs4sclient.model.domain.Portfolio
import tcs4sclient.model.domain.market.{ Figi, Ticker }
import tcs4sclient.model.domain.user.portfolio.Position

case object StockDoesNotExistInUserPortfolio extends StockValidation {
  def errorMessage: String = "Stock ticker doesn't contain in user portfolio positions."
}

final case class Stocks(positions: List[Position]) { //todo refactor to syntax ops
  def getFigiByTicker(ticker: Ticker): Figi = Figi(
    positions.filter(_.ticker.contains(ticker.id)).head.figi
  )
}

sealed trait StockValidator[F[_]] {
  def hasTickerInStocks(ticker: Ticker, portfolio: Portfolio): F[Stocks]
}

object StockValidator {
  def apply[F[_]](implicit sv: StockValidator[F]): StockValidator[F] = sv

  def validate[F[_]: StockValidator, E](t: Ticker, p: Portfolio): F[Stocks] =
    StockValidator[F].hasTickerInStocks(t, p)
}

object StockValidatorInterpreter {
  def stockValidator[F[_], E](mkError: StockValidation => E)(implicit A: ApplicativeError[F, E]): StockValidator[F] =
    new StockValidator[F] {

      override def hasTickerInStocks(ticker: Ticker, portfolio: Portfolio): F[Stocks] =
        validateStocksUsingTicker(portfolio.positions, ticker).map(Stocks)
      // (Stocks.apply _).curried.pure[F] <*> validateStocksUsingTicker(stocks, ticker) <*> ticker.pure[F]

      private def validateStocksUsingTicker(stocks: List[Position], ticker: Ticker): F[List[Position]] =
        stocks.find(_.ticker.contains(ticker.id)) match {
          case Some(_) => stocks.pure[F]
          case _       => A.raiseError(mkError(StockDoesNotExistInUserPortfolio))
        }
      //todo add validation in case invalid ticker ????

    }
}
