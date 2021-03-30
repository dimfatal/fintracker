package tcs.api.syntax

import io.circe.Decoder
import org.http4s.Method
import tcs.api.client.HttpClient
import tcs.api.models.TcsApi

//final class MethodSyntax[A <: TcsApi](private val api: A) extends AnyVal {
//
//  /**
//   * Sends the method to the Tinkoff service.
//   * It allows to execute Tinkoff methods having `TcsClient` in implicit scope.
//   */
//
//  def get[F[_], R: Decoder](params: Map[String, String])(implicit client: TcsClient[F]): F[R] =
//    client.execute[R](Method.GET, api.path, params)
//
//  def post[F[_], R: Decoder](params: Map[String, String])(implicit client: TcsClient[F]): F[R] =
//    client.execute[R](Method.POST, api.path, params)
//}
