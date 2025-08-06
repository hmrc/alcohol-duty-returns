/*
 * Copyright 2024 HM Revenue & Customs
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

import play.api.libs.json.{Json, OFormat}

import java.time.Instant

/**
  * For showing an individual return
  */
case class AdrReturnDetails(
  identification: AdrReturnDetailsIdentification,
  alcoholDeclared: AdrReturnAlcoholDeclared,
  adjustments: AdrReturnAdjustments,
  totalDutyDue: AdrReturnTotalDutyDue,
  netDutySuspension: Option[AdrReturnNetDutySuspension],
  spirits: Option[AdrReturnSpirits] // Will be None on non-spirits months or if none declared
)

object AdrReturnDetails {
  def fromGetReturnDetails(returnDetailsSuccess: GetReturnDetails): AdrReturnDetails =
    AdrReturnDetails(
      identification = AdrReturnDetailsIdentification.fromReturnDetailsSuccess(returnDetailsSuccess),
      alcoholDeclared = AdrReturnAlcoholDeclared.fromReturnDetailsSuccess(returnDetailsSuccess),
      adjustments = AdrReturnAdjustments.fromReturnDetailsSuccess(returnDetailsSuccess),
      totalDutyDue = AdrReturnTotalDutyDue.fromReturnDetailsSuccess(returnDetailsSuccess),
      netDutySuspension = AdrReturnNetDutySuspension.fromReturnDetailsSuccess(returnDetailsSuccess),
      spirits = AdrReturnSpirits.fromReturnDetailsSuccess(returnDetailsSuccess)
    )

  implicit val adrReturnDetailsFormat: OFormat[AdrReturnDetails] = Json.format[AdrReturnDetails]
}

case class AdrReturnDetailsIdentification(periodKey: String, chargeReference: Option[String], submittedTime: Instant)

object AdrReturnDetailsIdentification {
  def fromReturnDetailsSuccess(returnDetailsSuccess: GetReturnDetails): AdrReturnDetailsIdentification =
    AdrReturnDetailsIdentification(
      periodKey = returnDetailsSuccess.chargeDetails.periodKey,
      chargeReference = returnDetailsSuccess.chargeDetails.chargeReference,
      submittedTime = returnDetailsSuccess.chargeDetails.receiptDate
    )

  implicit val adrReturnDetailsIdentificationFormat: OFormat[AdrReturnDetailsIdentification] =
    Json.format[AdrReturnDetailsIdentification]
}

case class AdrReturnAlcoholDeclared(alcoholDeclaredDetails: Option[Seq[AdrReturnAlcoholDeclaredRow]], total: BigDecimal)

object AdrReturnAlcoholDeclared {
  def fromReturnDetailsSuccess(returnDetailsSuccess: GetReturnDetails): AdrReturnAlcoholDeclared =
    AdrReturnAlcoholDeclared(
      returnDetailsSuccess.alcoholProducts.regularReturn.flatMap(items =>
        if (items.isEmpty) {
          None
        } else {
          Some(
            items.map(
              AdrReturnAlcoholDeclaredRow.fromRegularReturnDetails
            )
          )
        }
      ),
      total = returnDetailsSuccess.totalDutyDue.totalDutyDueAlcoholProducts
    )

  implicit val adrReturnAlcoholDeclaredFormat: OFormat[AdrReturnAlcoholDeclared] = Json.format[AdrReturnAlcoholDeclared]
}

case class AdrReturnAlcoholDeclaredRow(
  taxType: String,
  litresOfPureAlcohol: BigDecimal,
  dutyRate: BigDecimal,
  dutyValue: BigDecimal
)

object AdrReturnAlcoholDeclaredRow {
  def fromRegularReturnDetails(regularReturnDetails: RegularReturnDetails): AdrReturnAlcoholDeclaredRow =
    AdrReturnAlcoholDeclaredRow(
      taxType = regularReturnDetails.taxType,
      litresOfPureAlcohol = regularReturnDetails.litresOfPureAlcohol,
      dutyRate = regularReturnDetails.dutyRate,
      dutyValue = regularReturnDetails.dutyDue
    )

  implicit val adrReturnAlcoholDeclaredRowFormat: OFormat[AdrReturnAlcoholDeclaredRow] =
    Json.format[AdrReturnAlcoholDeclaredRow]
}

case class AdrReturnAdjustments(adjustmentDetails: Option[Seq[AdrReturnAdjustmentsRow]], total: BigDecimal)

