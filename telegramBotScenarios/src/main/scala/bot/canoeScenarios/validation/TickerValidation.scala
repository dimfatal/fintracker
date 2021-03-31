package bot.canoeScenarios.validation

import cats.ApplicativeError
import cats.data.Nested
import cats.effect.Sync
import cats.implicits._
import org.http4s.client.Client
import tcs4sclient.api.client.{ Http4sTinkoffClientBuilder, TinkoffClient }
import tcs4sclient.api.methods.TinkoffInvestApi
import tcs4sclient.model.domain.market.Ticker

trait TickerValidation {
  def errorMessages: String
}

case object NotFoundInstrumentValidation extends TickerValidation {
  override def errorMessages: String = "Can't find any market instrument by this ticker"
}

object TickerValidator {
  def apply[F[_]](implicit t: TickerValidator[F]): TickerValidator[F]                                             = t
  def validate[F[_]: TickerValidator, S[_]: Client: Sync, E](ticker: Ticker, token: String): Nested[S, F, Ticker] =
    TickerValidator[F].tickerValid[S](ticker, token)
}

sealed trait TickerValidator[F[_]] {
  def tickerValid[S[_]: Sync: Client](ticker: Ticker, token: String): Nested[S, F, Ticker]
}

object TickerValidatorInterpreter {
  def tickerValidator[F[_], E](mkError: TickerValidation => E)(implicit A: ApplicativeError[F, E]): TickerValidator[F] =
    new TickerValidator[F] {
      override def tickerValid[S[_]: Sync: Client](ticker: Ticker, token: String): Nested[S, F, Ticker] = {
        implicit val c: TinkoffClient[S] = Http4sTinkoffClientBuilder.fromHttp4sClient[S](token)(implicitly[Client[S]])
        existOnMarket(ticker)
      }

      private def existOnMarket[S[_]: TinkoffClient: Sync](ticker: Ticker): Nested[S, F, Ticker] =
        Nested(
          TinkoffInvestApi
            .marketInstrumentsInstance
            .searchByTicker(ticker.id)
            .map(_.total)
            .map { i =>
              if (i > 0) ticker.pure[F]
              else A.raiseError(mkError(NotFoundInstrumentValidation))
            }
        )

    }

}
