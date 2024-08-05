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

import play.api.libs.json.Json
import uk.gov.hmrc.alcoholdutyreturns.base.SpecBase
import uk.gov.hmrc.alcoholdutyreturns.models.ReturnPeriod
import uk.gov.hmrc.alcoholdutyreturns.models.returns.AdrTypeOfSpirit.Other

import java.time.Instant

class AdrReturnSubmissionSpec extends SpecBase {
  "AdrAlcoholQuantity" should {
    "validate if all fields are valid" in new SetUp {
      adrReturnSubmission.dutyDeclared.dutyDeclaredItems.head.quantityDeclared.validate(prefix).isValid shouldBe true
    }

    "succeed if litres equals lpa" in new SetUp {
      adrReturnSubmission.dutyDeclared.dutyDeclaredItems.head.quantityDeclared
        .copy(litres = positive, lpa = positive)
        .validate(prefix)
        .isValid shouldBe true
    }

    "succeed if litres is less than lpa" in new SetUp {
      adrReturnSubmission.dutyDeclared.dutyDeclaredItems.head.quantityDeclared
        .copy(litres = positive, lpa = positiveLarger)
        .validate(prefix)
        .isValid shouldBe false
    }

    "fail if litres is zero" in new SetUp {
      adrReturnSubmission.dutyDeclared.dutyDeclaredItems.head.quantityDeclared
        .copy(litres = zero)
        .validate(prefix)
        .isValid shouldBe false
    }

    "fail if litres is negative" in new SetUp {
      adrReturnSubmission.dutyDeclared.dutyDeclaredItems.head.quantityDeclared
        .copy(litres = negative)
        .validate(prefix)
        .isValid shouldBe false
    }

    "fail if lpa is zero" in new SetUp {
      adrReturnSubmission.dutyDeclared.dutyDeclaredItems.head.quantityDeclared
        .copy(lpa = zero)
        .validate(prefix)
        .isValid shouldBe false
    }

    "fail if lpa is negative" in new SetUp {
      adrReturnSubmission.dutyDeclared.dutyDeclaredItems.head.quantityDeclared
        .copy(lpa = negative)
        .validate(prefix)
        .isValid shouldBe false
    }
  }

  "AdrDuty" should {
    "validate if all fields are valid and duty is owed" in new SetUp {
      adrReturnSubmission.dutyDeclared.dutyDeclaredItems.head.dutyDue.validate(prefix, true).isValid shouldBe true
    }

    "validate if all fields are valid and duty is refunded" in new SetUp {
      adrReturnSubmission.adjustments.overDeclarationProducts.head.dutyDue.validate(prefix, false).isValid shouldBe true
    }

    "fail if tax code is invalid" in new SetUp {
      adrReturnSubmission.dutyDeclared.dutyDeclaredItems.head.dutyDue
        .copy(taxCode = invalidTaxCode)
        .validate(prefix, true)
        .isValid shouldBe false
    }

    "succeed if dutyRate is zero when owed" in new SetUp {
      adrReturnSubmission.dutyDeclared.dutyDeclaredItems.head.dutyDue
        .copy(dutyRate = zero)
        .validate(prefix, true)
        .isValid shouldBe true
    }

    "fail if dutyRate is negative when owed" in new SetUp {
      adrReturnSubmission.dutyDeclared.dutyDeclaredItems.head.dutyDue
        .copy(dutyRate = negative)
        .validate(prefix, true)
        .isValid shouldBe false
    }

    "succeed if dutyRate is zero when refunded" in new SetUp {
      adrReturnSubmission.adjustments.overDeclarationProducts.head.dutyDue
        .copy(dutyRate = zero)
        .validate(prefix, false)
        .isValid shouldBe true
    }

    "fail if dutyRate is negative when refunded" in new SetUp {
      adrReturnSubmission.adjustments.overDeclarationProducts.head.dutyDue
        .copy(dutyRate = negative)
        .validate(prefix, false)
        .isValid shouldBe false
    }

    "succeed if dutyDue is zero when owed" in new SetUp {
      adrReturnSubmission.dutyDeclared.dutyDeclaredItems.head.dutyDue
        .copy(dutyDue = zero)
        .validate(prefix, true)
        .isValid shouldBe true
    }

    "fail if dutyDue is negative when owed" in new SetUp {
      adrReturnSubmission.dutyDeclared.dutyDeclaredItems.head.dutyDue
        .copy(dutyDue = negative)
        .validate(prefix, true)
        .isValid shouldBe false
    }

    "succeed if dutyDue is zero when refunded" in new SetUp {
      adrReturnSubmission.adjustments.overDeclarationProducts.head.dutyDue
        .copy(dutyDue = zero)
        .validate(prefix, false)
        .isValid shouldBe true
    }

    "fail if dutyDue is positive when refunded" in new SetUp {
      adrReturnSubmission.adjustments.overDeclarationProducts.head.dutyDue
        .copy(dutyDue = positive)
        .validate(prefix, false)
        .isValid shouldBe false
    }
  }

  "AdrDutyDeclared" should {
    "validate if all fields are valid and duty is owed" in new SetUp {
      adrReturnSubmission.dutyDeclared.validate().isValid shouldBe true
    }

    "validate if all fields are valid and no duty is owed" in new SetUp {
      adrReturnSubmission.dutyDeclared
        .copy(declared = false, dutyDeclaredItems = Seq.empty)
        .validate()
        .isValid shouldBe true
    }

    "fail if declared, but no items" in new SetUp {
      adrReturnSubmission.dutyDeclared.copy(dutyDeclaredItems = Seq.empty).validate().isValid shouldBe false
    }

    "fail if not declared, but items" in new SetUp {
      adrReturnSubmission.dutyDeclared.copy(declared = false).validate().isValid shouldBe false
    }
  }

