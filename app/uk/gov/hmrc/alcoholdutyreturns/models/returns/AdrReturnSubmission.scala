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

import cats.implicits.catsSyntaxSemigroup
import enumeratum.{Enum, EnumEntry, PlayJsonEnum}
import play.api.libs.json.{Json, OFormat, OWrites}
import uk.gov.hmrc.alcoholdutyreturns.models.ReturnPeriod
import uk.gov.hmrc.alcoholdutyreturns.models.validation.{ValidationResult, Validations, noValidationResult}

import java.time.{Instant, LocalDate}

case class AdrAlcoholQuantity(
  litres: BigDecimal,
  lpa: BigDecimal
) {
  def validate(fieldPrefix: String): ValidationResult =
    Validations.validateGreaterThanOrEqualTo(litres, lpa, s"$fieldPrefix.litres", s"$fieldPrefix.lpa") |+|
      Validations.validatePositive(litres, s"$fieldPrefix.litres") |+|
      Validations.validatePositive(lpa, s"$fieldPrefix.lpa")
}

case object AdrAlcoholQuantity {
  implicit val adrAlcoholQuantityFormat: OFormat[AdrAlcoholQuantity] = Json.format[AdrAlcoholQuantity]
}

case class AdrDuty(
  taxCode: String,
  dutyRate: BigDecimal,
  dutyDue: BigDecimal
) {
  def validate(fieldPrefix: String, dutyDueIsOwedNotRefunded: Boolean): ValidationResult =
    Validations.validateTaxCode(taxCode, s"$fieldPrefix.declaredDutyDue") |+|
      Validations.validateNonNegative(dutyRate, s"$fieldPrefix.dutyRate") |+| {
        if (dutyDueIsOwedNotRefunded) {
          Validations.validateNonNegative(dutyDue, s"$fieldPrefix.dutyDue")
        } else {
          Validations.validateNonPositive(dutyDue, s"$fieldPrefix.dutyDue")
        }
      }
}

case object AdrDuty {
  implicit val adrDutyFormat: OFormat[AdrDuty] = Json.format[AdrDuty]
}

sealed trait AdrDutySuspendedAlcoholRegime extends EnumEntry

object AdrDutySuspendedAlcoholRegime
    extends Enum[AdrDutySuspendedAlcoholRegime]
    with PlayJsonEnum[AdrDutySuspendedAlcoholRegime] {
  val values = findValues

  case object Beer extends AdrDutySuspendedAlcoholRegime
  case object Cider extends AdrDutySuspendedAlcoholRegime
  case object Wine extends AdrDutySuspendedAlcoholRegime
  case object Spirits extends AdrDutySuspendedAlcoholRegime
  case object OtherFermentedProduct extends AdrDutySuspendedAlcoholRegime
}

sealed trait AdrTypeOfSpirit extends EnumEntry

object AdrTypeOfSpirit extends Enum[AdrTypeOfSpirit] with PlayJsonEnum[AdrTypeOfSpirit] {
  val values = findValues

  case object Malt extends AdrTypeOfSpirit
  case object Grain extends AdrTypeOfSpirit
  case object NeutralAgricultural extends AdrTypeOfSpirit
  case object NeutralIndustrial extends AdrTypeOfSpirit
  case object Beer extends AdrTypeOfSpirit
  case object CiderOrPerry extends AdrTypeOfSpirit
  case object WineOrMadeWine extends AdrTypeOfSpirit
  case object Other extends AdrTypeOfSpirit
}

sealed trait AdrUnitOfMeasure extends EnumEntry

object AdrUnitOfMeasure extends Enum[AdrUnitOfMeasure] with PlayJsonEnum[AdrUnitOfMeasure] {
  val values = findValues

  case object Tonnes extends AdrUnitOfMeasure
  case object Litres extends AdrUnitOfMeasure
}

case class AdrDutyDeclaredItem(
  quantityDeclared: AdrAlcoholQuantity,
  dutyDue: AdrDuty
) {
  def validate(index: Int): ValidationResult =
    quantityDeclared.validate(s"dutyDeclared.dutyDeclaredItems[$index]") |+|
      dutyDue.validate(s"dutyDeclared.dutyDeclaredItems[$index]", dutyDueIsOwedNotRefunded = true)
}

case object AdrDutyDeclaredItem {
  implicit val adrDutyDeclaredItemFormat: OFormat[AdrDutyDeclaredItem] = Json.format[AdrDutyDeclaredItem]
}

