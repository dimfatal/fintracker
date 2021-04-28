package bot.inMemoryStorage

import cats.Functor
import cats.data.OptionT
import cats.effect.ConcurrentEffect
import cats.effect.concurrent.Ref
import cats.implicits._
import tcs4sclient.model.domain.user.AccountType
import tcsInterpreters.AccountId

trait AccountsStorage[F[_]] {
  def find(accountType: AccountType)(implicit f: Functor[F]): F[Option[String]]
  def save(accounts: Map[AccountType, String]): F[Unit]
  def clear(): F[Unit]
}

class InMemoryAccountsStorage[F[_]](private val ref: Ref[F, Option[Map[AccountType, String]]]) extends AccountsStorage[F] {

  override def find(accountType: AccountType)(implicit F: Functor[F]): F[Option[String]] =
    getAllAccounts.map(_.get(accountType))

  override def save(accounts: Map[AccountType, String]): F[Unit] =
    ref.set(Option(accounts))

  override def clear(): F[Unit] = ??? //todo refresh when new tinkoff client

  def getAllAccounts(implicit F: Functor[F]): F[Map[AccountType, String]] =
    ref.get.map(_.getOrElse(Map.empty))
}

object InMemoryAccountsStorage {

  def apply[F[_]: InMemoryAccountsStorage]: InMemoryAccountsStorage[F] = implicitly[InMemoryAccountsStorage[F]]

  def make[F[_]: ConcurrentEffect]: F[InMemoryAccountsStorage[F]] =
    Ref.of(none[Map[AccountType, String]]).map { ref =>
      new InMemoryAccountsStorage[F](ref)
    }
}

final case class AccountNotFoundError(message: String) extends Throwable // todo need review it after adding switch tinkoff clients feature

object AccountTypeStorageSyntax {
  implicit class AccountTypeIdOps[F[_]: InMemoryAccountsStorage: Functor](accountType: AccountType) {
    def findId: OptionT[F, AccountId] = OptionT(InMemoryAccountsStorage[F].find(accountType)).map(AccountId)
  }
}
