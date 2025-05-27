/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.alcoholdutyreturns.models.returns

import enumeratum.{Enum, EnumEntry}
import play.api.libs.json._

object TypeOfSpiritType extends Enum[TypeOfSpiritType] {
  val values = findValues

  case object MaltSpirit extends TypeOfSpiritType
  case object GrainSpirit extends TypeOfSpiritType
  case object NeutralSpiritAgricultural extends TypeOfSpiritType
  case object NeutralSpiritIndustrial extends TypeOfSpiritType
  case object BeerBased extends TypeOfSpiritType
  case object WineMadeWineBased extends TypeOfSpiritType
  case object CiderPerryBased extends TypeOfSpiritType
  case object Other extends TypeOfSpiritType

  implicit val typeOfSpiritTypeReads: Reads[TypeOfSpiritType] = {
    case JsString("01") => JsSuccess(MaltSpirit)
    case JsString("02") => JsSuccess(GrainSpirit)
    case JsString("03") => JsSuccess(NeutralSpiritAgricultural)
    case JsString("04") => JsSuccess(NeutralSpiritIndustrial)
    case JsString("05") => JsSuccess(BeerBased)
    case JsString("06") => JsSuccess(WineMadeWineBased)
    case JsString("07") => JsSuccess(CiderPerryBased)
    case JsString("08") => JsSuccess(Other)
    case s: JsString    => JsError(s"$s is not a valid TypeOfSpiritType")
    case v              => JsError(s"got $v was expecting a string representing a TypeOfSpiritType")
  }

  implicit val typeOfSpiritTypeWrites: Writes[TypeOfSpiritType] = {
    case MaltSpirit                => JsString("01")
    case GrainSpirit               => JsString("02")
    case NeutralSpiritAgricultural => JsString("03")
    case NeutralSpiritIndustrial   => JsString("04")
    case BeerBased                 => JsString("05")
    case WineMadeWineBased         => JsString("06")
    case CiderPerryBased           => JsString("07")
    case Other                     => JsString("08")
  }

  def fromAdrTypeOfSpirit(typeOfSpirit: AdrTypeOfSpirit): TypeOfSpiritType =
    typeOfSpirit match {
      case AdrTypeOfSpirit.Malt                => MaltSpirit
      case AdrTypeOfSpirit.Grain               => GrainSpirit
      case AdrTypeOfSpirit.NeutralAgricultural => NeutralSpiritAgricultural
      case AdrTypeOfSpirit.NeutralIndustrial   => NeutralSpiritIndustrial
      case AdrTypeOfSpirit.Beer                => BeerBased
      case AdrTypeOfSpirit.WineOrMadeWine      => WineMadeWineBased
      case AdrTypeOfSpirit.CiderOrPerry        => CiderPerryBased
      case AdrTypeOfSpirit.Other               => Other
    }
}

sealed trait TypeOfSpiritType extends EnumEntry