object AdrReturnAdjustments {
  val underDeclaredKey     = "underdeclaration"
  val overDeclaredKey      = "overdeclaration"
  val repackagedDraughtKey = "repackagedDraught"
  val spoiltKey            = "spoilt"
  val drawbackKey          = "drawback"

  val isOwedToHmrcMap: Map[String, Boolean] =
    Map(
      underDeclaredKey     -> true,
      overDeclaredKey      -> false,
      repackagedDraughtKey -> true,
      spoiltKey            -> false,
      drawbackKey          -> false
    )

  def isOwedToHmrc(key: String) = isOwedToHmrcMap.getOrElse(
    key,
    throw new IllegalArgumentException("Bad adjustment key when checking if amount is owed to HMRC")
  )

  def fromReturnDetailsSuccess(returnDetailsSuccess: GetReturnDetails): AdrReturnAdjustments = {
    val allAdjustmentRows = Seq(
      returnDetailsSuccess.underDeclaration.underDeclarationProducts
        .map(
          _.map(
            AdrReturnAdjustmentsRow.fromReturnDetails(underDeclaredKey, _)
          )
        )
        .getOrElse(Seq.empty),
      returnDetailsSuccess.overDeclaration.overDeclarationProducts
        .map(
          _.map(
            AdrReturnAdjustmentsRow.fromReturnDetails(overDeclaredKey, _)
          )
        )
        .getOrElse(Seq.empty),
      returnDetailsSuccess.repackagedDraught.repackagedDraughtProducts
        .map(
          _.map(rpd =>
            AdrReturnAdjustmentsRow.fromReturnDetails(repackagedDraughtKey, rpd.toReturnDetailsForAdjustment())
          )
        )
        .getOrElse(Seq.empty),
      returnDetailsSuccess.spoiltProduct.spoiltProductProducts
        .map(
          _.map(
            AdrReturnAdjustmentsRow.fromReturnDetails(spoiltKey, _)
          )
        )
        .getOrElse(Seq.empty),
      returnDetailsSuccess.drawback.drawbackProducts
        .map(
          _.map(
            AdrReturnAdjustmentsRow.fromReturnDetails(drawbackKey, _)
          )
        )
        .getOrElse(Seq.empty)
    ).flatten

    AdrReturnAdjustments(
      adjustmentDetails = if (allAdjustmentRows.isEmpty) None else Some(allAdjustmentRows),
      total = returnDetailsSuccess.totalDutyDue.totalDutyUnderDeclaration -
        returnDetailsSuccess.totalDutyDue.totalDutyOverDeclaration +
        returnDetailsSuccess.totalDutyDue.totalDutyRepDraughtProducts -
        returnDetailsSuccess.totalDutyDue.totalDutySpoiltProduct -
        returnDetailsSuccess.totalDutyDue.totalDutyDrawback
    )
  }

  implicit val adrReturnAdjustmentsFormat: OFormat[AdrReturnAdjustments] = Json.format[AdrReturnAdjustments]
}

case class AdrReturnAdjustmentsRow(
  adjustmentTypeKey: String,
  returnPeriodAffected: Option[String],
  taxType: String,
  litresOfPureAlcohol: BigDecimal,
  dutyRate: Option[BigDecimal],
  dutyValue: BigDecimal
)

object AdrReturnAdjustmentsRow {
  def fromReturnDetails(key: String, returnDetails: ReturnDetails): AdrReturnAdjustmentsRow =
    AdrReturnAdjustmentsRow(
      adjustmentTypeKey = key,
      returnPeriodAffected = returnDetails.returnPeriodAffected,
      taxType = returnDetails.taxType,
      litresOfPureAlcohol = returnDetails.litresOfPureAlcohol,
      dutyRate = returnDetails.dutyRate,
      dutyValue = if (AdrReturnAdjustments.isOwedToHmrc(key)) returnDetails.dutyDue else -returnDetails.dutyDue
    )

  implicit val adrReturnAdjustmentsRowFormat: OFormat[AdrReturnAdjustmentsRow] = Json.format[AdrReturnAdjustmentsRow]
}

case class AdrReturnTotalDutyDue(totalDue: BigDecimal)

object AdrReturnTotalDutyDue {
  def fromReturnDetailsSuccess(returnDetailsSuccess: GetReturnDetails): AdrReturnTotalDutyDue =
    AdrReturnTotalDutyDue(
      totalDue = returnDetailsSuccess.totalDutyDue.totalDutyDue
    )

