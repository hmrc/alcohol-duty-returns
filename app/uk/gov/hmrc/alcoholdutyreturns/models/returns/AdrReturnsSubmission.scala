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

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}
import play.api.libs.json.{Json, Reads}

case class AdrAlcoholQuantity(
  litres: BigDecimal,
  lpa: BigDecimal
)

case object AdrAlcoholQuantity {
  implicit val adrAlcoholQuantityReads: Reads[AdrAlcoholQuantity] = Json.reads[AdrAlcoholQuantity]
}

case class AdrDuty(
  taxCode: String,
  dutyRate: BigDecimal,
  dutyDue: BigDecimal
)

case object AdrDuty {
  implicit val adrDutyReads: Reads[AdrDuty] = Json.reads[AdrDuty]
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
)

case object AdrDutyDeclaredItem {
  implicit val adrDutyDeclaredItemReads: Reads[AdrDutyDeclaredItem] = Json.reads[AdrDutyDeclaredItem]
}

case class AdrDutyDeclared(
                            declared: Boolean,
                            dutyDeclaredItems: Seq[AdrDutyDeclaredItem]
                          )

case object AdrDutyDeclared {
  implicit val adrDutyDeclaredReads: Reads[AdrDutyDeclared] = Json.reads[AdrDutyDeclared]
}

case class AdrAdjustmentItem(
                              returnPeriod: String,
                              adjustmentQuantity: AdrAlcoholQuantity,
                              dutyDue: AdrDuty
)

case object AdrAdjustmentItem {
  implicit val adrAdjustmentItemReads: Reads[AdrAdjustmentItem] = Json.reads[AdrAdjustmentItem]
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
  implicit val adrRepackagedDraughtAdjustmentItemReads: Reads[AdrRepackagedDraughtAdjustmentItem] =
    Json.reads[AdrRepackagedDraughtAdjustmentItem]
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
                         )

case object AdrAdjustments {
  implicit val adrAdjustmentsReads: Reads[AdrAdjustments] =
    Json.reads[AdrAdjustments]
}

case class AdrDutySuspendedProduct(
  regime: AdrDutySuspendedAlcoholRegime,
  suspendedQuantity: AdrAlcoholQuantity
)

case object AdrDutySuspendedProduct {
  implicit val adrDutySuspendedProductReads: Reads[AdrDutySuspendedProduct] = Json.reads[AdrDutySuspendedProduct]
}

case class AdrDutySuspended(
                             declared: Boolean,
                             dutySuspendedProducts: Set[AdrDutySuspendedProduct]
                           )

case object AdrDutySuspended {
  implicit val adrDutySuspendedReads: Reads[AdrDutySuspended] = Json.reads[AdrDutySuspended]
}

case class AdrSpiritsVolumes(
  totalSpirits: BigDecimal,
  scotchWhiskey: BigDecimal,
  irishWhisky: BigDecimal
)

case object AdrSpiritsVolumes {
  implicit val adrSpiritsVolumesReads: Reads[AdrSpiritsVolumes] = Json.reads[AdrSpiritsVolumes]
}

case class AdrSpiritsGrainsQuantities(
  maltedBarley: Option[BigDecimal],
  otherMaltedGrain: Option[BigDecimal],
  wheat: Option[BigDecimal],
  maize: Option[BigDecimal],
  rye: Option[BigDecimal],
  unmaltedGrain: Option[BigDecimal]
)

case object AdrSpiritsGrainsQuantities {
  implicit val adrSpiritsGrainsQuantitiesReads: Reads[AdrSpiritsGrainsQuantities] = Json.reads[AdrSpiritsGrainsQuantities]
}

case class AdrSpiritsIngredientsVolumes(
  ethylene: Option[BigDecimal],
  molasses: Option[BigDecimal],
  beer: Option[BigDecimal],
  wine: Option[BigDecimal],
  madeWine: Option[BigDecimal],
  ciderOrPerry: Option[BigDecimal]
)

case object AdrSpiritsIngredientsVolumes {
  implicit val adrSpiritsIngredientsVolumesReads: Reads[AdrSpiritsIngredientsVolumes] =
    Json.reads[AdrSpiritsIngredientsVolumes]
}

case class AdrOtherIngredient(
  quantity: BigDecimal,
  unitOfMeasure: AdrUnitOfMeasure,
  ingredientName: String
)

case object AdrOtherIngredient {
  implicit val adrOtherIngredientReads: Reads[AdrOtherIngredient] = Json.reads[AdrOtherIngredient]
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
)

case object AdrSpiritsProduced {
  implicit val adrSpiritsProducedReads: Reads[AdrSpiritsProduced] = Json.reads[AdrSpiritsProduced]
}

case class AdrSpirits(
  spiritsDeclared: Boolean,
  spiritsProduced: Option[AdrSpiritsProduced]
                     )

case object AdrSpirits {
  implicit val adrSpiritsReads: Reads[AdrSpirits] = Json.reads[AdrSpirits]
}

case class AdrTotals (
                        declaredDutyDue: BigDecimal,
                        overDeclaration: BigDecimal,
                        underDeclaration: BigDecimal,
                        spoiltProduct: BigDecimal,
                        drawback: BigDecimal,
                        repackagedDraught: BigDecimal,
                        totalDutyDue: BigDecimal
                      )

case object AdrTotals {
  implicit val adrTotalsReads: Reads[AdrTotals] = Json.reads[AdrTotals]
}

case class AdrReturnsSubmission(
                                 dutyDeclared: AdrDutyDeclared,
                                 adjustments: AdrAdjustments,
                                 dutySuspended: AdrDutySuspended,
                                 spirits: Option[AdrSpirits],
                                 totals: AdrTotals
                               )

case object AdrReturnsSubmission {
  implicit val adrReturnsSubmissionReads: Reads[AdrReturnsSubmission] = Json.reads[AdrReturnsSubmission]
}