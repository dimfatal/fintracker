package broker.interpreters

import broker.dsl.{TcsBrokerApi, UserAccount, UserAccountOperations}
import cats.Applicative
import fs2.Stream
import tcs.api.client.HttpClient
import tcs.api.models.user.AccountApi
import tcs.api.models.user.operations.OperationsApi
import tcs.api.models.user.portfolio.UserPortfolioApi
import tcs.domain.model.user.{Account, AccountType, Accounts}
import tcs.domain.model.{Operations, Positions}

import java.time.OffsetDateTime


object BrokerApi {

  object UserAccountOperations {
    def dsl[F[_]: HttpClient : Applicative](accType: AccountType) : UserAccountOperations[F] = new UserAccountOperations[F]{

      implicit override lazy val accounts: Stream[F, Accounts] = Stream.eval(AccountApi[F].accounts)

     // implicit override val accountType: AccountType = `type`

      private def account: AccountType => Stream[F, Account] = in =>
        Stream.eval(AccountApi[F].getAccount(in))

      def positions: Stream[F, Positions] =
        account(accType).evalMap(
          UserPortfolioApi(_).positions
        )

      def operations(from: OffsetDateTime, to: OffsetDateTime, figi: String) : Stream[F, Operations] =
        account(accType).evalMap(
          OperationsApi(_)
            .operations(from, to, figi)
        )
    }
  }
}

object BrokerApi1 {

  object UserAccountOperations {
    def dsl[F[_]: HttpClient : Applicative] : UserAccount[F] = new UserAccount[F] {


      override lazy val accounts: Stream[F, Accounts] = Stream.eval(AccountApi[F].accounts)

      private def account(implicit accountType: AccountType) : Stream[F, Account] =
        Stream.eval(AccountApi[F].getAccount(accountType))

      implicit class UserAccountOperations(accountType: AccountType) extends TcsBrokerApi[F] {

        override def operations(from: OffsetDateTime, to: OffsetDateTime, figi: String): Stream[F, Operations] =
          account(accountType).evalMap(
            OperationsApi(_)
              .operations(from, to, figi)
          )

        override def positions: Stream[F, Positions] =
          account(accountType).evalMap(
            UserPortfolioApi(_).positions
          )
      }
    }
  }
}
