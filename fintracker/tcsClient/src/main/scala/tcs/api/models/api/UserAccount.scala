package tcs.api.models.api

//import cats.Functor
//import org.http4s.Method
//import tcs1.api.client.HttpClient
//import tcs1.domain.model.user.{Account, AccountType, Accounts}
//
//trait AccountApi[F[_]] {
//  def getAll: F[Accounts]
//  def find(accountType: AccountType): F[Option[Account]]
//}
//
//class AccountService[F[_]](client : HttpClient[F])(implicit F : Functor[F]) extends AccountApi[F] {
//
//  import tcs1.domain.model.user.AccountsResponseDecoder.accountsDecoder
//
//  val path: String = "/user/accounts"
//
//  override def getAll: F[Accounts] = client.execute(Method.GET, path, Map.empty)
//
//  override def find(accountType: AccountType): F[Option[Account]] =
//    F.map(getAll)(
//      _.accounts
//        .find(
//          _.brokerAccountType == accountType))
//}




