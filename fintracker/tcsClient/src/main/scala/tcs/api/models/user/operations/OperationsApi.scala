package tcs.api.models.user.operations

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import org.http4s.Method
import tcs.api.client.HttpClient
import tcs.api.models.TcsApi
import tcs.domain.model.Operations
import tcs.domain.model.TcsResponse.decoderOperations
import tcs.domain.model.user.{Account}

final class OperationsApi[F[_]: HttpClient](private val account: Account) extends TcsApi {

  private def brokerAccountId: String = account.brokerAccountId

  override def path: String = "/operations"

  val tcsClient: HttpClient[F] = implicitly[HttpClient[F]]

  def operations(from: OffsetDateTime, to: OffsetDateTime, figi: String): F[Operations] = {
    val query = Map(
      "from"            -> from.format(DateTimeFormatter.ISO_DATE_TIME),
      "to"              -> to.format(DateTimeFormatter.ISO_DATE_TIME),
      "figi"            -> figi,
      "brokerAccountId" -> brokerAccountId
    )

    tcsClient.execute(Method.GET, path, query)
  }

}

object OperationsApi {
  def apply[F[_]: HttpClient](account: Account): OperationsApi[F] = new OperationsApi(account)
}
