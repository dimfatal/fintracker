package tcs.api.models.user

import cats.Functor
import cats.data.ReaderT
import org.http4s.Method
import tcs.api.client.HttpClient
import tcs.api.models.TcsApi
import tcs.domain.model.user.{Account, AccountType, Accounts}


trait AccountApi[F[_]] extends TcsApi {
  import tcs.domain.model.user.AccountsDecoder.accountsDecoder

  def mapAccountType : ReaderT[F, AccountType, Account]

  def accounts: ReaderT[F, HttpClient[F], Accounts] = ReaderT { implicit client =>
    client.execute(Method.GET, path, Map.empty)
  }
}

object AccountApi {
  implicit def accountApiInstance[F[_]: Functor: HttpClient]: AccountApi[F] = new AccountApi[F] {
    val path: String = "/user/accounts"

    override def mapAccountType : ReaderT[F, AccountType, Account] = ReaderT { accType : AccountType =>
      val reader = for {
        req <- accounts
        account = req.accounts.filter(_.brokerAccountType == accType).head
      }yield account

      reader.run(implicitly[HttpClient[F]])
    }
  }
}