case class AdrDutyDeclared(
  declared: Boolean,
  dutyDeclaredItems: Seq[AdrDutyDeclaredItem]
) {
  def validate(): ValidationResult =
    (if (declared) {
       Validations.validateItemsExpected(dutyDeclaredItems, "dutyDeclared")
     } else {
       Validations.validateNoItemsExpected(dutyDeclaredItems, "dutyDeclared")
     }) |+|
      dutyDeclaredItems.zipWithIndex.foldLeft(noValidationResult) { case (result, (item, index)) =>
        result |+| item.validate(index)
      }
}

case object AdrDutyDeclared {
  implicit val adrDutyDeclaredFormat: OFormat[AdrDutyDeclared] = Json.format[AdrDutyDeclared]
}

case class AdrAdjustmentItem(
  returnPeriod: ReturnPeriod,
  adjustmentQuantity: AdrAlcoholQuantity,
  dutyDue: AdrDuty
) {
  def validate(arrayField: String, index: Int, dutyDueIsOwedNotRefunded: Boolean): ValidationResult =
    adjustmentQuantity.validate(s"$arrayField[$index]") |+|
      dutyDue.validate(s"$arrayField[$index]", dutyDueIsOwedNotRefunded)
}

case object AdrAdjustmentItem {
  implicit val adrAdjustmentItemFormat: OFormat[AdrAdjustmentItem] = Json.format[AdrAdjustmentItem]
}

case class AdrRepackagedDraughtAdjustmentItem(
  returnPeriod: ReturnPeriod,
  originalTaxCode: String,
  originalDutyRate: BigDecimal,
  newTaxCode: String,
  newDutyRate: BigDecimal,
  repackagedQuantity: AdrAlcoholQuantity,
  dutyAdjustment: BigDecimal
) {
  def validate(index: Int): ValidationResult =
    Validations.validateTaxCode(originalTaxCode, s"adjustments.repackagedDraughtProducts[$index].originalTaxCode") |+|
      Validations.validateNonNegative(
        originalDutyRate,
        s"adjustments.repackagedDraughtProducts[$index].originalDutyRate"
      ) |+|
      Validations.validateTaxCode(newTaxCode, s"adjustments.repackagedDraughtProducts[$index].newTaxCode") |+|
      Validations.validateNonNegative(newDutyRate, s"adjustments.repackagedDraughtProducts[$index].newDutyRate") |+|
      repackagedQuantity.validate(s"adjustments.repackagedDraughtProducts[$index]")
}

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
  def validate(): ValidationResult =
    (if (overDeclarationDeclared) {
       Validations.validateItemsExpected(overDeclarationProducts, "adjustments.overDeclarationProducts") |+|
         overDeclarationProducts.zipWithIndex.foldLeft(noValidationResult) { case (result, (item, index)) =>
           result |+| item.validate("adjustments.overDeclarationProducts", index, dutyDueIsOwedNotRefunded = false)
         }
     } else {
       Validations.validateFieldNotExpected(reasonForOverDeclaration, "adjustments.reasonForOverDeclaration") |+|
         Validations.validateNoItemsExpected(overDeclarationProducts, "adjustments.overDeclarationProducts")
     }) |+|
      (if (underDeclarationDeclared) {
         Validations.validateItemsExpected(underDeclarationProducts, "adjustments.underDeclarationProducts") |+|
           underDeclarationProducts.zipWithIndex.foldLeft(noValidationResult) { case (result, (item, index)) =>
             result |+| item.validate("adjustments.underDeclarationProducts", index, dutyDueIsOwedNotRefunded = true)
           }
       } else {
         Validations.validateFieldNotExpected(reasonForUnderDeclaration, "adjustments.reasonForUnderDeclaration") |+|
           Validations.validateNoItemsExpected(underDeclarationProducts, "adjustments.underDeclarationProducts")
       }) |+|
      (if (spoiltProductDeclared) {
         Validations.validateItemsExpected(spoiltProducts, "adjustments.spoiltProducts") |+|
           spoiltProducts.zipWithIndex.foldLeft(noValidationResult) { case (result, (item, index)) =>
             result |+| item.validate("adjustments.spoiltProducts", index, dutyDueIsOwedNotRefunded = false)
           }
       } else {
         Validations.validateNoItemsExpected(spoiltProducts, "adjustments.spoiltProducts")
       }) |+|
      (if (drawbackDeclared) {
         Validations.validateItemsExpected(drawbackProducts, "adjustments.drawbackProducts") |+|
           drawbackProducts.zipWithIndex.foldLeft(noValidationResult) { case (result, (item, index)) =>
             result |+| item.validate("adjustments.drawbackProducts", index, dutyDueIsOwedNotRefunded = false)
           }
       } else {
         Validations.validateNoItemsExpected(drawbackProducts, "adjustments.drawbackProducts")
       }) |+|
      (if (repackagedDraughtDeclared) {
         Validations.validateItemsExpected(repackagedDraughtProducts, "adjustments.repackagedDraughtProducts") |+|
           repackagedDraughtProducts.zipWithIndex.foldLeft(noValidationResult) { case (result, (item, index)) =>
             result |+| item.validate(index)
           }
       } else {
         Validations.validateNoItemsExpected(repackagedDraughtProducts, "adjustments.repackagedDraughtProducts")
       })

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
) {
  def validate(index: Int): ValidationResult =
    suspendedQuantity.validate(s"dutySuspended.dutySuspendedProducts[$index]")
}

