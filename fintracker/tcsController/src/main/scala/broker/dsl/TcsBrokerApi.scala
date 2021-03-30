package broker.dsl

import java.time.OffsetDateTime
import fs2.Stream
import tcs.domain.model.{Operations, Positions}

trait TcsBrokerApi[F[_]] {
  def operations(from: OffsetDateTime, to: OffsetDateTime, figi: String): Stream[F, Operations]
  def positions: Stream[F, Positions]
}