/*
 * Copyright 2026 HM Revenue & Customs
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

import play.api.libs.json.{Format, JsError, JsResult, JsString, JsSuccess, JsValue, Json, OFormat, Reads, Writes}
import java.time.{Instant, LocalDate}

case class AdrAlcoholQuantity(
  litres: BigDecimal,
  lpa: BigDecimal
)

case object AdrAlcoholQuantity {
  implicit val adrAlcoholQuantityFormat: OFormat[AdrAlcoholQuantity] = Json.format[AdrAlcoholQuantity]
}

case class AdrDuty(
  taxCode: String,
  dutyRate: BigDecimal,
  dutyDue: BigDecimal
)

case object AdrDuty {
  implicit val adrDutyFormat: OFormat[AdrDuty] = Json.format[AdrDuty]
}

enum AdrDutySuspendedAlcoholRegime:
  case Beer, Cider, Wine, Spirits, OtherFermentedProduct

object AdrDutySuspendedAlcoholRegime {
  given Format[AdrDutySuspendedAlcoholRegime] = new Format[AdrDutySuspendedAlcoholRegime] {
    def reads(json: JsValue): JsResult[AdrDutySuspendedAlcoholRegime] = json match {
      case JsString("Beer")                  => JsSuccess(Beer)
      case JsString("Cider")                 => JsSuccess(Cider)
      case JsString("Wine")                  => JsSuccess(Wine)
      case JsString("Spirits")               => JsSuccess(Spirits)
      case JsString("OtherFermentedProduct") => JsSuccess(OtherFermentedProduct)
      case _                                 => JsError("Invalid Duty Suspended Alcohol Regime")
    }

    def writes(adrDutySuspendedAlcoholRegime: AdrDutySuspendedAlcoholRegime): JsValue = JsString(
      adrDutySuspendedAlcoholRegime.toString
    )
  }
}

enum AdrTypeOfSpirit:
  case Malt, Grain, NeutralAgricultural, NeutralIndustrial, Beer, CiderOrPerry, WineOrMadeWine, Other

object AdrTypeOfSpirit {
  given Format[AdrTypeOfSpirit] = new Format[AdrTypeOfSpirit] {
    def reads(json: JsValue): JsResult[AdrTypeOfSpirit] = json match {
      case JsString("Malt")                => JsSuccess(Malt)
      case JsString("Grain")               => JsSuccess(Grain)
      case JsString("NeutralAgricultural") => JsSuccess(NeutralAgricultural)
      case JsString("NeutralIndustrial")   => JsSuccess(NeutralIndustrial)
      case JsString("Beer")                => JsSuccess(Beer)
      case JsString("CiderOrPerry")        => JsSuccess(CiderOrPerry)
      case JsString("WineOrMadeWine")      => JsSuccess(WineOrMadeWine)
      case JsString("Other")               => JsSuccess(Other)
      case _                               => JsError("Invalid Type of Spirit")
    }

    def writes(adrTypeOfSpirit: AdrTypeOfSpirit): JsValue = JsString(adrTypeOfSpirit.toString)
  }

  def fromTypeOfSpiritType(typeOfSpiritType: TypeOfSpiritType): AdrTypeOfSpirit =
    typeOfSpiritType match {
      case TypeOfSpiritType.MaltSpirit                => Malt
      case TypeOfSpiritType.GrainSpirit               => Grain
      case TypeOfSpiritType.NeutralSpiritAgricultural => NeutralAgricultural
      case TypeOfSpiritType.NeutralSpiritIndustrial   => NeutralIndustrial
      case TypeOfSpiritType.BeerBased                 => Beer
      case TypeOfSpiritType.WineMadeWineBased         => WineOrMadeWine
      case TypeOfSpiritType.CiderPerryBased           => CiderOrPerry
      case TypeOfSpiritType.Other                     => Other
    }
}

case class AdrDutyDeclaredItem(
  quantityDeclared: AdrAlcoholQuantity,
  dutyDue: AdrDuty
)

case object AdrDutyDeclaredItem {
  implicit val adrDutyDeclaredItemFormat: OFormat[AdrDutyDeclaredItem] = Json.format[AdrDutyDeclaredItem]
}

case class AdrDutyDeclared(
  declared: Boolean,
  dutyDeclaredItems: Seq[AdrDutyDeclaredItem]
)

case object AdrDutyDeclared {
  implicit val adrDutyDeclaredFormat: OFormat[AdrDutyDeclared] = Json.format[AdrDutyDeclared]
}

case class AdrAdjustmentItem(
  returnPeriod: String,
  adjustmentQuantity: AdrAlcoholQuantity,
  dutyDue: AdrDuty
)

case object AdrAdjustmentItem {
  implicit val adrAdjustmentItemFormat: OFormat[AdrAdjustmentItem] = Json.format[AdrAdjustmentItem]
}

case class AdrRepackagedDraughtAdjustmentItem(
  returnPeriod: String,
  originalTaxCode: String,
  originalDutyRate: BigDecimal,
  newTaxCode: String,
  newDutyRate: BigDecimal,
  repackagedQuantity: AdrAlcoholQuantity,
  dutyAdjustment: BigDecimal
)

case object AdrRepackagedDraughtAdjustmentItem {
  implicit val adrRepackagedDraughtAdjustmentItemFormat: OFormat[AdrRepackagedDraughtAdjustmentItem] =
    Json.format[AdrRepackagedDraughtAdjustmentItem]
}

case class AdrAdjustments(
  overDeclarationDeclared: Boolean,
  reasonForOverDeclaration: Option[String],
  overDeclarationProducts: Seq[AdrAdjustmentItem],
  underDeclarationDeclared: Boolean,
  reasonForUnderDeclaration: Option[String],
  underDeclarationProducts: Seq[AdrAdjustmentItem],
  spoiltProductDeclared: Boolean,
  spoiltProducts: Seq[AdrAdjustmentItem],
  drawbackDeclared: Boolean,
  drawbackProducts: Seq[AdrAdjustmentItem],
  repackagedDraughtDeclared: Boolean,
  repackagedDraughtProducts: Seq[AdrRepackagedDraughtAdjustmentItem]
) {

  def hasAdjustments: Boolean =
    overDeclarationProducts.nonEmpty ||
      underDeclarationProducts.nonEmpty ||
      spoiltProducts.nonEmpty ||
      drawbackProducts.nonEmpty ||
      repackagedDraughtProducts.nonEmpty
}

case object AdrAdjustments {
  implicit val adrAdjustmentsFormat: OFormat[AdrAdjustments] =
    Json.format[AdrAdjustments]
}

case class AdrDutySuspendedProduct(
  regime: AdrDutySuspendedAlcoholRegime,
  suspendedQuantity: AdrAlcoholQuantity
)

case object AdrDutySuspendedProduct {
  implicit val adrDutySuspendedProductFormat: OFormat[AdrDutySuspendedProduct] = Json.format[AdrDutySuspendedProduct]
}

case class AdrDutySuspended(
  declared: Boolean,
  dutySuspendedProducts: Seq[AdrDutySuspendedProduct]
)

case object AdrDutySuspended {
  implicit val adrDutySuspendedFormat: OFormat[AdrDutySuspended] = Json.format[AdrDutySuspended]
}

case class AdrSpiritsVolumes(
  totalSpirits: BigDecimal,
  scotchWhisky: BigDecimal,
  irishWhiskey: BigDecimal
)

case object AdrSpiritsVolumes {
  implicit val adrSpiritsVolumesFormat: OFormat[AdrSpiritsVolumes] = Json.format[AdrSpiritsVolumes]
}

case class AdrSpiritsProduced(
  spiritsVolumes: AdrSpiritsVolumes,
  typesOfSpirit: Set[AdrTypeOfSpirit],
  otherSpiritTypeName: Option[String]
)

case object AdrSpiritsProduced {
  implicit val adrSpiritsProducedFormat: OFormat[AdrSpiritsProduced] = Json.format[AdrSpiritsProduced]
}

case class AdrSpirits(
  spiritsDeclared: Boolean,
  spiritsProduced: Option[AdrSpiritsProduced]
)

case object AdrSpirits {
  implicit val adrSpiritsFormat: OFormat[AdrSpirits] = Json.format[AdrSpirits]
}

case class AdrTotals(
  declaredDutyDue: BigDecimal,
  overDeclaration: BigDecimal,
  underDeclaration: BigDecimal,
  spoiltProduct: BigDecimal,
  drawback: BigDecimal,
  repackagedDraught: BigDecimal,
  totalDutyDue: BigDecimal
)

case object AdrTotals {
  implicit val adrTotalsFormat: OFormat[AdrTotals] = Json.format[AdrTotals]
}

case class AdrReturnSubmission(
  dutyDeclared: AdrDutyDeclared,
  adjustments: AdrAdjustments,
  dutySuspended: AdrDutySuspended,
  spirits: Option[AdrSpirits],
  totals: AdrTotals
)

case object AdrReturnSubmission {
  implicit val adrReturnSubmissionFormat: OFormat[AdrReturnSubmission] = Json.format[AdrReturnSubmission]
}

case class AdrReturnCreatedDetails(
  processingDate: Instant,
  amount: BigDecimal,
  chargeReference: Option[String],
  paymentDueDate: Option[LocalDate]
)

object AdrReturnCreatedDetails {
  implicit val returnCreatedDetailsWrites: OFormat[AdrReturnCreatedDetails] = Json.format[AdrReturnCreatedDetails]

  def fromReturnCreatedDetails(returnCreatedDetails: ReturnCreatedDetails): AdrReturnCreatedDetails =
    AdrReturnCreatedDetails(
      processingDate = returnCreatedDetails.processingDate,
      amount = returnCreatedDetails.amount,
      chargeReference = returnCreatedDetails.chargeReference,
      paymentDueDate = returnCreatedDetails.paymentDueDate
    )
}
