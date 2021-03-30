package tcs4sclient.api.methods

import org.http4s.Method
import tcs4sclient.api.client.TinkoffClient
import tcs4sclient.model.domain.{ MarketInstruments, Portfolio, TradeHistory }

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

trait TradeHistoryApi[F[_]] {
  def operations(from: OffsetDateTime, to: OffsetDateTime, figi: String, brokerAccountId: String): F[TradeHistory]
}

trait PortfolioApi[F[_]] {
  def portfolio(brokerAccountId: String): F[Portfolio]
}

trait MarketInstrumentsApi[F[_]] {
  // def searchByFigi[A: EntityDecoder[F, MarketInstrument]](figi: String): F[MarketInstrument]
  def searchByTicker(ticker: String): F[MarketInstruments]
}

object TinkoffInvestApi {

  implicit def tradeHistoryInstance[F[_]: TinkoffClient]: TradeHistoryApi[F] = new TradeHistoryApi[F] {
    import tcs4sclient.model.domain.user.operations.OperationDecoder.decoderOperations

    val path: String = "/operations"

    override def operations(from: OffsetDateTime, to: OffsetDateTime, figi: String, brokerAccountId: String): F[TradeHistory] = {

      val query = Map(
        "from"            -> from.format(DateTimeFormatter.ISO_DATE_TIME),
        "to"              -> to.format(DateTimeFormatter.ISO_DATE_TIME),
        "figi"            -> figi,
        "brokerAccountId" -> brokerAccountId
      )
      implicitly[TinkoffClient[F]].execute(Method.GET, path, query)
    }

  }

  implicit def positionsInstance[F[_]](implicit client: TinkoffClient[F]): PortfolioApi[F] = new PortfolioApi[F] {

    override def portfolio(brokerAccountId: String): F[Portfolio] = {
      import tcs4sclient.model.domain.user.portfolio.Position.decoderPositions

      val path: String = "/portfolio"

      client.execute(Method.GET, path, Map("brokerAccountId" -> brokerAccountId))
    }
  }

  implicit def marketInstrumentsInstance[F[_]](implicit client: TinkoffClient[F]): MarketInstrumentsApi[F] = new MarketInstrumentsApi[F] {
    import marketPath._
    import tcs4sclient.model.domain.market.MarketInstrument.decoderMarketInstruments

    val path: String = "/market"

    override def searchByTicker(ticker: String): F[MarketInstruments] =
      client.execute(Method.GET, byTicker(path), Map("ticker" -> ticker))

    //    override def searchByFigi[A: EntityDecoder[F, MarketInstrument]](figi: String): F[MarketInstrument] = {
    //      import tcs.domain.model.market.MarketInstrument.decoderMarketInstrument
    //
    //      client.execute(Method.GET, byfigi(path), Map("figi" -> figi))
    //
    //    }
  }

  object marketPath {
    def byfigi: String => String   = path => s"$path/search/by-figi"
    def byTicker: String => String = path => s"$path/search/by-ticker"
  }

}
