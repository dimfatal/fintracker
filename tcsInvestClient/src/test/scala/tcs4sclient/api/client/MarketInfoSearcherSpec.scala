//package tcs4sclient.api.client

// class MarketInfoSearcherSpec extends AnyFreeSpec {

//   // private implicit val logger = Slf4jLogger.getLogger[IO]
// //   "Market search instrument by " - {
// //     "ticker" in {

// // //      val marketInstrumentsApi: MarketInstrumentsApi[IO] = TinkoffInvestApi.marketInstrumentsInstance[IO] //todo should it used like syntax ops type class for ticker or figi
// // //
// // //      override def searchInstrumentByTicker(ticker: Ticker): Stream[F, MarketInstruments] =
// // //        Stream.eval(marketInstrumentsApi.searchByTicker(ticker.id))
// // //      val ss = PortfolioStockPrices.getHoldPositions(ataraStockOperations).groupMap(a => a.operationType)(x => (x.price, x.date))
// // //      Logger[IO].info(s"\n${ss.head._1} -> \n\t${ss.head._2.mkString("\n\t")}").unsafeRunSync()
// // //      assert(ss.head._1 == "Buy" )
// // //      assert(ss.head._2.length == 2 )
// //     }
// //     "figi" in {}
// //   }
// }
