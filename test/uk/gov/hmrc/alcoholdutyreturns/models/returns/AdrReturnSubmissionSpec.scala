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

import java.time.Instant

class AdrReturnSubmissionSpec extends SpecBase {
  "AdrReturnSubmission" should {
    "deserialise from json" in new SetUp {
      Json.parse(adrReturnSubmissionJson).as[AdrReturnSubmission] shouldBe adrReturnSubmission
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
    val periodKey  = "24AC"
    val total      = BigDecimal("12345.67")
    val now        = Instant.now(clock)

    val adrReturnSubmissionJson     =
      """{"dutyDeclared":{"declared":true,"dutyDeclaredItems":[{"quantityDeclared":{"litres":1000.1,"lpa":100.101},"dutyDue":{"taxCode":"331","dutyRate":1.27,"dutyDue":127.12}},{"quantityDeclared":{"litres":2000.21,"lpa":200.2022},"dutyDue":{"taxCode":"332","dutyRate":1.57,"dutyDue":314.31}}]},"adjustments":{"overDeclarationDeclared":true,"reasonForOverDeclaration":"Submitted too much","overDeclarationProducts":[{"returnPeriod":"24AD","adjustmentQuantity":{"litres":400.04,"lpa":40.0404},"dutyDue":{"taxCode":"352","dutyRate":1.32,"dutyDue":-52.85}}],"underDeclarationDeclared":true,"reasonForUnderDeclaration":"Submitted too little","underDeclarationProducts":[{"returnPeriod":"24AC","adjustmentQuantity":{"litres":300.03,"lpa":30.0303},"dutyDue":{"taxCode":"351","dutyRate":2.32,"dutyDue":69.67}}],"spoiltProductDeclared":true,"spoiltProducts":[{"returnPeriod":"24AE","adjustmentQuantity":{"litres":500.05,"lpa":50.0505},"dutyDue":{"taxCode":"353","dutyRate":1.82,"dutyDue":-91.09}}],"drawbackDeclared":true,"drawbackProducts":[{"returnPeriod":"24AF","adjustmentQuantity":{"litres":600.06,"lpa":60.0606},"dutyDue":{"taxCode":"361","dutyRate":2.21,"dutyDue":-132.73}}],"repackagedDraughtDeclared":true,"repackagedDraughtProducts":[{"returnPeriod":"24AG","originalTaxCode":"371","originalDutyRate":0.27,"newTaxCode":"331","newDutyRate":1.27,"repackagedQuantity":{"litres":700.07,"lpa":70.0707},"dutyAdjustment":70.07}]},"dutySuspended":{"declared":true,"dutySuspendedProducts":[{"regime":"Beer","suspendedQuantity":{"litres":101.1,"lpa":1010.1011}},{"regime":"Wine","suspendedQuantity":{"litres":202.2,"lpa":2020.2022}},{"regime":"Cider","suspendedQuantity":{"litres":303.3,"lpa":3030.3033}},{"regime":"Spirits","suspendedQuantity":{"litres":404.4,"lpa":4040.4044}},{"regime":"OtherFermentedProduct","suspendedQuantity":{"litres":505.5,"lpa":5050.5055}}]},"spirits":{"spiritsDeclared":true,"spiritsProduced":{"spiritsVolumes":{"totalSpirits":123.45,"scotchWhiskey":234.56,"irishWhisky":345.67},"typesOfSpirit":["Malt","Beer","Other"],"otherSpiritTypeName":"MaltyBeer","hasOtherMaltedGrain":true,"grainsQuantities":{"maltedBarley":10,"otherMaltedGrain":11.11,"wheat":22.22,"maize":33.33,"rye":44.44,"unmaltedGrain":55.55},"otherMaltedGrainType":"Smarties","ingredientsVolumes":{"ethylene":10.1,"molasses":20.2,"beer":30.3,"wine":40.4,"madeWine":50.5,"ciderOrPerry":60.6},"otherIngredient":{"quantity":70.7,"unitOfMeasure":"Tonnes","ingredientName":"Coco Pops"}}},"totals":{"declaredDutyDue":441.53,"overDeclaration":-52.85,"underDeclaration":69.67,"spoiltProduct":-91.09,"drawback":-132.73,"repackagedDraught":70.07,"totalDutyDue":304.6}}"""
    val adrReturnCreatedDetailsJson =
      s"""{"processingDate":"2024-06-11T15:07:47.838Z","amount":$total,"chargeReference":"$chargeReference","paymentDueDate":"2024-04-25"}"""

    val adrReturnSubmission     = exampleReturnSubmissionRequest
    val returnCreatedDetails    =
      exampleReturnCreatedSuccessfulResponse(periodKey, total, now, chargeReference, submissionId).success
    val adrReturnCreatedDetails = exampleReturnCreatedDetails(periodKey, total, now, chargeReference)
  }
}
