package tcs.broker.tracker

import broker.interpreters.TcsBroker.UserAccountOperations
import canoe.api.{Bot, Scenario, TelegramClient, chatApi}
import canoe.syntax.{command, text, textContent}
import cats.Applicative
import cats.effect.{ConcurrentEffect, ExitCode, IO, IOApp, Sync}
import fs2.{Pipe, Stream}
import tcs.api.client.HttpClient
import tcs.domain.model.Operations
import tcs.domain.model.market.{Figi, InstrumentId, Ticker}
import tcs.domain.model.user.Tinkoff
import tcs.domain.model.user.operations.Operation
import tcs.domain.model.user.portfolio.Position

import java.time.OffsetDateTime

object CheckProfit extends IOApp {

  val tgToken: String = "1498629531:AAFzR4eASsMmk0ia8h3j7QWJbVAOqL4585A"

  override def run(args: List[String]): IO[ExitCode] =
    Stream
      .resource(TelegramClient.global[IO](tgToken))
      .flatMap { implicit client =>
        println("start")
        Bot.polling[IO].follow(/*findProfit*/)

      }
      .compile
      .drain
      .as(ExitCode.Success)

  def askTcs[F[_]: TelegramClient: Sync]: Scenario[F, Unit] =
    for {
      msg      <- Scenario.expect(command("o"))
      _        <- Scenario.eval(msg.chat.send("нужен ticker инструмента"))
      tickerId <- Scenario.expect(text)
      sum       = CountOperationsPrices[IO](Ticker(tickerId)).compile.toList.unsafeRunSync()
      _        <- Scenario.eval(msg.chat.send(textContent("result")))
      _        <- Scenario.eval(msg.chat.send(textContent(sum.toString())))
    } yield ()

//  def findProfit[F[_]: TelegramClient: Sync]: Scenario[F, Unit] =
//    for {
//      msg      <- Scenario.expect(command("p"))
//      res = tinkoffClientRunner[IO]().compile.toVector.unsafeRunSync()
//      _        <- Scenario.eval(msg.chat.send(textContent(res.flatten.mkString("\n"))))
//    } yield ()

//  def tinkoffClientRunner[F[_]: ConcurrentEffect: Applicative]()  = {
//    Stream
//      .resource(TcsClient.global[F](tcsToken))
//      .flatMap { implicit client =>
//        for {
//          res <- PositionsOper.positionsProfit[F]
//          s = println(res)
//        } yield res
//      }
//  }
  val tcsToken        = "t.dZIHJ-nk1gzpK7L5d8lrAX-uYQxvRm5Ppsum022OeDUTWO6_YLnKKXHkj2n8nwoTqtpJLLL79QuMhXfrla6v8w"
  def CountOperationsPrices[F[_]: ConcurrentEffect: Applicative](idInstrument: Ticker): Stream[F, BigDecimal ] =
    Stream
      .resource(HttpClient.global[F](tcsToken))
      .flatMap { implicit client =>
        for {
          operations <- operationsAllTime[F](idInstrument).map(_.operations) //"BBG003PHHZT1")
          s = println(buyOperationsPrices(operations))
        } yield sellOperationsPrices(operations).sum - buyOperationsPrices(operations).sum +  commissions(operations).sum
      }

  def operationsPerWeek[F[_]: Applicative: Sync](id: InstrumentId)(implicit tcsClient: HttpClient[F]): Stream[F, Operations] =
    operationsByInstrumentId(OffsetDateTime.now.minusWeeks(1), OffsetDateTime.now, id) //todo use marketInstrument object for syntax

  def operationsAllTime[F[_]: Applicative: Sync](id: InstrumentId)(implicit tcsClient: HttpClient[F]): Stream[F, Operations] =
    operationsByInstrumentId(OffsetDateTime.now.minusYears(20), OffsetDateTime.now, id) //todo use marketInstrument object for syntax

  def buyOperationsPrices:  List[Operation] => List[BigDecimal] = _
    .filter(operation =>
      operation.operationType.contains("Buy") &&
        operation.status == "Done")
    .flatMap(_.price)

  def sellOperationsPrices:  List[Operation] => List[BigDecimal] = _
    .filter(operation =>
      operation.operationType.contains("Sell") &&
        operation.status == "Done")
    .flatMap(_.price)
  def commissions:  List[Operation] => List[BigDecimal] = _
    .filter(operation => operation.status == "Done")
    .flatMap(_.commission.map(_.value))

  def operationsByInstrumentId[F[_]: Applicative : HttpClient](from: OffsetDateTime, to: OffsetDateTime, id: InstrumentId): Stream[F, Operations] = ???
    //position(id).through(operationsFromPosition(from, to))


  def operationsFromPosition[F[_]: Applicative](from: OffsetDateTime, to: OffsetDateTime)(implicit tcsClient: HttpClient[F]): Pipe[F, Position, Operations] = in =>
    in.flatMap(s => Tinkoff.operations(from, to, s.figi))

  def position[F[_]: Applicative : HttpClient]: InstrumentId => Stream[F, List[Position]] = { //todo find way debug this Either or Validation to get exception handling
    case Ticker(id) =>
      Tinkoff.positions
        .map(_.positions.filter(_.ticker.contains(id)))
    case Figi(id)   =>
      Tinkoff.positions
        .map(_.positions.filter(_.figi == id))
  }


}

// ${pos.averagePositionPriceNoNkd.map(_.value * pos.lots).getOrElse(BigDecimal.defaultMathContext)  ${pos.balance} ->