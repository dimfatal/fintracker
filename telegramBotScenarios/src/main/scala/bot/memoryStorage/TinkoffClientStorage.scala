package bot.memoryStorage

import cats.Functor
import cats.effect.ConcurrentEffect
import cats.effect.concurrent.Ref
import cats.implicits.{ none, _ }
import tcs4sclient.api.client.TinkoffClient

trait TinkoffClientStorage[F[_]] {
  def get: F[Option[TinkoffClient[F]]]
  def save(c: TinkoffClient[F]): F[Unit]
}

object ClientStorage {
  def make[F[_]: ConcurrentEffect: Functor]: F[TinkoffClientStorage[F]] =
    Ref[F].of(none[TinkoffClient[F]]).map { ref =>
      new TinkoffClientStorage[F] {
        override def get: F[Option[TinkoffClient[F]]]   = ref.get
        override def save(c: TinkoffClient[F]): F[Unit] = ref.set(c.some)
      }
    }
}
