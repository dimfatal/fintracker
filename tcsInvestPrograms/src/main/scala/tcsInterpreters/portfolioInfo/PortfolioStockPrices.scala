package tcsInterpreters.portfolioInfo

import cats.Functor
import cats.data.Ior
import cats.implicits._
import fs2.Stream
import tcs4sclient.api.methods.{ PortfolioApi, TradeHistoryApi }
import tcs4sclient.model.domain.market.Ticker
import tcs4sclient.model.domain.user.operations.Operation
import tcsInterpreters.portfolioInfo.PortfolioStockPrices._
import tcsInterpreters.portfolioInfo.validation._
import tcsInterpreters.{ AccountId, InMemoryAccountsStorage }

import java.time.OffsetDateTime

class PortfolioStockPrices[F[_]: InMemoryAccountsStorage](implicit operationsApi: TradeHistoryApi[F], portfolioApi: PortfolioApi[F]) {

  private implicit val stockValidatorEitherInterpreter: StockValidator[Either[StockValidation, *]] =
    StockValidatorInterpreter.stockValidator[Either[StockValidation, *], StockValidation](identity)

  def calculate(account: AccountId, ticker: Ticker)(implicit f: Functor[F]): Stream[F, String] =
    Stream
      .emit(account)
      .through(_.evalMap(a => portfolioApi.portfolio(a.id)))
      .through(_.map(portfolio => StockValidator.validate(ticker, portfolio)))
      .through(
        _.map(
          _.map(stocks =>
            Stream.eval(
              operationsApi
                .operations( //todo try refactor and solve multyMap situation split operations and portfolio call
                  OffsetDateTime.now.minusYears(20),
                  OffsetDateTime.now,
                  stocks.getFigiByTicker(ticker).id,
                  account.id
                )
                .map(op => getHoldPositions(op.operations))
                .map(operationsPriceAndDateToString)
            )
          )
        )
      )
      .through(_.map(_.fold(e => Stream.emit(e.errorMessage).covary[F], op => op)))
      .through(_.flatten)

}

object PortfolioStockPrices {

  def getHoldPositions(operations: List[Operation]): List[Operation] = {

    val successBuyOrSellOperations         = operations.filter(op => //todo make it syntax ops from Operations case class
      op.status == "Done" &&
        (op.operationType.contains("BuyCard") ||
          op.operationType.contains("Buy") ||
          op.operationType.contains("Sell"))
    )
    val multiplyOperationsAccordingWithQty = successBuyOrSellOperations.flatMap { operation =>
      List.fill(operation.quantity.get.toInt)(operation)
    }
    val buyPositions                       = multiplyOperationsAccordingWithQty
      .filter(op =>
        op.operationType.contains("Buy") || //todo replace string with case class
          op.operationType.contains("BuyCard")
      )
      .reverse //todo replace string with case class
    val sellPositions                      = multiplyOperationsAccordingWithQty.filter(_.operationType.contains("Sell")).reverse //todo replace string with case class

    val holdPositions = sellPositions
      .align(buyPositions)
      .map {
        case Ior.Both(_, _) => None
        case Ior.Right(a)   => Some(a)
        case Ior.Left(a)    => Some(a)
      }
      .filter(x => x.isDefined)
      .map(_.get)

    holdPositions
  }

  def operationsPriceAndDateToString(operations: List[Operation]): String =
    operations.map(op => s"${op.payment} -> ${op.date}").mkString("\n")
}
