package tcsInterpreters

import cats.effect.IO
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.scalatest.freespec.AnyFreeSpec
import tcsInterpreters.portfolioInfo.PortfolioStockPrices

class StockPortfolioSpec extends AnyFreeSpec {

  val ataraStockOperations = OperationsApiJson.operations

  private implicit val logger = Slf4jLogger.getLogger[IO]
  "Position direction" - {
    "long" in {
      val ss = PortfolioStockPrices.getHoldPositions(ataraStockOperations).groupMap(a => a.operationType)(x => (x.price, x.date))
      Logger[IO].info(s"\n${ss.head._1} -> \n\t${ss.head._2.mkString("\n\t")}").unsafeRunSync()
      assert(ss.head._1 == "Buy")
      assert(ss.head._2.length == 2)
    }
    "short" in {}
  }
}
