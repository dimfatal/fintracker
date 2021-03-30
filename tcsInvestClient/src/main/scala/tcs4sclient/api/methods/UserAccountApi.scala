package tcs4sclient.api.methods

import org.http4s.Method
import tcs4sclient.api.client.TinkoffClient
import tcs4sclient.model.domain.user.Accounts

trait UserAccountApi[F[_]] {
  def accounts: F[Accounts]
}

object UserAccountApi {

  def apply[F[_]: UserAccountApi]: UserAccountApi[F] = implicitly[UserAccountApi[F]]

  implicit def userAccountInstance[F[_]: TinkoffClient]: UserAccountApi[F] = new UserAccountApi[F] {

    import tcs4sclient.model.domain.user.AccountsDecoder.accountsDecoder

    val path: String = "/user/accounts"

    override def accounts: F[Accounts] = implicitly[TinkoffClient[F]].execute[Accounts](Method.GET, path, Map.empty)
  }
}
