package tcs.api.models
package user.portfolio

import tcs.api.client.HttpClient
import tcs.domain.model.user.Account
import tcs.domain.model.Positions
import org.http4s.Method
import tcs.domain.model.TcsResponse.decoderPositions

final class UserPortfolioApi[F[_]: HttpClient](private val account: Account) extends TcsApi {
  private val brokerAccountId: String = account.brokerAccountId

  override def path: String = "/portfolio"

  val tcsClient: HttpClient[F] = implicitly[HttpClient[F]]

  def positions: F[Positions] =
    tcsClient.execute(Method.GET, path, Map("brokerAccountId" -> brokerAccountId))
}

object UserPortfolioApi {
  def apply[F[_]: HttpClient](account: Account): UserPortfolioApi[F] = new UserPortfolioApi(account)

}
