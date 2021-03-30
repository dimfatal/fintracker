package tcsInterpreters.portfolioInfo

import cats.implicits._
import tcs4sclient.api.methods.{ PortfolioApi, TradeHistoryApi }
import tcs4sclient.model.domain.market.Figi
import tcs4sclient.model.domain.user.operations.Operation
import tcs4sclient.model.domain.user.portfolio.Position
import tcsInterpreters.portfolioInfo.validation.{ StockValidation, TradeHistoryExistenceValidator, TradeHistoryExistenceValidatorInterpreter }
import tcsInterpreters.{ AccountId, InMemoryAccountsStorage }

import java.time.OffsetDateTime

case class PeriodQuery(from: OffsetDateTime, to: OffsetDateTime)

object DateTimePeriod {
  val allTime: PeriodQuery = PeriodQuery(OffsetDateTime.now.minusYears(20), OffsetDateTime.now)
} //todo move it from here

class PortfolioStockProfit[F[_]: InMemoryAccountsStorage](implicit tradeHistoryApi: TradeHistoryApi[F], portfolioApi: PortfolioApi[F]) {

  private implicit val tradeHistoryExistenceValidator: TradeHistoryExistenceValidator[Either[StockValidation, *]] =
    TradeHistoryExistenceValidatorInterpreter.operationHistoryExistenceValidator[Either[StockValidation, *], StockValidation](identity)

  def calculate(account: AccountId, figi: Figi, periodQuery: PeriodQuery): fs2.Stream[F, String] = {

    import periodQuery._

    val history = fs2
      .Stream
      .emit(account.id)
      .through(_.evalMap(tradeHistoryApi.operations(from, to, figi.id, _)))
      .through(_.map(op => TradeHistoryExistenceValidator.validate(op)))
      .through(_.map(_.map(_.operations.filter(_.status == "Done"))))
      .through(summarizeHistoryProfit)

    val current = fs2
      .Stream
      .emit(account.id)
      .through(_.evalMap(portfolioApi.portfolio))
      .through(_.map(_.positions.find(_.figi.contains(figi.id))))
      .through(summarizePositionProfit)

    for {
      h <- history
      c <- current
    } yield h
      .map(_ + c)
      .fold(
        e => e.errorMessage, //todo review and refactor it
        s => s.toString()
      )

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

  private def summarizeHistoryProfit: fs2.Pipe[F, Either[StockValidation, List[Operation]], Either[StockValidation, BigDecimal]] = _.map(
    _.map(op => buyOperationsPrices(op).sum + sellOperationsPrices(op).sum + commissions(op).sum)
  )

  private def summarizePositionProfit: fs2.Pipe[F, Option[Position], BigDecimal] = _.map {
    case Some(p) =>
      val stockDeposit  = p.averagePositionPrice.map(_.value).map(_ * p.lots)
      val expectedYield = p.expectedYield.map(_.value)
      (stockDeposit |+| expectedYield).getOrElse(0)
    case None    => 0
  }
}
