package broker.interpreters


import broker.dsl.MarketInfo
import cats.data.ReaderT
import cats.{Applicative, Functor}
import fs2.Stream
import org.http4s.Method
import tcs.api.client.HttpClient
import tcs.api.models.market.MarketInstrumentsApi
import tcs.api.models.user.AccountApi
import tcs.domain.model._
import tcs.domain.model.market.InstrumentId
import tcs.domain.model.user.{Account, AccountType, Accounts}

import java.time.OffsetDateTime

trait BrokerAccountOperations[F[_]] {
  def operations(from: OffsetDateTime, to: OffsetDateTime, figi: String): Stream[F, Operations]
  def positions: Stream[F, Positions]
}//todo think how to implement interpreter look how it works in structure functional what about ops package could it possible do smth like



object BrokerAccountOperations {


  implicit class BrokerAccount[F[_]: AccountApi[F]](accountType: AccountType) extends BrokerAccountOperations[F] {


    val account =

    override def operations(from: OffsetDateTime, to: OffsetDateTime, figi: String): Stream[F, Operations] = ???

    override def positions: Stream[F, Positions] = ???
  }

  implicit class MarketInstrumentsInfo[F[_]: HttpClient: Applicative](id: InstrumentId) extends MarketInfo[F] {

    override def instrument: Stream[F, MarketInstruments] =
      Stream.eval(MarketInstrumentsApi(id).getInstrument) //todo make it implicit base on InstrumentId?
  }

}

//
//object Interpreters {
//  implicit def account[F[_]]: AccountApi[F] = new AccountApi[F] {
//
//    import tcs.domain.model.user.AccountsResponseDecoder.accountsDecoder
//
//    override def accounts: ReaderT[F, HttpClient[F], Accounts] = ReaderT { client =>
//      client.execute(Method.GET, path, Map.empty)
//    }
//
//    val path: String = "/user/accounts"
//  }
//
//}
