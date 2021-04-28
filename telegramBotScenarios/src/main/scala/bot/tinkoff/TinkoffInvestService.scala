package bot.tinkoff

import bot.inMemoryStorage.InMemoryAccountsStorage
import cats.Monad
import tcs4sclient.api.client.TinkoffClient
import tcsInterpreters.{ AccountsMap, PositionsInfo, StockProfit, TinkoffInvest, TinkoffInvestPrograms }

trait TinkoffInvestLogic[G[_[_]]] extends {
  def run[F[_]](F: TinkoffInvestPrograms[F]): G[F]
}

object TinkoffInvestLogic {
  implicit def accounts: TinkoffInvestLogic[AccountsMap] =
    new TinkoffInvestLogic[AccountsMap] {
      override def run[F[_]](F: TinkoffInvestPrograms[F]): AccountsMap[F] = F.accountsMap
    }

  implicit def positionsList: TinkoffInvestLogic[PositionsInfo] =
    new TinkoffInvestLogic[PositionsInfo] {
      override def run[F[_]](F: TinkoffInvestPrograms[F]): PositionsInfo[F] =
        F.positionsInfo
    }

  implicit def stockProfit: TinkoffInvestLogic[StockProfit] =
    new TinkoffInvestLogic[StockProfit] {
      override def run[F[_]](F: TinkoffInvestPrograms[F]): StockProfit[F] =
        F.historyProfit
    }

  def apply[G[_[_]]: TinkoffInvestLogic]: TinkoffInvestLogic[G] = implicitly[TinkoffInvestLogic[G]]
}

class TinkoffInvestService[G[_[_]]: TinkoffInvestLogic] {
  def make[F[_]: TinkoffClient: InMemoryAccountsStorage: Monad]: G[F] = TinkoffInvestLogic[G].run(TinkoffInvest.dsl[F])
}
