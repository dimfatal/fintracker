package tcsInterpreters

import cats.Functor
import cats.effect.ConcurrentEffect
import cats.effect.concurrent.Ref
import cats.implicits._
import tcs4sclient.model.domain.user.AccountType

trait AccountsStorage[F[_]] {
  def get(accountType: AccountType)(implicit f: Functor[F]): F[String]
  def save(accounts: Map[AccountType, String]): F[Unit]
  def clear(): F[Unit]
}

class InMemoryAccountsStorage[F[_]](private val ref: Ref[F, Option[Map[AccountType, String]]]) extends AccountsStorage[F] {

  override def get(accountType: AccountType)(implicit F: Functor[F]): F[String] =
    getAllAccounts.map(_.getOrElse(accountType, ""))

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

//todo move it from here
final case class AccountId(id: String) extends AnyVal

final case class AccountNotFoundError(message: String) extends Throwable // todo need review it after adding switch tinkoff clients feature

object AccountTypeStorageSyntax {
  implicit class AccountTypeIdOps[F[_]: InMemoryAccountsStorage: Functor](accountType: AccountType) {
    def findId: F[Either[AccountNotFoundError, AccountId]] = InMemoryAccountsStorage[F].get(accountType).map { a =>
      Either.cond(a != "", AccountId(a), AccountNotFoundError(s"account with type $accountType not found "))
    }

    def id: F[AccountId] = InMemoryAccountsStorage[F].get(accountType).map(AccountId)
  }
}