  "AdrAdjustmentItem" should {
    "validate if all fields are valid and duty is owed" in new SetUp {
      adrReturnSubmission.adjustments.underDeclarationProducts.head.validate(arrayField, 0, true).isValid shouldBe true
    }

    "validate if all fields are valid and duty is refunded" in new SetUp {
      adrReturnSubmission.adjustments.overDeclarationProducts.head.validate(arrayField, 0, false).isValid shouldBe true
    }

    "fail if bad adjustment quantity" in new SetUp {
      adrReturnSubmission.adjustments.underDeclarationProducts.head
        .copy(adjustmentQuantity =
          adrReturnSubmission.adjustments.underDeclarationProducts.head.adjustmentQuantity.copy(litres = negative)
        )
        .validate(arrayField, 0, true)
        .isValid shouldBe false
    }

    "fail if bad duty when owed" in new SetUp {
      adrReturnSubmission.adjustments.underDeclarationProducts.head
        .copy(dutyDue = adrReturnSubmission.adjustments.underDeclarationProducts.head.dutyDue.copy(dutyDue = negative))
        .validate(arrayField, 0, true)
        .isValid shouldBe false
    }

    "fail if bad duty when refunded" in new SetUp {
      adrReturnSubmission.adjustments.overDeclarationProducts.head
        .copy(dutyDue = adrReturnSubmission.adjustments.overDeclarationProducts.head.dutyDue.copy(dutyDue = positive))
        .validate(arrayField, 0, false)
        .isValid shouldBe false
    }
  }

  "AdrRepackagedDraughtAdjustmentItem" should {
    "validate if all fields are valid and duty is owed" in new SetUp {
      adrReturnSubmission.adjustments.repackagedDraughtProducts.head.validate(0).isValid shouldBe true
    }

    "fail if a bad original tax code" in new SetUp {
      adrReturnSubmission.adjustments.repackagedDraughtProducts.head
        .copy(originalTaxCode = invalidTaxCode)
        .validate(0)
        .isValid shouldBe false
    }

    "fail if a negative original duty rate" in new SetUp {
      adrReturnSubmission.adjustments.repackagedDraughtProducts.head
        .copy(originalDutyRate = negative)
        .validate(0)
        .isValid shouldBe false
    }

    "fail if a bad new tax code" in new SetUp {
      adrReturnSubmission.adjustments.repackagedDraughtProducts.head
        .copy(newTaxCode = invalidTaxCode)
        .validate(0)
        .isValid shouldBe false
    }

    "fail if a negative new duty rate" in new SetUp {
      adrReturnSubmission.adjustments.repackagedDraughtProducts.head
        .copy(newDutyRate = negative)
        .validate(0)
        .isValid shouldBe false
    }

    "fail if a bad repackaged quantity" in new SetUp {
      adrReturnSubmission.adjustments.repackagedDraughtProducts.head
        .copy(repackagedQuantity =
          adrReturnSubmission.adjustments.repackagedDraughtProducts.head.repackagedQuantity.copy(litres = negative)
        )
        .validate(0)
        .isValid shouldBe false
    }
  }

