import io.circe.parser.decode
import tcs4sclient.model.domain.user.operations.Operation
import tcs4sclient.model.domain.user.operations.OperationDecoder.decoderOperations
import tcs4sclient.model.domain.{TcsResponse, TradeHistory}

/*object TestJson {
  /*private val rawJson: String =
    """
      |{
      |  "trackingId": "bf3385877c0bc655",
      |  "payload": {
      |    "positions": [
      |      {
      |        "figi": "BBG005Q3MQY4",
      |        "ticker": "ATRA",
      |        "isin": "US0465131078",
      |        "instrumentType": "Stock",
      |        "balance": 2,
      |        "lots": 2,
      |        "expectedYield": {
      |          "currency": "USD",
      |          "value": -2.24
      |        },
      |        "averagePositionPrice": {
      |          "currency": "USD",
      |          "value": 19.9
      |        },
      |        "name": "Atara Biotherapeutics Inc"
      |      },
      |      {
      |        "figi": "BBG000C1S2X2",
      |        "ticker": "VRTX",
      |        "isin": "US92532F1003",
      |        "instrumentType": "Stock",
      |        "balance": 3,
      |        "lots": 3,
      |        "expectedYield": {
      |          "currency": "USD",
      |          "value": 0.15
      |        },
      |        "averagePositionPrice": {
      |          "currency": "USD",
      |          "value": 213.38
      |        },
      |        "name": "Vertex Pharmaceuticals"
      |      },
      |      {
      |        "figi": "BBG000BN5LG7",
      |        "ticker": "VALE",
      |        "isin": "US91912E1055",
      |        "instrumentType": "Stock",
      |        "balance": 1,
      |        "lots": 1,
      |        "expectedYield": {
      |          "currency": "USD",
      |          "value": -0.18
      |        },
      |        "averagePositionPrice": {
      |          "currency": "USD",
      |          "value": 17.67
      |        },
      |        "name": "Vale SA"
      |      },
      |      {
      |        "figi": "BBG000BR2B91",
      |        "ticker": "PFE",
      |        "isin": "US7170811035",
      |        "instrumentType": "Stock",
      |        "balance": 55,
      |        "lots": 55,
      |        "expectedYield": {
      |          "currency": "USD",
      |          "value": -174.44
      |        },
      |        "averagePositionPrice": {
      |          "currency": "USD",
      |          "value": 37.87
      |        },
      |        "name": "Pfizer"
      |      },
      |      {
      |        "figi": "BBG007HTCQT0",
      |        "ticker": "MOMO",
      |        "isin": "US60879B1070",
      |        "instrumentType": "Stock",
      |        "balance": 1,
      |        "lots": 1,
      |        "expectedYield": {
      |          "currency": "USD",
      |          "value": 1.42
      |        },
      |        "averagePositionPrice": {
      |          "currency": "USD",
      |          "value": 18.69
      |        },
      |        "name": "Momo"
      |      },
      |      {
      |        "figi": "BBG000BVPV84",
      |        "ticker": "AMZN",
      |        "isin": "US0231351067",
      |        "instrumentType": "Stock",
      |        "balance": 2,
      |        "lots": 2,
      |        "expectedYield": {
      |          "currency": "USD",
      |          "value": -135.72
      |        },
      |        "averagePositionPrice": {
      |          "currency": "USD",
      |          "value": 3346.36
      |        },
      |        "name": "Amazon.com"
      |      },
      |      {
      |        "figi": "BBG000HJTMS9",
      |        "ticker": "DTE@DE",
      |        "isin": "DE0005557508",
      |        "instrumentType": "Stock",
      |        "balance": 1,
      |        "lots": 1,
      |        "expectedYield": {
      |          "currency": "EUR",
      |          "value": -0.26
      |        },
      |        "averagePositionPrice": {
      |          "currency": "EUR",
      |          "value": 15.18
      |        },
      |        "name": "Deutsche Telekom AG"
      |      },
      |      {
      |        "figi": "BBG000BBPFB9",
      |        "ticker": "AMAT",
      |        "isin": "US0382221051",
      |        "instrumentType": "Stock",
      |        "balance": 2,
      |        "lots": 2,
      |        "expectedYield": {
      |          "currency": "USD",
      |          "value": 7.77
      |        },
      |        "averagePositionPrice": {
      |          "currency": "USD",
      |          "value": 113.46
      |        },
      |        "name": "Applied Materials"
      |      },
      |      {
      |        "figi": "BBG000BJF1Z8",
      |        "ticker": "FDX",
      |        "isin": "US31428X1063",
      |        "instrumentType": "Stock",
      |        "balance": 1,
      |        "lots": 1,
      |        "expectedYield": {
      |          "currency": "USD",
      |          "value": 10.03
      |        },
      |        "averagePositionPrice": {
      |          "currency": "USD",
      |          "value": 252.6
      |        },
      |        "name": "FedEx"
      |      },
      |      {
      |        "figi": "BBG000BC4JJ4",
      |        "ticker": "APD",
      |        "isin": "US0091581068",
      |        "instrumentType": "Stock",
      |        "balance": 1,
      |        "lots": 1,
      |        "expectedYield": {
      |          "currency": "USD",
      |          "value": -22.85
      |        },
      |        "averagePositionPrice": {
      |          "currency": "USD",
      |          "value": 283.4
      |        },
      |        "name": "Air Products & Chemicals"
      |      },
      |      {
      |        "figi": "TCS00A102EK1",
      |        "ticker": "TBIO",
      |        "isin": "RU000A102EK1",
      |        "instrumentType": "Etf",
      |        "balance": 1700,
      |        "lots": 1700,
      |        "expectedYield": {
      |          "currency": "USD",
      |          "value": 20.59
      |        },
      |        "averagePositionPrice": {
      |          "currency": "USD",
      |          "value": 0.0971
      |        },
      |        "name": "Тинькофф NASDAQ Biotech"
      |      },
      |      {
      |        "figi": "BBG005DXDPK9",
      |        "ticker": "FXGD",
      |        "isin": "IE00B8XB7377",
      |        "instrumentType": "Etf",
      |        "balance": 61,
      |        "lots": 61,
      |        "expectedYield": {
      |          "currency": "RUB",
      |          "value": -3427.6
      |        },
      |        "averagePositionPrice": {
      |          "currency": "RUB",
      |          "value": 958
      |        },
      |        "name": "FinEx Золото"
      |      },
      |      {
      |        "figi": "BBG00NRFC2X2",
      |        "ticker": "FXTB",
      |        "isin": "IE00BL3DYW26",
      |        "instrumentType": "Etf",
      |        "balance": 1,
      |        "lots": 1,
      |        "expectedYield": {
      |          "currency": "RUB",
      |          "value": -1.7
      |        },
      |        "averagePositionPrice": {
      |          "currency": "RUB",
      |          "value": 749
      |        },
      |        "name": "FinEx Казначейские облигации США (USD)"
      |      },
      |      {
      |        "figi": "BBG0013HGFT4",
      |        "ticker": "USD000UTSTOM",
      |        "instrumentType": "Currency",
      |        "balance": 175.62,
      |        "lots": 0,
      |        "expectedYield": {
      |          "currency": "RUB",
      |          "value": -224.18
      |        },
      |        "averagePositionPrice": {
      |          "currency": "RUB",
      |          "value": 74.5475
      |        },
      |        "name": "Доллар США"
      |      },
      |      {
      |        "figi": "BBG0013HJJ31",
      |        "ticker": "EUR_RUB__TOM",
      |        "instrumentType": "Currency",
      |        "balance": 1951.44,
      |        "lots": 1,
      |        "expectedYield": {
      |          "currency": "RUB",
      |          "value": -6359.69
      |        },
      |        "averagePositionPrice": {
      |          "currency": "RUB",
      |          "value": 92.165
      |        },
      |        "name": "Евро"
      |      }
      |    ]
      |  },
      |  "status": "Ok"
      |}
      |""".stripMargin*/
}*/