case object AdrDutySuspendedProduct {
  implicit val adrDutySuspendedProductFormat: OFormat[AdrDutySuspendedProduct] = Json.format[AdrDutySuspendedProduct]
}

case class AdrDutySuspended(
  declared: Boolean,
  dutySuspendedProducts: Seq[AdrDutySuspendedProduct]
) {
  def validate(): ValidationResult =
    if (declared) {
      Validations.validateItemsExpected(dutySuspendedProducts, "dutySuspended.dutySuspendedProducts") |+|
        dutySuspendedProducts.zipWithIndex.foldLeft(noValidationResult) { case (result, (item, index)) =>
          result |+| item.validate(index)
        }
    } else {
      Validations.validateNoItemsExpected(dutySuspendedProducts, "dutySuspended.dutySuspendedProducts")
    }
}

case object AdrDutySuspended {
  implicit val adrDutySuspendedFormat: OFormat[AdrDutySuspended] = Json.format[AdrDutySuspended]
}

case class AdrSpiritsVolumes(
  totalSpirits: BigDecimal,
  scotchWhiskey: BigDecimal,
  irishWhisky: BigDecimal
) {
  def validate(): ValidationResult =
    Validations.validatePositive(totalSpirits, "spirits.spiritsProduced.spiritsVolumes.totalSpirits") |+|
      Validations.validateNonNegative(scotchWhiskey, "spirits.spiritsProduced.spiritsVolumes.scotchWhiskey") |+|
      Validations.validateNonNegative(irishWhisky, "spirits.spiritsProduced.spiritsVolumes.irishWhisky")
}

case object AdrSpiritsVolumes {
  implicit val adrSpiritsVolumesFormat: OFormat[AdrSpiritsVolumes] = Json.format[AdrSpiritsVolumes]
}

case class AdrSpiritsGrainsQuantities(
  maltedBarley: Option[BigDecimal],
  otherMaltedGrain: Option[BigDecimal],
  wheat: Option[BigDecimal],
  maize: Option[BigDecimal],
  rye: Option[BigDecimal],
  unmaltedGrain: Option[BigDecimal]
) {
  def validate(): ValidationResult =
    maltedBarley.fold(noValidationResult)(
      Validations.validatePositive(_, "spirits.spiritsProduced.spiritsVolumes.grainsQuantities.maltedBarley")
    ) |+|
      otherMaltedGrain.fold(noValidationResult)(
        Validations.validatePositive(_, "spirits.spiritsProduced.spiritsVolumes.grainsQuantities.otherMaltedGrain")
      ) |+|
      wheat.fold(noValidationResult)(
        Validations.validatePositive(_, "spirits.spiritsProduced.spiritsVolumes.grainsQuantities.wheat")
      ) |+|
      maize.fold(noValidationResult)(
        Validations.validatePositive(_, "spirits.spiritsProduced.spiritsVolumes.grainsQuantities.maize")
      ) |+|
      rye.fold(noValidationResult)(
        Validations.validatePositive(_, "spirits.spiritsProduced.spiritsVolumes.grainsQuantities.rye")
      ) |+|
      unmaltedGrain.fold(noValidationResult)(
        Validations.validatePositive(_, "spirits.spiritsProduced.spiritsVolumes.grainsQuantities.unmaltedGrain")
      )
}

case object AdrSpiritsGrainsQuantities {
  implicit val adrSpiritsGrainsQuantitiesFormat: OFormat[AdrSpiritsGrainsQuantities] =
    Json.format[AdrSpiritsGrainsQuantities]
}

