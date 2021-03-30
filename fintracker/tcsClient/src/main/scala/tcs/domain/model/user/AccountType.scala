package tcs.domain.model
package user

import cats.implicits.catsSyntaxEitherId
import io.circe.{ Decoder, DecodingFailure, HCursor }
import io.circe.generic.semiauto.deriveDecoder

sealed trait AccountType
case object Tinkoff    extends AccountType
case object TinkoffIIS extends AccountType


final case class Account(brokerAccountId: String, brokerAccountType: AccountType)


final case class Accounts(accounts: List[Account])

object AccountsDecoder {

  implicit val accountsDecoder: Decoder[Accounts] = deriveDecoder[Accounts] //def ??

  implicit val accountDecoder: Decoder[Account]          = deriveDecoder[Account]
  implicit val accountTypesDecoder: Decoder[AccountType] = (cursor: HCursor) =>
    for {
      value  <- cursor.as[String]
      result <- value match {
                  case "Tinkoff"    => Tinkoff.asRight
                  case "TinkoffIis" => TinkoffIIS.asRight
                  case s            => DecodingFailure(s"Invalid house type ${s}", cursor.history).asLeft
                }
    } yield result

}

//object Account {
//
//  /**
//   * Decodes chat based on the `type` value of the input Json
//   */
//  implicit val accountDecoder: Decoder[Account] = Decoder.instance[Account] { cursor =>
//    cursor
//      .get[AccountType]("brokerAccountType")
//      .map {
//        case AccountType.Tinkoff => deriveDecoder[Tinkoff]
//        case AccountType.TinkoffIis => deriveDecoder[TinkoffIis]
//
//      }
//      .flatMap(_.tryDecode(cursor))
//  }
//}
//
//final case class Tinkoff(brokerAccountId: String) extends Account
//
//final case class TinkoffIis(brokerAccountId: String) extends Account
//
//object AccountType extends Enumeration {
//  type AccountType = Value
//  val Tinkoff, TinkoffIis = Value
//
//  implicit val accountTypeDecoder: Decoder[AccountType] = Decoder[String].map(item => AccountType.withName(item))
//}
