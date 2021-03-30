package tcs.api.models
package market

import org.http4s.Method
import tcs.api.client.HttpClient
import tcs.api.models.market.marketPath._
import tcs.domain.model.MarketInstruments
import tcs.domain.model.market._

final class MarketInstrumentsApi[F[_]: HttpClient](private val instrumentId: InstrumentId) extends TcsApi {

  override def path: String               = "/market"
  def getInstrument: F[MarketInstruments] =
    instrumentId match {
      case Ticker(id) => getInstrumentByTicker(id)
      case Figi(id)   => getInstrumentByFigi(id)
    }

  private def getInstrumentByFigi(figi: String): F[MarketInstruments]     =
    implicitly[HttpClient[F]].execute(Method.GET, byfigi(path), Map("figi" -> figi))

  private def getInstrumentByTicker(ticker: String): F[MarketInstruments] =
    implicitly[HttpClient[F]].execute(Method.GET, byTicker(path), Map("ticker" -> ticker))
}

object MarketInstrumentsApi {
  def apply[F[_]: HttpClient](instrumentId: InstrumentId): MarketInstrumentsApi[F] = new MarketInstrumentsApi(instrumentId)
}

object marketPath {
  def byfigi: String => String   = path => s"$path/search/by-figi"
  def byTicker: String => String = path => s"$path/search/by-ticker"
}

//object MarketApiOps {
//  class Market[F[_] : TcsClient] extends MarketInstrumentsApi[F] {
//    override def getInstrumentByFigi(figi : String) : Stream[F, MarketInstrument] =
//      Stream(new MarketInfo().getInstrumentByFigi(figi))
//  }
//}
