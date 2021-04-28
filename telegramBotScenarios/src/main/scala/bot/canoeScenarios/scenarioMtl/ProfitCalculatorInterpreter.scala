package bot.canoeScenarios.scenarioMtl

import bot.inMemoryStorage.AccountTypeStorageSyntax.AccountTypeIdOps
import bot.inMemoryStorage._
import bot.tinkoff._
import canoe.api.TelegramClient
import cats.MonadError
import cats.data.OptionT
import cats.syntax.all._
import tcs4sclient.api.client.TinkoffClient
import tcs4sclient.api.methods.TinkoffInvestApi
import tcs4sclient.model.domain.market.{ MarketInstrument, Ticker }
import tcs4sclient.model.domain.user.AccountType
import tcsInterpreters.{ AccountId, StockProfit }

object ProfitCalculatorInterpreter {
  def makeScenario[F[_]: TinkoffClient: TelegramClient: InMemoryAccountsStorage](implicit
    ae: MonadError[F, ProfitCalculatorError]
  ): ProfitCalculator[F] =
    new ProfitCalculator[F] {

      private def validateTickerNotEmpty(tickerName: String): F[Unit] = //todo may use RegExp instead
        if (tickerName.isEmpty) ae.raiseError(TelegramParameterExist(tickerName))
        else ae.unit

      private def validateTickerNoSpacesContain(tickerName: String): F[Unit] = //todo may use RegExp instead
        if (tickerName.contains(" ")) ae.raiseError(TelegramParameterCorrect(tickerName))
        else ae.unit

      private def validateTicker(tickerName: String): F[Unit] =
        OptionT(
          TinkoffInvestApi
            .marketInstrumentsInstance
            .searchByTicker(tickerName)
            .map(_.total)
            .map {
              case i if i > 0 => Some(i)
              case _          => None
            }
        )
          .fold(ae.raiseError[Unit](TelegramParameterUsefulForMarketTicker(tickerName)))(_ => ae.unit)
          .flatten //todo make not Unit

      private def validateAccount(account: AccountType): F[AccountId] =
        account.findId.fold(ae.raiseError[AccountId](AccountNotFountError(account)))(s => s.pure[F]).flatten //todo make not Unit

      override def calculate(tickerName: String, account: AccountType): fs2.Stream[F, Map[MarketInstrument, BigDecimal]] = {
        val i = validateTickerNotEmpty(tickerName) *>
          validateTickerNoSpacesContain(tickerName) *>
          validateTicker(tickerName) *>
          validateAccount(account)
            .map(id => new TinkoffInvestService[StockProfit].make.calculate(id, Ticker(tickerName)))
            .flatten

        fs2.Stream.eval(i)
      }
    }
}