case class AdrSpiritsIngredientsVolumes(
  ethylene: Option[BigDecimal],
  molasses: Option[BigDecimal],
  beer: Option[BigDecimal],
  wine: Option[BigDecimal],
  madeWine: Option[BigDecimal],
  ciderOrPerry: Option[BigDecimal]
) {
  def validate(): ValidationResult =
    ethylene.fold(noValidationResult)(
      Validations.validatePositive(_, "spirits.spiritsProduced.spiritsVolumes.ingredientsVolumes.ethylene")
    ) |+|
      molasses.fold(noValidationResult)(
        Validations.validatePositive(_, "spirits.spiritsProduced.spiritsVolumes.ingredientsVolumes.molasses")
      ) |+|
      beer.fold(noValidationResult)(
        Validations.validatePositive(_, "spirits.spiritsProduced.spiritsVolumes.ingredientsVolumes.beer")
      ) |+|
      wine.fold(noValidationResult)(
        Validations.validatePositive(_, "spirits.spiritsProduced.spiritsVolumes.ingredientsVolumes.wine")
      ) |+|
      madeWine.fold(noValidationResult)(
        Validations.validatePositive(_, "spirits.spiritsProduced.spiritsVolumes.ingredientsVolumes.madeWine")
      ) |+|
      ciderOrPerry.fold(noValidationResult)(
        Validations.validatePositive(_, "spirits.spiritsProduced.spiritsVolumes.ingredientsVolumes.ciderOrPerry")
      )
}

case object AdrSpiritsIngredientsVolumes {
  implicit val adrSpiritsIngredientsVolumesFormat: OFormat[AdrSpiritsIngredientsVolumes] =
    Json.format[AdrSpiritsIngredientsVolumes]
}

case class AdrOtherIngredient(
  quantity: BigDecimal,
  unitOfMeasure: AdrUnitOfMeasure,
  ingredientName: String
) {
  def validate(): ValidationResult =
    Validations.validatePositive(quantity, "spirits.spiritsProduced.spiritsVolumes.otherIngredient.quantity") |+|
      Validations.validateStringNonEmpty(
        ingredientName,
        "spirits.spiritsProduced.spiritsVolumes.otherIngredient.ingredientName"
      )
}

case object AdrOtherIngredient {
  implicit val adrOtherIngredientFormat: OFormat[AdrOtherIngredient] = Json.format[AdrOtherIngredient]
}

case class AdrSpiritsProduced(
  spiritsVolumes: AdrSpiritsVolumes,
  typesOfSpirit: Set[AdrTypeOfSpirit],
  otherSpiritTypeName: Option[String],
  hasOtherMaltedGrain: Boolean,
  grainsQuantities: AdrSpiritsGrainsQuantities,
  otherMaltedGrainType: Option[String],
  ingredientsVolumes: AdrSpiritsIngredientsVolumes,
  otherIngredient: Option[AdrOtherIngredient]
) {
  def validate(): ValidationResult =
    spiritsVolumes.validate() |+|
      Validations.validateItemsExpected(typesOfSpirit, "spirits.spiritsProduced.typesOfSpirit") |+|
      (if (typesOfSpirit.contains(AdrTypeOfSpirit.Other)) {
         Validations.validateFieldExpected(otherSpiritTypeName, "spirits.spiritsProduced.otherSpiritTypeName")
       } else {
         Validations.validateFieldNotExpected(otherSpiritTypeName, "spirits.spiritsProduced.otherSpiritTypeName")
       }) |+|
      grainsQuantities.validate() |+|
      (if (hasOtherMaltedGrain) {
         Validations.validateFieldExpected(otherMaltedGrainType, "spirits.spiritsProduced.otherMaltedGrainType")
       } else {
         Validations.validateFieldNotExpected(otherMaltedGrainType, "spirits.spiritsProduced.otherMaltedGrainType")
       }) |+|
      ingredientsVolumes.validate() |+|
      otherIngredient.fold(noValidationResult)(_.validate())
}

case object AdrSpiritsProduced {
  implicit val adrSpiritsProducedFormat: OFormat[AdrSpiritsProduced] = Json.format[AdrSpiritsProduced]
}