  "AdrAdjustments" should {
    "validate if all fields are valid" in new SetUp {
      adrReturnSubmission.adjustments.validate().isValid shouldBe true
    }

    "fail if over-declaration declared and no items" in new SetUp {
      adrReturnSubmission.adjustments.copy(overDeclarationProducts = Seq.empty).validate().isValid shouldBe false
    }

    "fail if over-declaration declared and bad items" in new SetUp {
      adrReturnSubmission.adjustments
        .copy(overDeclarationProducts =
          Seq(
            adrReturnSubmission.adjustments.overDeclarationProducts.head.copy(adjustmentQuantity =
              adrReturnSubmission.adjustments.overDeclarationProducts.head.adjustmentQuantity.copy(litres = zero)
            )
          )
        )
        .validate()
        .isValid shouldBe false
    }

    "fail if no over-declaration declared and reason exists" in new SetUp {
      adrReturnSubmission.adjustments
        .copy(overDeclarationDeclared = false, overDeclarationProducts = Seq.empty)
        .validate()
        .isValid shouldBe false
    }

    "fail if no over-declaration declared and items" in new SetUp {
      adrReturnSubmission.adjustments
        .copy(overDeclarationDeclared = false, reasonForOverDeclaration = None)
        .validate()
        .isValid shouldBe false
    }

    "fail if under-declaration declared and no items" in new SetUp {
      adrReturnSubmission.adjustments.copy(underDeclarationProducts = Seq.empty).validate().isValid shouldBe false
    }

    "fail if under-declaration declared and bad items" in new SetUp {
      adrReturnSubmission.adjustments
        .copy(underDeclarationProducts =
          Seq(
            adrReturnSubmission.adjustments.underDeclarationProducts.head.copy(adjustmentQuantity =
              adrReturnSubmission.adjustments.underDeclarationProducts.head.adjustmentQuantity.copy(litres = zero)
            )
          )
        )
        .validate()
        .isValid shouldBe false
    }

    "fail if no under-declaration declared and reason exists" in new SetUp {
      adrReturnSubmission.adjustments
        .copy(underDeclarationDeclared = false, underDeclarationProducts = Seq.empty)
        .validate()
        .isValid shouldBe false
    }

    "fail if no under-declaration declared and items" in new SetUp {
      adrReturnSubmission.adjustments
        .copy(underDeclarationDeclared = false, reasonForUnderDeclaration = None)
        .validate()
        .isValid shouldBe false
    }

    "fail if spoilt declared and no items" in new SetUp {
      adrReturnSubmission.adjustments.copy(spoiltProducts = Seq.empty).validate().isValid shouldBe false
    }

    "fail if spoilt declared and bad items" in new SetUp {
      adrReturnSubmission.adjustments
        .copy(spoiltProducts =
          Seq(
            adrReturnSubmission.adjustments.spoiltProducts.head.copy(adjustmentQuantity =
              adrReturnSubmission.adjustments.spoiltProducts.head.adjustmentQuantity.copy(litres = zero)
            )
          )
        )
        .validate()
        .isValid shouldBe false
    }

    "fail if no spoilt declared and items" in new SetUp {
      adrReturnSubmission.adjustments.copy(spoiltProductDeclared = false).validate().isValid shouldBe false
    }

    "fail if drawback declared and no items" in new SetUp {
      adrReturnSubmission.adjustments.copy(drawbackProducts = Seq.empty).validate().isValid shouldBe false
    }

    "fail if drawback declared and bad items" in new SetUp {
      adrReturnSubmission.adjustments
        .copy(drawbackProducts =
          Seq(
            adrReturnSubmission.adjustments.drawbackProducts.head.copy(adjustmentQuantity =
              adrReturnSubmission.adjustments.drawbackProducts.head.adjustmentQuantity.copy(litres = zero)
            )
          )
        )
        .validate()
        .isValid shouldBe false
    }

    "fail if no drawback declared and items" in new SetUp {
      adrReturnSubmission.adjustments.copy(drawbackDeclared = false).validate().isValid shouldBe false
    }

    "fail if repackaged draught declared and no items" in new SetUp {
      adrReturnSubmission.adjustments.copy(repackagedDraughtProducts = Seq.empty).validate().isValid shouldBe false
    }

    "fail if repackaged draught declared and bad items" in new SetUp {
      adrReturnSubmission.adjustments
        .copy(repackagedDraughtProducts =
          Seq(adrReturnSubmission.adjustments.repackagedDraughtProducts.head.copy(originalTaxCode = invalidTaxCode))
        )
        .validate()
        .isValid shouldBe false
    }

    "fail if no repackaged draught declared and items" in new SetUp {
      adrReturnSubmission.adjustments.copy(repackagedDraughtDeclared = false).validate().isValid shouldBe false
    }

    "return it has adjustments if any adjustment has entries" in new SetUp {
      adrReturnSubmission.adjustments
        .copy(
          underDeclarationProducts = Seq.empty,
          spoiltProducts = Seq.empty,
          drawbackProducts = Seq.empty,
          repackagedDraughtProducts = Seq.empty
        )
        .hasAdjustments shouldBe true
      adrReturnSubmission.adjustments
        .copy(
          overDeclarationProducts = Seq.empty,
          spoiltProducts = Seq.empty,
          drawbackProducts = Seq.empty,
          repackagedDraughtProducts = Seq.empty
        )
        .hasAdjustments shouldBe true
      adrReturnSubmission.adjustments
        .copy(
          overDeclarationProducts = Seq.empty,
          underDeclarationProducts = Seq.empty,
          drawbackProducts = Seq.empty,
          repackagedDraughtProducts = Seq.empty
        )
        .hasAdjustments shouldBe true
      adrReturnSubmission.adjustments
        .copy(
          overDeclarationProducts = Seq.empty,
          underDeclarationProducts = Seq.empty,
          spoiltProducts = Seq.empty,
          repackagedDraughtProducts = Seq.empty
        )
        .hasAdjustments shouldBe true
      adrReturnSubmission.adjustments
        .copy(
          overDeclarationProducts = Seq.empty,
          underDeclarationProducts = Seq.empty,
          spoiltProducts = Seq.empty,
          drawbackProducts = Seq.empty
        )
        .hasAdjustments shouldBe true
    }

    "return it doesn't have adjustments if no adjustment has entries" in new SetUp {
      adrReturnSubmission.adjustments
        .copy(
          overDeclarationProducts = Seq.empty,
          underDeclarationProducts = Seq.empty,
          spoiltProducts = Seq.empty,
          drawbackProducts = Seq.empty,
          repackagedDraughtProducts = Seq.empty
        )
        .hasAdjustments shouldBe false
    }
  }

  "AdrDutySuspendedProduct" should {
    "validate if all fields are valid" in new SetUp {
      adrReturnSubmission.dutySuspended.dutySuspendedProducts.head.validate(0).isValid shouldBe true
    }

    "fail if the suspended quantity is invalid" in new SetUp {
      adrReturnSubmission.dutySuspended.dutySuspendedProducts.head
        .copy(suspendedQuantity =
          adrReturnSubmission.dutySuspended.dutySuspendedProducts.head.suspendedQuantity.copy(litres = negative)
        )
        .validate(0)
        .isValid shouldBe false
    }
  }

  "AdrDutySuspended" should {
    "validate if all fields are valid" in new SetUp {
      adrReturnSubmission.dutySuspended.validate().isValid shouldBe true
    }

    "fail if declared but no items" in new SetUp {
      adrReturnSubmission.dutySuspended.copy(dutySuspendedProducts = Seq.empty).validate().isValid shouldBe false
    }

    "fail if items any item is invalid" in new SetUp {
      adrReturnSubmission.dutySuspended
        .copy(dutySuspendedProducts =
          Seq(
            adrReturnSubmission.dutySuspended.dutySuspendedProducts.head.copy(suspendedQuantity =
              adrReturnSubmission.dutySuspended.dutySuspendedProducts.head.suspendedQuantity.copy(litres = negative)
            )
          )
        )
        .validate()
        .isValid shouldBe false
    }

    "fail if not declared but items" in new SetUp {
      adrReturnSubmission.dutySuspended.copy(declared = false).validate().isValid shouldBe false
    }
  }

