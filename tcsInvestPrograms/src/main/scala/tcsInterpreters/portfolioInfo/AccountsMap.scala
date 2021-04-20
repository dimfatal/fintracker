package tcsInterpreters.portfolioInfo

import cats.Functor
import cats.implicits._
import fs2._
import tcs4sclient.api.client.TinkoffClient
import tcs4sclient.api.methods.UserAccountApi
import tcs4sclient.model.domain.user.AccountType

case class AccountsMap[F[_]: Functor: TinkoffClient]()(implicit accountApi: UserAccountApi[F]) {

  def make(): Stream[F, Map[AccountType, String]] = for {
    accountsMap <- Stream.eval(
                     UserAccountApi[F]
                       .accounts
                       .map(
                         _.accounts
                           .map(account => account.brokerAccountType -> account.brokerAccountId)
                           .toMap
                       )
                   )
  } yield accountsMap
}
