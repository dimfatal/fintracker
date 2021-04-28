package tcsInterpreters

import java.time.OffsetDateTime

final case class AccountId(id: String) extends AnyVal
final case class PeriodQuery(from: OffsetDateTime, to: OffsetDateTime)

object DateTimePeriod {
  val allTime: PeriodQuery = PeriodQuery(OffsetDateTime.now.minusYears(20), OffsetDateTime.now)
}
