package broker.dsl

import tcs.domain.model.{MarketInstruments, Operations, Positions}
import fs2.Stream
import tcs.domain.model.market.InstrumentId
import tcs.domain.model.user.{AccountType, Accounts}

import java.time.OffsetDateTime

trait InstrumentInfo[F[_]] {
  def instrumentInfo(id: InstrumentId): Stream[F, MarketInstruments]
}

trait TradeHistory[F[_]] {
  def operations(from: OffsetDateTime, to: OffsetDateTime, figi: String): Stream[F, Operations]
}

trait UserPortfolio[F[_]] {
  def positions: Stream[F, Positions]
}

trait UserAccount[F[_]]  {
  val accounts : Stream[F, Accounts]

}

trait UserAccountOperations[F[_]] extends UserAccount[F] with TradeHistory[F] with UserPortfolio[F] {
 // implicit val accountType : AccountType
}