  implicit val adrReturnTotalDutyDueFormat: OFormat[AdrReturnTotalDutyDue] = Json.format[AdrReturnTotalDutyDue]
}

case class AdrReturnNetDutySuspension(
  totalLtsBeer: Option[BigDecimal],
  totalLtsWine: Option[BigDecimal],
  totalLtsCider: Option[BigDecimal],
  totalLtsSpirit: Option[BigDecimal],
  totalLtsOtherFermented: Option[BigDecimal],
  totalLtsPureAlcoholBeer: Option[BigDecimal],
  totalLtsPureAlcoholWine: Option[BigDecimal],
  totalLtsPureAlcoholCider: Option[BigDecimal],
  totalLtsPureAlcoholSpirit: Option[BigDecimal],
  totalLtsPureAlcoholOtherFermented: Option[BigDecimal]
)

object AdrReturnNetDutySuspension {
  def fromReturnDetailsSuccess(returnDetailsSuccess: GetReturnDetails): Option[AdrReturnNetDutySuspension] =
    if (returnDetailsSuccess.netDutySuspension.netDutySuspensionFilled) {
      returnDetailsSuccess.netDutySuspension.netDutySuspensionProducts.map(dutySuspension =>
        AdrReturnNetDutySuspension(
          totalLtsBeer = dutySuspension.totalLtsBeer,
          totalLtsWine = dutySuspension.totalLtsWine,
          totalLtsCider = dutySuspension.totalLtsCider,
          totalLtsSpirit = dutySuspension.totalLtsSpirit,
          totalLtsOtherFermented = dutySuspension.totalLtsOtherFermented,
          totalLtsPureAlcoholBeer = dutySuspension.totalLtsPureAlcoholBeer,
          totalLtsPureAlcoholWine = dutySuspension.totalLtsPureAlcoholWine,
          totalLtsPureAlcoholCider = dutySuspension.totalLtsPureAlcoholCider,
          totalLtsPureAlcoholSpirit = dutySuspension.totalLtsPureAlcoholSpirit,
          totalLtsPureAlcoholOtherFermented = dutySuspension.totalLtsPureAlcoholOtherFermented
        )
      )
    } else {
      None
    }

  implicit val adrReturnNetDutySuspensionFormat: OFormat[AdrReturnNetDutySuspension] =
    Json.format[AdrReturnNetDutySuspension]
}

case class AdrReturnSpiritsVolumes(
  totalSpirits: BigDecimal,
  scotchWhisky: BigDecimal,
  irishWhiskey: BigDecimal
)

case object AdrReturnSpiritsVolumes {
  def fromSpiritsProducedDetails(spiritsProducedDetails: SpiritsProducedDetails): AdrReturnSpiritsVolumes =
    // Note the spelling of Whisk(e)y in the downstream API is the wrong way around
    AdrReturnSpiritsVolumes(
      totalSpirits = spiritsProducedDetails.totalSpirits,
      scotchWhisky = spiritsProducedDetails.scotchWhiskey,
      irishWhiskey = spiritsProducedDetails.irishWhisky
    )

  implicit val adrReturnSpiritsVolumesFormat: OFormat[AdrReturnSpiritsVolumes] = Json.format[AdrReturnSpiritsVolumes]
}

case class AdrReturnSpirits(
  spiritsVolumes: AdrReturnSpiritsVolumes,
  typesOfSpirit: Set[AdrTypeOfSpirit],
  otherSpiritTypeName: Option[String]
)

case object AdrReturnSpirits {
  def fromReturnDetailsSuccess(returnDetailsSuccess: GetReturnDetails): Option[AdrReturnSpirits] =
    returnDetailsSuccess.spiritsProduced
      .flatMap(_.spiritsProduced)
      .map(spiritsProducedDetails =>
        AdrReturnSpirits(
          spiritsVolumes = AdrReturnSpiritsVolumes.fromSpiritsProducedDetails(spiritsProducedDetails),
          typesOfSpirit = spiritsProducedDetails.typeOfSpirit.map(AdrTypeOfSpirit.fromTypeOfSpiritType),
          otherSpiritTypeName = spiritsProducedDetails.typeOfSpiritOther
        )
      )

  implicit val adrReturnSpiritsFormat: OFormat[AdrReturnSpirits] = Json.format[AdrReturnSpirits]
}
