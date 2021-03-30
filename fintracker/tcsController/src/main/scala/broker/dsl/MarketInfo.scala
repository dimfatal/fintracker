package broker.dsl
import fs2.Stream
import tcs.domain.model.MarketInstruments

trait MarketInfo[F[_]] {
  def instrument: Stream[F, MarketInstruments]

}



