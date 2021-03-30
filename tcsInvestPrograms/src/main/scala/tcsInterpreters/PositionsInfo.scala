package tcsInterpreters

import cats.Functor
import cats.implicits._
import tcs4sclient.api.methods.PortfolioApi
import tcs4sclient.model.domain.Portfolio

trait PositionsInfo[F[_]] {
  def display(account: AccountId): F[List[String]]
}

class PositionsInfoString[F[_]: Functor](implicit portfolioApi: PortfolioApi[F]) extends PositionsInfo[F] {

  def display(account: AccountId): F[List[String]] =
    portfolioApi
      .portfolio(account.id)
      .map(positionsToString)

  private def positionsToString: Portfolio => List[String] = in =>
    in.positions.map { pos =>
      s"${pos.name}/${pos.ticker.getOrElse("")} /  - " +
        s"${pos.averagePositionPrice.map(_.value * pos.lots + pos.expectedYield.map(_.value).get).getOrElse(0)} " +
        s"${pos.averagePositionPrice.map(_.currency).getOrElse("Error")} -> " +
        s"${pos.expectedYield.map(_.value).getOrElse(BigDecimal.defaultMathContext)} " +
        s"${pos.averagePositionPrice.map(_.currency).getOrElse("Error")} \n "
    }
}
