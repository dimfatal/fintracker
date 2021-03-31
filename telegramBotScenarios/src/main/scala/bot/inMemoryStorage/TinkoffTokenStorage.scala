package bot.inMemoryStorage

import cats.implicits._
import cats.Functor
import cats.effect.ConcurrentEffect
import cats.effect.concurrent.Ref

trait TinkoffTokenStorage[F[_]] {
  def get: F[String]
  def save(c: String): F[Unit]
  def clear: F[Unit]
}

object TokenStorage {
  def make[F[_]: ConcurrentEffect: Functor]: F[TinkoffTokenStorage[F]] =
    Ref[F].of("").map { ref =>
      new TinkoffTokenStorage[F] {
        override def get: F[String]           = ref.get
        override def save(c: String): F[Unit] = ref.set(c)

        override def clear: F[Unit] = ???
      }
    }
}
