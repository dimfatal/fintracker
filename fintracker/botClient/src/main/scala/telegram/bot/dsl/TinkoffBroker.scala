package telegram.bot.dsl

import broker.dsl.{InstrumentInfo, TradeHistory, UserAccount, UserAccountOperations, UserPortfolio}
import broker.interpreters.BrokerApi
import broker.interpreters.BrokerApi.UserAccountOperations.dsl
import cats.Applicative
import cats.effect.IO
import fs2.{Pipe, Stream}
import tcs.api.client.HttpClient
import tcs.domain.model.user.{AccountType, Tinkoff}
import tcs.domain.model.{Operations, Positions}

import java.time.OffsetDateTime

trait TinkoffBroker[F[_]] {
  def positions: Stream[F, Positions]
  def tradeHistory(from: OffsetDateTime, to: OffsetDateTime, figi: String): Stream[F, Operations]
  def positionsProfit: Stream[F, List[String]]
}

trait Broker {
  import broker.interpreters.TcsBroker.UserAccountOperations

  implicit def tcsClient: HttpClient[IO]

  def tinkoffBroker: TinkoffBroker[IO] = new TinkoffBroker[IO] {

    override def positions: Stream[IO, Positions] = Tinkoff.positions

    override def tradeHistory(from: OffsetDateTime, to: OffsetDateTime, figi: String): Stream[IO, Operations] =
      Tinkoff.operations(from, to, figi)

    def positionsProfit: Stream[IO, List[String]] = Tinkoff.positions.through(displayProfit)

    private def displayProfit: Pipe[IO, Positions, List[String]] = in =>
      in.flatMap(x =>
        Stream.emit(x.positions.map { pos =>
          s"${pos.name} - " +
            s"${pos.averagePositionPrice.map(_.value * pos.lots + pos.expectedYield.map(_.value).get).getOrElse(0)} " +
            s"${pos.averagePositionPrice.map(_.currency).getOrElse("Error")} -> " +
            s"${pos.expectedYield.map(_.value).getOrElse(BigDecimal.defaultMathContext)} " +
            s"${pos.averagePositionPrice.map(_.currency).getOrElse("Error")} \n "
        })
      )
  }
}

trait TinkoffBroker1[F[_]] {
  def positions1: Stream[F, Positions]
  def tradeHistory1(from: OffsetDateTime, to: OffsetDateTime, figi: String): Stream[F, Operations]
  def positionsProfit1: Stream[F, List[String]]
}


trait ProgramBroker1[F[_]]{
  def tinkoffBroker : TinkoffBroker1[F]
}


object ProgramBroker1 {
  object Broker1 {
    def dsl[F[_]](implicit U : UserAccountOperations[F]): ProgramBroker1[F] =  new ProgramBroker1[F]{

        import U._

        override val tinkoffBroker: TinkoffBroker1[F] = new TinkoffBroker1[F] {

          override def positions1: Stream[F, Positions] = positions //todo

          override def tradeHistory1(from: OffsetDateTime, to: OffsetDateTime, figi: String): Stream[F, Operations] =
            operations(from, to, figi) // todo

          def positionsProfit1: Stream[F, List[String]] = positions.through(displayProfit1) // todo

          private def displayProfit1: Pipe[F, Positions, List[String]] = in =>
            in.flatMap(x =>
              Stream.emit(x.positions.map { pos =>
                s"${pos.name} - " +
                  s"${pos.averagePositionPrice.map(_.value * pos.lots + pos.expectedYield.map(_.value).get).getOrElse(0)} " +
                  s"${pos.averagePositionPrice.map(_.currency).getOrElse("Error")} -> " +
                  s"${pos.expectedYield.map(_.value).getOrElse(BigDecimal.defaultMathContext)} " +
                  s"${pos.averagePositionPrice.map(_.currency).getOrElse("Error")} \n "
              })
            )
        }

    }
  }
}
