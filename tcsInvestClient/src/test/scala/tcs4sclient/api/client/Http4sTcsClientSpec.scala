package tcs4sclient.api.client

import cats.effect.IO
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import io.circe.Decoder
import org.http4s._
import org.http4s.client.Client
import org.http4s.dsl.io._
import org.scalatest.freespec.AnyFreeSpec
import tcs4sclient.api.{ FailedMethod, FailedResponse, ResponseDecodingError }
import tcs4sclient.model.domain.TradeHistory

class Http4sTcsClientSpec extends AnyFreeSpec {

  private def response(s: String) = s"""{"status" : "ok", "trackingId": "String", "payload" : "$s"}"""
  private implicit val logger     = Slf4jLogger.getLogger[IO]

  "Client" - {
    "sends" - {
      "to correct tinkoff endpoint" in {
        val client: Client[IO] = Client.fromHttpApp(HttpApp(request => Ok(response(request.uri.toString))))
        val tcsClient          = new Tinkoff4STcsClient("token", client)

        val testMethodName = "test"

        assert(
          tcsClient.execute[String](Method.GET, testMethodName, Map.empty).unsafeRunSync() ==
            s"https://api-invest.tinkoff.ru/openapi/${testMethodName}"
        )
      }

      val tcsClient = new Tinkoff4STcsClient(
        "",
        Client.fromHttpApp(HttpApp[IO] { r =>
          Ok(
            response(
              r.headers
                .get(org.http4s.headers.`Content-Type`)
                .map(_.value.replace("\"", "''"))
                .getOrElse("")
            )
          )
        })
      )

      "json POST request if attachments contain file upload" in {
        assert(tcsClient.execute[String](Method.POST, "testMethodName", Map.empty).unsafeRunSync() == "application/json")
      }
    }

    "encodes/decodes" - {
      //      "request entity with method encoder" in {
      //        val tcsClient = new Http4sTcsClient(
      //          "",
      //          Client.fromHttpApp(HttpApp[IO](_
      //            .bodyText
      //            .compile
      //            .string
      //            .flatMap(s =>
      //              Ok(response(s.replace("\"", "'"))))))
      //        )
      //
      //        Encoder.instance[String](_ => Json.fromString("encoded"))
      //        val res = tcsClient.execute[String]()
      //
      //        assert(res.unsafeRunSync() == "'encoded'")
      //      }

      "result entity with method decoder" in {
        val tcsClient                     = new Tinkoff4STcsClient("", Client.fromHttpApp(HttpApp[IO](_ => Ok(response("")))))
        implicit val dec: Decoder[String] = Decoder.const("decoded")
        val res                           = tcsClient.execute[String](Method.GET, "testMethodName", Map.empty)

        assert(res.unsafeRunSync() == "decoded")
      }

      "result entity decoder with either" in {
        val tcsClient = new Tinkoff4STcsClient("", Client.fromHttpApp(HttpApp[IO](_ => Ok(OperationsApiJson.rawJsonAtaraOperations))))
        import tcs4sclient.model.domain.user.operations.OperationDecoder.decoderOperations

        val res = tcsClient.execute[TradeHistory](Method.GET, "testMethodName", Map.empty).unsafeRunSync()

        Logger[IO].info(res.operations.length.toString).unsafeRunSync()
        assert(res.operations.length == 13)
      }
    }

    "handles" - {
      "decode failure as ResponseDecodingError" in {
        val tcsClient = new Tinkoff4STcsClient("", Client.fromHttpApp(HttpApp[IO](_ => Ok("{}"))))

        assertThrows[ResponseDecodingError](tcsClient.execute[String](Method.GET, "testMethodName", Map.empty).unsafeRunSync())
      }

      "unsuccessful result as FailedMethod" in {
        val response  = """{"status" : "ok", "trackingId": "" }"""
        val tcsClient = new Tinkoff4STcsClient("", Client.fromHttpApp(HttpApp[IO](_ => Ok(response))))

        assertThrows[FailedMethod[String]](tcsClient.execute[String](Method.GET, "testMethodName", Map.empty).unsafeRunSync())
      }

      "decode error response as FailedMethod" in {
        val response  = """                                                                                         {
                         |  "trackingId": "4fbd52cf59b70080",
                         |  "payload": {
                         |    "message": "Unknown account",
                         |    "code": "GATEWAY_REQUEST_DATA_ERROR"
                         |  },
                         |  "status": "Error"
                         |}
                         |""".stripMargin
        val tcsClient = new Tinkoff4STcsClient("", Client.fromHttpApp(HttpApp[IO](_ => Ok(response))))

        val ss = intercept[FailedResponse] {
          tcsClient.execute[String](Method.GET, "testMethodName", Map.empty).unsafeRunSync()
        }
        assert(ss.response.message == "Unknown account")
        assert(ss.response.code == "GATEWAY_REQUEST_DATA_ERROR")
      }
    }
  }
}
