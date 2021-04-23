package bot.canoeScenarios

import bot.canoeScenarios.validation._
import bot.inMemoryStorage.AccountTypeStorageSyntax.AccountTypeIdOps
import bot.inMemoryStorage.{ InMemoryAccountsStorage, TinkoffTokenStorage }
import bot.tinkoff.StockProfitMap
import bot.tinkoff.TinkoffInvestPrograms.{ TinkoffInvestLogic, TinkoffService }
import canoe.api._
import canoe.models.Chat
import canoe.syntax._
import cats.Applicative
import cats.effect.Sync
import cats.effect.concurrent.Semaphore
import cats.implicits._
import org.http4s.client.Client
import tcs4sclient.api.client.{ Http4sTinkoffClientBuilder, TinkoffClient }
import tcs4sclient.model.domain.market.{ MarketInstrument, Ticker }
import tcs4sclient.model.domain.user.{ AccountType, Tinkoff }
import tcsInterpreters.portfolioInfo.DateTimePeriod.allTime
import tcsInterpreters.portfolioInfo.PeriodQuery

object CheckStockProfitFromPeriod {

  private implicit val tickerValidator: TickerValidator[Either[TickerValidation, *]]                      =
    TickerValidatorInterpreter.tickerValidator[Either[TickerValidation, *], TickerValidation](identity)
  private implicit val commandValidator: CommandParameterValidator[Either[CommandParameterValidation, *]] =
    CommandParameterValidatorInterpreter.commandValidator[Either[CommandParameterValidation, *], CommandParameterValidation](identity)

  def run[F[_]: Sync: TelegramClient: InMemoryAccountsStorage: Client](
    semaphore: Semaphore[F],
    tokenStore: TinkoffTokenStorage[F],
    periodQuery: PeriodQuery = allTime,
    account: AccountType = Tinkoff
  ): Scenario[F, Unit] = {

    def stockProfitService(ticker: Ticker)(implicit client: TinkoffClient[F]): F[fs2.Stream[F, StockProfitMap]] =
      account
        .id
        .map(TinkoffInvestLogic.stockProfit(_, ticker, periodQuery))
        .map(implicit i => new TinkoffService[StockProfitMap].run)

    def tinkoffClient(token: String): TinkoffClient[F] = Http4sTinkoffClientBuilder.fromHttp4sClient(token)(implicitly[Client[F]])

    Scenario.eval(semaphore.available).flatMap { i =>
      if (i > 0) {
        Scenario.expect(command("p").andThen(x => (x.chat, x.text.split(" ").tail))).flatMap { case (chat, param) =>
          Scenario.eval(tokenStore.get.map(tinkoffClient)).flatMap(implicit c => calculate(chat, param)(stockProfitService))
        }
      } else Scenario.expect(command("p").chat).flatMap(chat => Scenario.eval(chat.send("run /s - command"))) >> Scenario.done
    }
  }

  private def calculate[F[_]: Sync: TinkoffClient: TelegramClient: InMemoryAccountsStorage](
    chat: Chat,
    userInput: Array[String]
  )( //todo how resolve it with MonadError ???
    service: Ticker => F[fs2.Stream[F, StockProfitMap]]
  ): Scenario[F, Unit] =
    CommandParameterValidator
      .validate(CommandParameter(userInput, expectedCount = 1))
      .fold(
        e =>
          e match {
            case CommandParameterExistValidation =>
              Scenario
                .eval(
                  chat
                    .send("enter ticker which you want to check")
                )
                .flatMap { _ =>
                  Scenario.expect(textMessage)
                }
                .flatMap(tickerMessage => calculate[F](chat, tickerMessage.text.split(" "))(service))

            case CommandParameterExpectedCount =>
              Scenario.eval(chat.send(s"invalid parameter ${CommandParameterExpectedCount.errorMessage}").map(_ => ()))

          },
        cmd =>
          Scenario
            .eval(
              TickerValidator
                .validate(Ticker(cmd.args.last))
                .value
                .map(
                  _.fold(
                    e => Scenario.eval(chat.send(s"invalid ticker - ${e.errorMessages}").map(_ => ())),
                    validTicker =>
                      Scenario
                        .eval(
                          fs2
                            .Stream
                            .eval(service(validTicker))
                            .flatten
                            .compile
                            .toList
                            .map(_.head)
                        )
                        .flatMap(marketInstrumentMap => askWhichInstrumentShouldBeTaken[F](marketInstrumentMap.items, chat))
                        .flatMap(result => Scenario.eval(chat.send(result)))
                        .flatMap(_ => Scenario.done)
                  )
                )
            )
            .flatten
      )

  def askWhichInstrumentShouldBeTaken[F[_]: Applicative: TelegramClient](
    instruments: Map[MarketInstrument, String],
    chat: Chat
  ): Scenario[F, String] =
    if (instruments.keys.size > 1) {
      Scenario.eval(
        chat
          .send(s"send index of instrument you want to check - ${instruments.keys.map(_.name).zipWithIndex.map { case (element, index) =>
            s" $index -> $element"
          }}")
      ) >> Scenario
        .expect(textMessage)
        .map(message =>
          message
            .text
            .toIntOption
            .flatMap(index => instruments.keys.toList.get(index))
            .map(instruments)
            .getOrElse("wrong index")
        )
    } else Scenario.eval(instruments.last._2.pure[F])
}