  "AdrSpiritsVolumes" should {
    "validate if all fields are valid" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get.spiritsVolumes.validate().isValid shouldBe true
    }

    "fail if totalSpirits is zero" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get.spiritsVolumes
        .copy(totalSpirits = zero)
        .validate()
        .isValid shouldBe false
    }

    "fail if totalSpirits is negative" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get.spiritsVolumes
        .copy(totalSpirits = negative)
        .validate()
        .isValid shouldBe false
    }

    "validate if scotchWhiskey is zero" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get.spiritsVolumes
        .copy(scotchWhiskey = zero)
        .validate()
        .isValid shouldBe true
    }

    "fail if scotchWhiskey is negative" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get.spiritsVolumes
        .copy(scotchWhiskey = negative)
        .validate()
        .isValid shouldBe false
    }

    "validate if irishWhisky is zero" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get.spiritsVolumes
        .copy(irishWhisky = zero)
        .validate()
        .isValid shouldBe true
    }

    "fail if irishWhisky is negative" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get.spiritsVolumes
        .copy(irishWhisky = negative)
        .validate()
        .isValid shouldBe false
    }
  }

  "AdrSpiritsGrainsQuantities" should {
    "validate if all fields are valid" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get.grainsQuantities.validate().isValid shouldBe true
    }

    "validate if maltedBarley is not present" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get.grainsQuantities
        .copy(maltedBarley = None)
        .validate()
        .isValid shouldBe true
    }

    "fail if all maltedBarley is zero" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get.grainsQuantities
        .copy(maltedBarley = Some(zero))
        .validate()
        .isValid shouldBe false
    }

    "fail if all maltedBarley is negative" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get.grainsQuantities
        .copy(maltedBarley = Some(negative))
        .validate()
        .isValid shouldBe false
    }

    "validate if otherMaltedGrain is not present" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get.grainsQuantities
        .copy(otherMaltedGrain = None)
        .validate()
        .isValid shouldBe true
    }

    "fail if all otherMaltedGrain is zero" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get.grainsQuantities
        .copy(otherMaltedGrain = Some(zero))
        .validate()
        .isValid shouldBe false
    }

    "fail if all otherMaltedGrain is negative" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get.grainsQuantities
        .copy(otherMaltedGrain = Some(negative))
        .validate()
        .isValid shouldBe false
    }

    "validate if wheat is not present" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get.grainsQuantities
        .copy(wheat = None)
        .validate()
        .isValid shouldBe true
    }

    "fail if all wheat is zero" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get.grainsQuantities
        .copy(wheat = Some(zero))
        .validate()
        .isValid shouldBe false
    }

    "fail if all wheat is negative" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get.grainsQuantities
        .copy(wheat = Some(negative))
        .validate()
        .isValid shouldBe false
    }

    "validate if maize is not present" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get.grainsQuantities
        .copy(maize = None)
        .validate()
        .isValid shouldBe true
    }

    "fail if all maize is zero" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get.grainsQuantities
        .copy(maize = Some(zero))
        .validate()
        .isValid shouldBe false
    }

    "fail if all maize is negative" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get.grainsQuantities
        .copy(maize = Some(negative))
        .validate()
        .isValid shouldBe false
    }

    "validate if rye is not present" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get.grainsQuantities
        .copy(rye = None)
        .validate()
        .isValid shouldBe true
    }

    "fail if all rye is zero" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get.grainsQuantities
        .copy(rye = Some(zero))
        .validate()
        .isValid shouldBe false
    }

    "fail if all rye is negative" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get.grainsQuantities
        .copy(rye = Some(negative))
        .validate()
        .isValid shouldBe false
    }

    "validate if unmaltedGrain is not present" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get.grainsQuantities
        .copy(unmaltedGrain = None)
        .validate()
        .isValid shouldBe true
    }

    "fail if all unmaltedGrain is zero" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get.grainsQuantities
        .copy(unmaltedGrain = Some(zero))
        .validate()
        .isValid shouldBe false
    }

    "fail if all unmaltedGrain is negative" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get.grainsQuantities
        .copy(unmaltedGrain = Some(negative))
        .validate()
        .isValid shouldBe false
    }
  }

  "AdrSpiritsIngredientsVolumes" should {
    "validate if all fields are valid" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get.ingredientsVolumes.validate().isValid shouldBe true
    }

    "validate if ethylene is not present" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get.ingredientsVolumes
        .copy(ethylene = None)
        .validate()
        .isValid shouldBe true
    }

    "fail if all ethylene is zero" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get.ingredientsVolumes
        .copy(ethylene = Some(zero))
        .validate()
        .isValid shouldBe false
    }

    "fail if all ethylene is negative" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get.ingredientsVolumes
        .copy(ethylene = Some(negative))
        .validate()
        .isValid shouldBe false
    }

    "validate if molasses is not present" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get.ingredientsVolumes
        .copy(molasses = None)
        .validate()
        .isValid shouldBe true
    }

    "fail if all molasses is zero" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get.ingredientsVolumes
        .copy(molasses = Some(zero))
        .validate()
        .isValid shouldBe false
    }

    "fail if all molasses is negative" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get.ingredientsVolumes
        .copy(molasses = Some(negative))
        .validate()
        .isValid shouldBe false
    }

    "validate if beer is not present" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get.ingredientsVolumes
        .copy(beer = None)
        .validate()
        .isValid shouldBe true
    }

    "fail if all beer is zero" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get.ingredientsVolumes
        .copy(beer = Some(zero))
        .validate()
        .isValid shouldBe false
    }

    "fail if all beer is negative" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get.ingredientsVolumes
        .copy(beer = Some(negative))
        .validate()
        .isValid shouldBe false
    }

    "validate if wine is not present" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get.ingredientsVolumes
        .copy(wine = None)
        .validate()
        .isValid shouldBe true
    }

    "fail if all wine is zero" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get.ingredientsVolumes
        .copy(wine = Some(zero))
        .validate()
        .isValid shouldBe false
    }

    "fail if all wine is negative" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get.ingredientsVolumes
        .copy(wine = Some(negative))
        .validate()
        .isValid shouldBe false
    }

    "validate if madeWine is not present" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get.ingredientsVolumes
        .copy(madeWine = None)
        .validate()
        .isValid shouldBe true
    }

    "fail if all madeWine is zero" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get.ingredientsVolumes
        .copy(madeWine = Some(zero))
        .validate()
        .isValid shouldBe false
    }

    "fail if all madeWine is negative" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get.ingredientsVolumes
        .copy(madeWine = Some(negative))
        .validate()
        .isValid shouldBe false
    }

    "validate if ciderOrPerry is not present" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get.ingredientsVolumes
        .copy(ciderOrPerry = None)
        .validate()
        .isValid shouldBe true
    }

    "fail if all ciderOrPerry is zero" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get.ingredientsVolumes
        .copy(ciderOrPerry = Some(zero))
        .validate()
        .isValid shouldBe false
    }

    "fail if all ciderOrPerry is negative" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get.ingredientsVolumes
        .copy(ciderOrPerry = Some(negative))
        .validate()
        .isValid shouldBe false
    }
  }

  "AdrOtherIngredient" should {
    "validate if all fields are valid" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get.otherIngredient.get.validate().isValid shouldBe true
    }

    "fail if quantity is zero" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get.otherIngredient.get
        .copy(quantity = zero)
        .validate()
        .isValid shouldBe false
    }

    "fail if ingredient name is an empty string" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get.otherIngredient.get
        .copy(ingredientName = "")
        .validate()
        .isValid shouldBe false
    }
  }

  "AdrSpiritsProduced" should {
    "validate if all fields are valid" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get.validate().isValid shouldBe true
    }

    "fail if spiritsVolumes fails to validate" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get
        .copy(spiritsVolumes =
          adrReturnSubmission.spirits.get.spiritsProduced.get.spiritsVolumes.copy(totalSpirits = zero)
        )
        .validate()
        .isValid shouldBe false
    }

    "fail if otherSpiritTypeName is present, but Other is not a type of spirit produced" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get
        .copy(typesOfSpirit = adrReturnSubmission.spirits.get.spiritsProduced.get.typesOfSpirit.filter(_ != Other))
        .validate()
        .isValid shouldBe false
    }

    "fail if otherSpiritTypeName is not present, but Other is a type of spirit produced" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get
        .copy(otherSpiritTypeName = None)
        .validate()
        .isValid shouldBe false
    }

    "fail if grainsQuantities fails to validate" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get
        .copy(grainsQuantities =
          adrReturnSubmission.spirits.get.spiritsProduced.get.grainsQuantities.copy(maltedBarley = Some(zero))
        )
        .validate()
        .isValid shouldBe false
    }

    "fail if hasOtherMaltedGrain, but otherMaltedGrainType is not present" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get
        .copy(otherMaltedGrainType = None)
        .validate()
        .isValid shouldBe false
    }

    "fail if not hasOtherMaltedGrain, but otherMaltedGrainType is present" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get
        .copy(hasOtherMaltedGrain = false)
        .validate()
        .isValid shouldBe false
    }

    "fail if ingredientsVolumes fails to validate" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get
        .copy(ingredientsVolumes =
          adrReturnSubmission.spirits.get.spiritsProduced.get.ingredientsVolumes.copy(ethylene = Some(zero))
        )
        .validate()
        .isValid shouldBe false
    }

    "succeed if otherIngredient is not present" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get.copy(otherIngredient = None).validate().isValid shouldBe true
    }

    "fail if otherIngredient is present but not valid" in new SetUp {
      adrReturnSubmission.spirits.get.spiritsProduced.get
        .copy(otherIngredient =
          Some(adrReturnSubmission.spirits.get.spiritsProduced.get.otherIngredient.get.copy(quantity = zero))
        )
        .validate()
        .isValid shouldBe false
    }
  }

  "AdrSpirits" should {
    "validate if all fields are valid" in new SetUp {
      adrReturnSubmission.spirits.get.validate(returnPeriod).isValid shouldBe true
    }

    "fail if declared but not on a quarter" in new SetUp {
      adrReturnSubmission.spirits.get.validate(notQuarterReturnPeriod).isValid shouldBe false
    }

    "fail if declared but spiritsProduced is missing" in new SetUp {
      adrReturnSubmission.spirits.get.copy(spiritsProduced = None).validate(returnPeriod).isValid shouldBe false
    }

    "fail if declared but spiritsProduced is not valid" in new SetUp {
      adrReturnSubmission.spirits.get
        .copy(spiritsProduced =
          Some(adrReturnSubmission.spirits.get.spiritsProduced.get.copy(otherSpiritTypeName = None))
        )
        .validate(returnPeriod)
        .isValid shouldBe false
    }

    "fail if not declared but spiritsProduced is present" in new SetUp {
      adrReturnSubmission.spirits.get.copy(spiritsDeclared = false).validate(returnPeriod).isValid shouldBe false
    }
  }

  "AdrTotals" should {
    "validate if all fields are valid" in new SetUp {
      adrReturnSubmission.totals
        .validate(
          wasDutyDeclared = true,
          wasOverDeclaration = true,
          wasUnderDeclaration = true,
          wasSpoiltDeclaration = true,
          wasDrawbackDeclaration = true,
          wasRepackagedDraughtDeclaration = true
        )
        .isValid shouldBe true
    }

    "validate if declaredDutyDue is zero" in new SetUp {
      adrReturnSubmission.totals
        .copy(declaredDutyDue = zero)
        .validate(
          wasDutyDeclared = true,
          wasOverDeclaration = true,
          wasUnderDeclaration = true,
          wasSpoiltDeclaration = true,
          wasDrawbackDeclaration = true,
          wasRepackagedDraughtDeclaration = true
        )
        .isValid shouldBe true
    }

    "fail if declaredDutyDue is negative" in new SetUp {
      adrReturnSubmission.totals
        .copy(declaredDutyDue = negative)
        .validate(
          wasDutyDeclared = true,
          wasOverDeclaration = true,
          wasUnderDeclaration = true,
          wasSpoiltDeclaration = true,
          wasDrawbackDeclaration = true,
          wasRepackagedDraughtDeclaration = true
        )
        .isValid shouldBe false
    }

    "fail if no duty declared but total present" in new SetUp {
      adrReturnSubmission.totals
        .validate(
          wasDutyDeclared = false,
          wasOverDeclaration = true,
          wasUnderDeclaration = true,
          wasSpoiltDeclaration = true,
          wasDrawbackDeclaration = true,
          wasRepackagedDraughtDeclaration = true
        )
        .isValid shouldBe false
    }

    "validate if overDeclaration is zero" in new SetUp {
      adrReturnSubmission.totals
        .copy(overDeclaration = zero)
        .validate(
          wasDutyDeclared = true,
          wasOverDeclaration = true,
          wasUnderDeclaration = true,
          wasSpoiltDeclaration = true,
          wasDrawbackDeclaration = true,
          wasRepackagedDraughtDeclaration = true
        )
        .isValid shouldBe true
    }

    "fail if overDeclaration is positive" in new SetUp {
      adrReturnSubmission.totals
        .copy(overDeclaration = positive)
        .validate(
          wasDutyDeclared = true,
          wasOverDeclaration = true,
          wasUnderDeclaration = true,
          wasSpoiltDeclaration = true,
          wasDrawbackDeclaration = true,
          wasRepackagedDraughtDeclaration = true
        )
        .isValid shouldBe false
    }

    "fail if no overdeclaration but total present" in new SetUp {
      adrReturnSubmission.totals
        .validate(
          wasDutyDeclared = true,
          wasOverDeclaration = false,
          wasUnderDeclaration = true,
          wasSpoiltDeclaration = true,
          wasDrawbackDeclaration = true,
          wasRepackagedDraughtDeclaration = true
        )
        .isValid shouldBe false
    }

    "validate if underDeclaration is zero" in new SetUp {
      adrReturnSubmission.totals
        .copy(underDeclaration = zero)
        .validate(
          wasDutyDeclared = true,
          wasOverDeclaration = true,
          wasUnderDeclaration = true,
          wasSpoiltDeclaration = true,
          wasDrawbackDeclaration = true,
          wasRepackagedDraughtDeclaration = true
        )
        .isValid shouldBe true
    }

    "fail if underDeclaration is negative" in new SetUp {
      adrReturnSubmission.totals
        .copy(underDeclaration = negative)
        .validate(
          wasDutyDeclared = true,
          wasOverDeclaration = true,
          wasUnderDeclaration = true,
          wasSpoiltDeclaration = true,
          wasDrawbackDeclaration = true,
          wasRepackagedDraughtDeclaration = true
        )
        .isValid shouldBe false
    }

    "fail if no underDeclaration but total present" in new SetUp {
      adrReturnSubmission.totals
        .validate(
          wasDutyDeclared = true,
          wasOverDeclaration = true,
          wasUnderDeclaration = false,
          wasSpoiltDeclaration = true,
          wasDrawbackDeclaration = true,
          wasRepackagedDraughtDeclaration = true
        )
        .isValid shouldBe false
    }

    "validate if spoiltProduct is zero" in new SetUp {
      adrReturnSubmission.totals
        .copy(spoiltProduct = zero)
        .validate(
          wasDutyDeclared = true,
          wasOverDeclaration = true,
          wasUnderDeclaration = true,
          wasSpoiltDeclaration = true,
          wasDrawbackDeclaration = true,
          wasRepackagedDraughtDeclaration = true
        )
        .isValid shouldBe true
    }

    "fail if spoiltProduct is positive" in new SetUp {
      adrReturnSubmission.totals
        .copy(spoiltProduct = positive)
        .validate(
          wasDutyDeclared = true,
          wasOverDeclaration = true,
          wasUnderDeclaration = true,
          wasSpoiltDeclaration = true,
          wasDrawbackDeclaration = true,
          wasRepackagedDraughtDeclaration = true
        )
        .isValid shouldBe false
    }

    "fail if no spoiltProduct but total present" in new SetUp {
      adrReturnSubmission.totals
        .validate(
          wasDutyDeclared = true,
          wasOverDeclaration = true,
          wasUnderDeclaration = true,
          wasSpoiltDeclaration = false,
          wasDrawbackDeclaration = true,
          wasRepackagedDraughtDeclaration = true
        )
        .isValid shouldBe false
    }

    "validate if drawback is zero" in new SetUp {
      adrReturnSubmission.totals
        .copy(drawback = zero)
        .validate(
          wasDutyDeclared = true,
          wasOverDeclaration = true,
          wasUnderDeclaration = true,
          wasSpoiltDeclaration = true,
          wasDrawbackDeclaration = true,
          wasRepackagedDraughtDeclaration = true
        )
        .isValid shouldBe true
    }

    "fail if drawback is positive" in new SetUp {
      adrReturnSubmission.totals
        .copy(drawback = positive)
        .validate(
          wasDutyDeclared = true,
          wasOverDeclaration = true,
          wasUnderDeclaration = true,
          wasSpoiltDeclaration = true,
          wasDrawbackDeclaration = true,
          wasRepackagedDraughtDeclaration = true
        )
        .isValid shouldBe false
    }

    "fail if no drawback but total present" in new SetUp {
      adrReturnSubmission.totals
        .validate(
          wasDutyDeclared = true,
          wasOverDeclaration = true,
          wasUnderDeclaration = true,
          wasSpoiltDeclaration = true,
          wasDrawbackDeclaration = false,
          wasRepackagedDraughtDeclaration = true
        )
        .isValid shouldBe false
    }

    "validate if repackagedDraught is zero" in new SetUp {
      adrReturnSubmission.totals
        .copy(repackagedDraught = zero)
        .validate(
          wasDutyDeclared = true,
          wasOverDeclaration = true,
          wasUnderDeclaration = true,
          wasSpoiltDeclaration = true,
          wasDrawbackDeclaration = true,
          wasRepackagedDraughtDeclaration = true
        )
        .isValid shouldBe true
    }

    "validate if repackagedDraught is positive" in new SetUp {
      adrReturnSubmission.totals
        .copy(repackagedDraught = positive)
        .validate(
          wasDutyDeclared = true,
          wasOverDeclaration = true,
          wasUnderDeclaration = true,
          wasSpoiltDeclaration = true,
          wasDrawbackDeclaration = true,
          wasRepackagedDraughtDeclaration = true
        )
        .isValid shouldBe true
    }

    "validate if repackagedDraught is negative" in new SetUp {
      adrReturnSubmission.totals
        .copy(repackagedDraught = negative)
        .validate(
          wasDutyDeclared = true,
          wasOverDeclaration = true,
          wasUnderDeclaration = true,
          wasSpoiltDeclaration = true,
          wasDrawbackDeclaration = true,
          wasRepackagedDraughtDeclaration = true
        )
        .isValid shouldBe true
    }

    "fail if no repackagedDraught but total present" in new SetUp {
      adrReturnSubmission.totals
        .validate(
          wasDutyDeclared = true,
          wasOverDeclaration = true,
          wasUnderDeclaration = true,
          wasSpoiltDeclaration = true,
          wasDrawbackDeclaration = true,
          wasRepackagedDraughtDeclaration = false
        )
        .isValid shouldBe false
    }

    "validate if a nil return and totalDutyDue zero" in new SetUp {
      nilReturn.totals
        .validate(
          wasDutyDeclared = false,
          wasOverDeclaration = false,
          wasUnderDeclaration = false,
          wasSpoiltDeclaration = false,
          wasDrawbackDeclaration = false,
          wasRepackagedDraughtDeclaration = false
        )
        .isValid shouldBe true
    }

    "fail if a nil return but totalDutyDue positive" in new SetUp {
      nilReturn.totals
        .copy(totalDutyDue = positive)
        .validate(
          wasDutyDeclared = false,
          wasOverDeclaration = false,
          wasUnderDeclaration = false,
          wasSpoiltDeclaration = false,
          wasDrawbackDeclaration = false,
          wasRepackagedDraughtDeclaration = false
        )
        .isValid shouldBe false
    }

    "fail if a nil return but totalDutyDue negative" in new SetUp {
      nilReturn.totals
        .copy(totalDutyDue = negative)
        .validate(
          wasDutyDeclared = false,
          wasOverDeclaration = false,
          wasUnderDeclaration = false,
          wasSpoiltDeclaration = false,
          wasDrawbackDeclaration = false,
          wasRepackagedDraughtDeclaration = false
        )
        .isValid shouldBe false
    }
  }

  "AdrReturnSubmission" should {
    "deserialise from json" in new SetUp {
      Json.parse(adrReturnSubmissionJson).as[AdrReturnSubmission] shouldBe adrReturnSubmission
    }

    "validate if all fields are valid" in new SetUp {
      adrReturnSubmission.validate(returnPeriod).isValid shouldBe true
    }

    "fail if duty declared is invalid" in new SetUp {
      adrReturnSubmission
        .copy(dutyDeclared = adrReturnSubmission.dutyDeclared.copy(dutyDeclaredItems = Seq.empty))
        .validate(returnPeriod)
        .isValid shouldBe false
    }

    "fail if adjustments is invalid" in new SetUp {
      adrReturnSubmission
        .copy(adjustments = adrReturnSubmission.adjustments.copy(overDeclarationProducts = Seq.empty))
        .validate(returnPeriod)
        .isValid shouldBe false
    }

    "fail if dutySuspended is invalid" in new SetUp {
      adrReturnSubmission
        .copy(dutySuspended = adrReturnSubmission.dutySuspended.copy(dutySuspendedProducts = Seq.empty))
        .validate(returnPeriod)
        .isValid shouldBe false
    }

    "fail if spirits is invalid" in new SetUp {
      adrReturnSubmission
        .copy(spirits = Some(adrReturnSubmission.spirits.get.copy(spiritsProduced = None)))
        .validate(returnPeriod)
        .isValid shouldBe false
    }

    "fail if totals is invalid" in new SetUp {
      adrReturnSubmission
        .copy(totals = adrReturnSubmission.totals.copy(overDeclaration = positive))
        .validate(returnPeriod)
        .isValid shouldBe false
    }
  }

  "AdrReturnCreatedDetails" should {
    "serialise to json" in new SetUp {
      Json.toJson(adrReturnCreatedDetails).toString() shouldBe adrReturnCreatedDetailsJson
    }

    "convert from ReturnCreatedDetails to AdrReturnCreatedDetails" in new SetUp {
      AdrReturnCreatedDetails.fromReturnCreatedDetails(returnCreatedDetails) shouldBe adrReturnCreatedDetails
    }
  }

  class SetUp {
    val periodKey              = periodKeyMar
    val returnPeriod           = ReturnPeriod.fromPeriodKey(periodKey).get
    val notQuarterReturnPeriod = ReturnPeriod.fromPeriodKey(periodKeyApr).get
    val badPeriodKey           = "24A"
    val total                  = BigDecimal("12345.67")
    val now                    = Instant.now(clock)
    val prefix                 = "prefix"
    val arrayField             = "array"
    val zero                   = BigDecimal(0)
    val negative               = BigDecimal("-0.01")
    val positive               = BigDecimal("0.01")
    val positiveLarger         = BigDecimal("0.02")
    val invalidTaxCode         = "31"

    val adrReturnSubmissionJson     =
      """{"dutyDeclared":{"declared":true,"dutyDeclaredItems":[{"quantityDeclared":{"litres":1000.1,"lpa":100.101},"dutyDue":{"taxCode":"331","dutyRate":1.27,"dutyDue":127.12}},{"quantityDeclared":{"litres":2000.21,"lpa":200.2022},"dutyDue":{"taxCode":"332","dutyRate":1.57,"dutyDue":314.31}}]},"adjustments":{"overDeclarationDeclared":true,"reasonForOverDeclaration":"Submitted too much","overDeclarationProducts":[{"returnPeriod":"24AD","adjustmentQuantity":{"litres":400.04,"lpa":40.0404},"dutyDue":{"taxCode":"352","dutyRate":1.32,"dutyDue":-52.85}}],"underDeclarationDeclared":true,"reasonForUnderDeclaration":"Submitted too little","underDeclarationProducts":[{"returnPeriod":"24AC","adjustmentQuantity":{"litres":300.03,"lpa":30.0303},"dutyDue":{"taxCode":"351","dutyRate":2.32,"dutyDue":69.67}}],"spoiltProductDeclared":true,"spoiltProducts":[{"returnPeriod":"24AE","adjustmentQuantity":{"litres":500.05,"lpa":50.0505},"dutyDue":{"taxCode":"353","dutyRate":1.82,"dutyDue":-91.09}}],"drawbackDeclared":true,"drawbackProducts":[{"returnPeriod":"24AF","adjustmentQuantity":{"litres":600.06,"lpa":60.0606},"dutyDue":{"taxCode":"361","dutyRate":2.21,"dutyDue":-132.73}}],"repackagedDraughtDeclared":true,"repackagedDraughtProducts":[{"returnPeriod":"24AG","originalTaxCode":"371","originalDutyRate":0.27,"newTaxCode":"331","newDutyRate":1.27,"repackagedQuantity":{"litres":700.07,"lpa":70.0707},"dutyAdjustment":70.07}]},"dutySuspended":{"declared":true,"dutySuspendedProducts":[{"regime":"Beer","suspendedQuantity":{"litres":1010.11,"lpa":101.1011}},{"regime":"Wine","suspendedQuantity":{"litres":2020.22,"lpa":202.2022}},{"regime":"Cider","suspendedQuantity":{"litres":3030.33,"lpa":303.3033}},{"regime":"Spirits","suspendedQuantity":{"litres":4040.44,"lpa":404.4044}},{"regime":"OtherFermentedProduct","suspendedQuantity":{"litres":5050.55,"lpa":505.5055}}]},"spirits":{"spiritsDeclared":true,"spiritsProduced":{"spiritsVolumes":{"totalSpirits":123.45,"scotchWhiskey":234.56,"irishWhisky":345.67},"typesOfSpirit":["Malt","Beer","Other"],"otherSpiritTypeName":"MaltyBeer","hasOtherMaltedGrain":true,"grainsQuantities":{"maltedBarley":10,"otherMaltedGrain":11.11,"wheat":22.22,"maize":33.33,"rye":44.44,"unmaltedGrain":55.55},"otherMaltedGrainType":"Smarties","ingredientsVolumes":{"ethylene":10.1,"molasses":20.2,"beer":30.3,"wine":40.4,"madeWine":50.5,"ciderOrPerry":60.6},"otherIngredient":{"quantity":70.7,"unitOfMeasure":"Tonnes","ingredientName":"Coco Pops"}}},"totals":{"declaredDutyDue":441.53,"overDeclaration":-52.85,"underDeclaration":69.67,"spoiltProduct":-91.09,"drawback":-132.73,"repackagedDraught":70.07,"totalDutyDue":304.6}}"""
    val adrReturnCreatedDetailsJson =
      s"""{"processingDate":"2024-06-11T15:07:47.838Z","amount":$total,"chargeReference":"$chargeReference","paymentDueDate":"2024-04-25"}"""

    val adrReturnSubmission     = exampleReturnSubmissionRequest
    val returnCreatedDetails    =
      exampleReturnCreatedSuccessfulResponse(periodKey, total, now, chargeReference, submissionId).success
    val adrReturnCreatedDetails = exampleReturnCreatedDetails(periodKey, total, now, chargeReference)

    val nilReturn = exampleNilSubmissionRequest
  }
}