case class AdrSpirits(
  spiritsDeclared: Boolean,
  spiritsProduced: Option[AdrSpiritsProduced]
) {
  def validate(returnPeriod: ReturnPeriod): ValidationResult =
    if (spiritsDeclared) {
      Validations.validateSpiritsQuarter(returnPeriod, "spirits.spiritsProduced") |+|
        Validations.validateFieldExpected(spiritsProduced, "spirits.spiritsProduced") |+|
        spiritsProduced.fold(noValidationResult)(_.validate())
    } else {
      Validations.validateFieldNotExpected(spiritsProduced, "spirits.spiritsProduced")
    }
}

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
) {
  // Could do some additional checks here that values are 0/non-zero when declared in the appropriate section
  def validate(
    wasDutyDeclared: Boolean,
    wasOverDeclaration: Boolean,
    wasUnderDeclaration: Boolean,
    wasSpoiltDeclaration: Boolean,
    wasDrawbackDeclaration: Boolean,
    wasRepackagedDraughtDeclaration: Boolean
  ): ValidationResult = {
    val wasNilReturn =
      !(wasDutyDeclared || wasOverDeclaration || wasUnderDeclaration || wasSpoiltDeclaration || wasDrawbackDeclaration || wasRepackagedDraughtDeclaration)

    (if (!wasDutyDeclared) {
       Validations.validateZero(declaredDutyDue, "dutyDeclared.declaredDutyDue")
     } else {
       Validations.validateNonNegative(declaredDutyDue, "dutyDeclared.declaredDutyDue")
     }) |+|
      (if (!wasOverDeclaration) {
         Validations.validateZero(overDeclaration, "dutyDeclared.overDeclaration")
       } else {
         Validations.validateNonPositive(overDeclaration, "dutyDeclared.overDeclaration")
       }) |+|
      (if (!wasUnderDeclaration) {
         Validations.validateZero(underDeclaration, "dutyDeclared.underDeclaration")
       } else {
         Validations.validateNonNegative(underDeclaration, "dutyDeclared.underDeclaration")
       }) |+|
      (if (!wasSpoiltDeclaration) {
         Validations.validateZero(spoiltProduct, "dutyDeclared.spoiltProduct")
       } else {
         Validations.validateNonPositive(spoiltProduct, "dutyDeclared.spoiltProduct")
       }) |+|
      (if (!wasDrawbackDeclaration) {
         Validations.validateZero(drawback, "dutyDeclared.drawback")
       } else {
         Validations.validateNonPositive(drawback, "dutyDeclared.drawback")
       }) |+|
      (if (!wasRepackagedDraughtDeclaration) {
         Validations.validateZero(repackagedDraught, "dutyDeclared.repackagedDraught")
       } else {
         noValidationResult
       }) |+|
      (if (wasNilReturn) {
         Validations.validateZero(totalDutyDue, "dutyDeclared.totalDutyDue")
       } else {
         noValidationResult
       })
  }
}

case object AdrTotals {
  implicit val adrTotalsFormat: OFormat[AdrTotals] = Json.format[AdrTotals]
}

case class AdrReturnSubmission(
  dutyDeclared: AdrDutyDeclared,
  adjustments: AdrAdjustments,
  dutySuspended: AdrDutySuspended,
  spirits: Option[AdrSpirits],
  totals: AdrTotals
) {
  def validate(returnPeriod: ReturnPeriod): ValidationResult = {
    val wasDutyDeclared                 = dutyDeclared.declared
    val wasOverDeclaration              = adjustments.overDeclarationDeclared
    val wasUnderDeclaration             = adjustments.underDeclarationDeclared
    val wasSpoiltDeclaration            = adjustments.spoiltProductDeclared
    val wasDrawbackDeclaration          = adjustments.drawbackDeclared
    val wasRepackagedDraughtDeclaration = adjustments.repackagedDraughtDeclared

    dutyDeclared.validate() |+|
      adjustments.validate() |+|
      dutySuspended.validate() |+|
      spirits.fold(noValidationResult)(_.validate(returnPeriod)) |+|
      totals.validate(
        wasDutyDeclared,
        wasOverDeclaration,
        wasUnderDeclaration,
        wasSpoiltDeclaration,
        wasDrawbackDeclaration,
        wasRepackagedDraughtDeclaration
      )
  }
}

case object AdrReturnSubmission {
  implicit val adrReturnSubmissionFormat: OFormat[AdrReturnSubmission] = Json.format[AdrReturnSubmission]
}

case class AdrReturnCreatedDetails(
  processingDate: Instant,
  amount: BigDecimal,
  chargeReference: Option[String],
  paymentDueDate: LocalDate
)

object AdrReturnCreatedDetails {
  implicit val returnCreatedDetailsWrites: OWrites[AdrReturnCreatedDetails] = Json.writes[AdrReturnCreatedDetails]

  def fromReturnCreatedDetails(returnCreatedDetails: ReturnCreatedDetails): AdrReturnCreatedDetails =
    AdrReturnCreatedDetails(
      processingDate = returnCreatedDetails.processingDate,
      amount = returnCreatedDetails.amount,
      chargeReference = returnCreatedDetails.chargeReference,
      paymentDueDate = returnCreatedDetails.paymentDueDate
    )
}