object OperationsApiJson {
  val rawJsonAtaraOperations: String =
    """
      |{
      |  "trackingId": "ef884b909bd800e5",
      |  "payload": {
      |    "operations": [
      |      {
      |        "operationType": "Buy",
      |        "date": "2021-02-08T23:16:21.909026+03:00",
      |        "isMarginCall": false,
      |        "instrumentType": "Stock",
      |        "figi": "BBG005Q3MQY4",
      |        "quantity": 1,
      |        "quantityExecuted": 1,
      |        "price": 20.81,
      |        "payment": -20.81,
      |        "currency": "USD",
      |        "commission": {
      |          "currency": "USD",
      |          "value": -0.06
      |        },
      |        "trades": [
      |          {
      |            "tradeId": "3223243830",
      |            "date": "2021-02-08T23:16:21.909026+03:00",
      |            "quantity": 1,
      |            "price": 20.81
      |          }
      |        ],
      |        "status": "Done",
      |        "id": "202732909480"
      |      },
      |      {
      |        "operationType": "BrokerCommission",
      |        "date": "2021-02-08T23:16:21.909026+03:00",
      |        "isMarginCall": false,
      |        "instrumentType": "Stock",
      |        "figi": "BBG005Q3MQY4",
      |        "quantity": 0,
      |        "quantityExecuted": 0,
      |        "payment": -0.06,
      |        "currency": "USD",
      |        "status": "Done",
      |        "id": "774258226"
      |      },
      |      {
      |        "operationType": "Buy",
      |        "date": "2021-01-28T11:47:36.979+03:00",
      |        "isMarginCall": false,
      |        "instrumentType": "Stock",
      |        "figi": "BBG005Q3MQY4",
      |        "quantity": 1,
      |        "quantityExecuted": 1,
      |        "price": 18.99,
      |        "payment": -18.99,
      |        "currency": "USD",
      |        "commission": {
      |          "currency": "USD",
      |          "value": -0.06
      |        },
      |        "trades": [
      |          {
      |            "tradeId": "3023165490",
      |            "date": "2021-01-28T11:48:33.095+03:00",
      |            "quantity": 1,
      |            "price": 18.99
      |          }
      |        ],
      |        "status": "Done",
      |        "id": "198077130800"
      |      },
      |      {
      |        "operationType": "BrokerCommission",
      |        "date": "2021-01-28T11:47:36.979+03:00",
      |        "isMarginCall": false,
      |        "instrumentType": "Stock",
      |        "figi": "BBG005Q3MQY4",
      |        "quantity": 0,
      |        "quantityExecuted": 0,
      |        "payment": -0.06,
      |        "currency": "USD",
      |        "status": "Done",
      |        "id": "732595859"
      |      },
      |      {
      |        "operationType": "Sell",
      |        "date": "2020-08-20T17:58:14.285+03:00",
      |        "isMarginCall": false,
      |        "instrumentType": "Stock",
      |        "figi": "BBG005Q3MQY4",
      |        "quantity": 9,
      |        "quantityExecuted": 9,
      |        "price": 13.46,
      |        "payment": 121.14,
      |        "currency": "USD",
      |        "commission": {
      |          "currency": "USD",
      |          "value": -0.36
      |        },
      |        "trades": [
      |          {
      |            "tradeId": "1312063250",
      |            "date": "2020-08-20T17:58:14.285+03:00",
      |            "quantity": 9,
      |            "price": 13.46
      |          }
      |        ],
      |        "status": "Done",
      |        "id": "154202982140"
      |      },
      |      {
      |        "operationType": "BrokerCommission",
      |        "date": "2020-08-20T17:58:14.285+03:00",
      |        "isMarginCall": false,
      |        "instrumentType": "Stock",
      |        "figi": "BBG005Q3MQY4",
      |        "payment": -0.36,
      |        "currency": "USD",
      |        "status": "Done",
      |        "id": "326936251"
      |      },
      |      {
      |        "operationType": "Buy",
      |        "date": "2020-08-18T18:22:17.828+03:00",
      |        "isMarginCall": false,
      |        "instrumentType": "Stock",
      |        "figi": "BBG005Q3MQY4",
      |        "quantity": 2,
      |        "quantityExecuted": 2,
      |        "price": 12,
      |        "payment": -24,
      |        "currency": "USD",
      |        "commission": {
      |          "currency": "USD",
      |          "value": -0.07
      |        },
      |        "trades": [
      |          {
      |            "tradeId": "1298239040",
      |            "date": "2020-08-18T18:25:50.17+03:00",
      |            "quantity": 2,
      |            "price": 12
      |          }
      |        ],
      |        "status": "Done",
      |        "id": "153555473660"
      |      },
      |      {
      |        "operationType": "BrokerCommission",
      |        "date": "2020-08-18T18:22:17.828+03:00",
      |        "isMarginCall": false,
      |        "instrumentType": "Stock",
      |        "figi": "BBG005Q3MQY4",
      |        "payment": -0.07,
      |        "currency": "USD",
      |        "status": "Done",
      |        "id": "323127157"
      |      },
      |      {
      |        "operationType": "Buy",
      |        "date": "2020-08-14T22:54:31.077+03:00",
      |        "isMarginCall": false,
      |        "instrumentType": "Stock",
      |        "figi": "BBG005Q3MQY4",
      |        "quantity": 3,
      |        "quantityExecuted": 3,
      |        "price": 12.17,
      |        "payment": -36.51,
      |        "currency": "USD",
      |        "commission": {
      |          "currency": "USD",
      |          "value": -0.11
      |        },
      |        "trades": [
      |          {
      |            "tradeId": "1285532800",
      |            "date": "2020-08-14T22:59:21.065+03:00",
      |            "quantity": 3,
      |            "price": 12.17
      |          }
      |        ],
      |        "status": "Done",
      |        "id": "153008599020"
      |      },
      |      {
      |        "operationType": "BrokerCommission",
      |        "date": "2020-08-14T22:54:31.077+03:00",
      |        "isMarginCall": false,
      |        "instrumentType": "Stock",
      |        "figi": "BBG005Q3MQY4",
      |        "payment": -0.11,
      |        "currency": "USD",
      |        "status": "Done",
      |        "id": "318723212"
      |      },
      |      {
      |        "operationType": "Buy",
      |        "date": "2020-08-14T20:04:18.599+03:00",
      |        "isMarginCall": false,
      |        "instrumentType": "Stock",
      |        "figi": "BBG005Q3MQY4",
      |        "quantity": 1,
      |        "quantityExecuted": 0,
      |        "price": 11.98,
      |        "payment": 0,
      |        "currency": "USD",
      |        "status": "Decline",
      |        "id": "152962858460"
      |      },
      |      {
      |        "operationType": "Buy",
      |        "date": "2020-08-07T17:34:21.912+03:00",
      |        "isMarginCall": false,
      |        "instrumentType": "Stock",
      |        "figi": "BBG005Q3MQY4",
      |        "quantity": 4,
      |        "quantityExecuted": 4,
      |        "price": 12.7,
      |        "payment": -50.8,
      |        "currency": "USD",
      |        "commission": {
      |          "currency": "USD",
      |          "value": -0.15
      |        },
      |        "trades": [
      |          {
      |            "tradeId": "1236127260",
      |            "date": "2020-08-07T17:46:42.67+03:00",
      |            "quantity": 4,
      |            "price": 12.7
      |          }
      |        ],
      |        "status": "Done",
      |        "id": "150985549150"
      |      },
      |      {
      |        "operationType": "BrokerCommission",
      |        "date": "2020-08-07T17:34:21.912+03:00",
      |        "isMarginCall": false,
      |        "instrumentType": "Stock",
      |        "figi": "BBG005Q3MQY4",
      |        "payment": -0.15,
      |        "currency": "USD",
      |        "status": "Done",
      |        "id": "306585119"
      |      }
      |    ]
      |  },
      |  "status": "Ok"
      |}
      |""".stripMargin

  val operations: List[Operation] = decode[TcsResponse[TradeHistory]](rawJsonAtaraOperations).toOption.get.payload.get.operations

}
