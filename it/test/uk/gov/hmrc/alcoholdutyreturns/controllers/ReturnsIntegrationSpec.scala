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

package uk.gov.hmrc.alcoholdutyreturns.controllers

import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.alcoholdutyreturns.base.ISpecBase

import java.time.Instant

class ReturnsIntegrationSpec extends ISpecBase {
  "ReturnsController when" - {
    "calling getReturn must" - {
      "return 200 OK and the return response when successful" in new SetUp {
        stubAuthorised(appaId)

        stubGet(getReturnUrl, OK, returnSuccessJson)

        val response = callRoute(
          FakeRequest("GET", routes.ReturnsController.getReturn(appaId, periodKey).url)
            .withHeaders("Authorization" -> "Bearer 12345")
        )

        status(response)        mustBe OK
        contentAsJson(response) mustBe Json.toJson(adrReturnDetails)

        verifyGet(getReturnUrl)
      }

      "return 200 OK and the return response when successful for nil return" in new SetUp {
        stubAuthorised(appaId)

        stubGet(getReturnUrl, OK, nilReturnSuccessJson)

        val response = callRoute(
          FakeRequest("GET", routes.ReturnsController.getReturn(appaId, periodKey).url)
            .withHeaders("Authorization" -> "Bearer 12345")
        )

        status(response)        mustBe OK
        contentAsJson(response) mustBe Json.toJson(nilAdrReturnDetails)

        verifyGet(getReturnUrl)
      }
    }

    "calling submitReturn must" - {
      "return 201 CREATED and the submission created response when successful" in new SetUp {
        stubAuthorised(appaId)

        stubPost(
          calculateDutyDueByTaxTypeUrl,
          OK,
          Json.toJson(calculateDutyDueByTaxTypeRequestForExampleSubmission).toString(),
          Json.toJson(calculatedDutyDueByTaxTypeForExampleSubmission).toString()
        )
        stubPost(submitReturnUrl, CREATED, returnSubmissionJson, returnCreatedSuccessJson)

        val response = callRoute(
          FakeRequest("POST", routes.ReturnsController.submitReturn(appaId, periodKey).url)
            .withHeaders("Authorization" -> "Bearer 12345")
            .withBody(Json.toJson(adrReturnsSubmission))
        )

        status(response)        mustBe CREATED
        contentAsJson(response) mustBe Json.toJson(adrReturnCreatedDetails)

        verifyPost(calculateDutyDueByTaxTypeUrl)
        verifyPost(submitReturnUrl)
      }

      "return 201 CREATED and the submission created response when successful for a nilReturn" in new SetUp {
        stubAuthorised(appaId)

        stubPost(
          calculateDutyDueByTaxTypeUrl,
          OK,
          Json.toJson(calculateDutyDueByTaxTypeRequestForExampleSubmission).toString(),
          Json.toJson(calculatedDutyDueByTaxTypeForExampleSubmission).toString()
        )
        stubPost(submitReturnUrl, CREATED, nilReturnSubmissionJson, nilReturnCreatedSuccessJson)

        val response = callRoute(
          FakeRequest("POST", routes.ReturnsController.submitReturn(appaId, periodKey).url)
            .withHeaders("Authorization" -> "Bearer 12345")
            .withBody(Json.toJson(nilAdrReturnsSubmission))
        )

        status(response)        mustBe CREATED
        contentAsJson(response) mustBe Json.toJson(nilAdrReturnCreatedDetails)

        verifyPostNeverCalled(calculateDutyDueByTaxTypeUrl)
        verifyPost(submitReturnUrl)
      }

      "return 400 BAD_REQUEST when the response fails schema validation" in new SetUp {
        stubAuthorised(appaId)

        stubPost(
          calculateDutyDueByTaxTypeUrl,
          OK,
          Json.toJson(calculateDutyDueByTaxTypeRequestForExampleSubmission).toString(),
          Json.toJson(calculatedDutyDueByTaxTypeForExampleSubmission).toString()
        )
        stubPost(submitReturnUrl, CREATED, returnSubmissionJson, returnCreatedSuccessJson)

        val response = callRoute(
          FakeRequest("POST", routes.ReturnsController.submitReturn(appaId, periodKey).url)
            .withHeaders("Authorization" -> "Bearer 12345")
            .withBody(Json.toJson(badAdrReturnsSubmission))
        )

        status(response) mustBe BAD_REQUEST

        verifyPost(calculateDutyDueByTaxTypeUrl)
        verifyPostNeverCalled(submitReturnUrl)
      }
    }
  }

  class SetUp {
    val periodKey: String = "24AC"
    val retId             = returnId.copy(periodKey = periodKey)
    val zero              = BigDecimal(0)
    val total             = BigDecimal("12345.67")
    val now               = Instant.ofEpochMilli(clockMillis)

    val getReturnUrl                 = config.getReturnUrl(retId)
    val submitReturnUrl              = config.submitReturnUrl
    val calculateDutyDueByTaxTypeUrl = config.getCalculateDutyDueByTaxTypeUrl

    val returnSuccessJson =
      s"""{"success":{"processingDate":"2024-06-11T15:07:47.838Z","idDetails":{"adReference":"$appaId","submissionID":"$submissionId"},"chargeDetails":{"periodKey":"24AC","chargeReference":"XA4732160686346","periodFrom":"2024-03-01","periodTo":"2024-03-31","receiptDate":"2024-06-11T15:07:47.838Z"},"alcoholProducts":{"alcoholProductsProducedFilled":"1","regularReturn":[{"taxType":"301","dutyRate":5.27,"litresProduced":240000.02,"litresOfPureAlcohol":12041,"dutyDue":63456.07}]},"overDeclaration":{"overDeclFilled":"1","reasonForOverDecl":"Why over-declared","overDeclarationProducts":[{"returnPeriodAffected":"24AB","taxType":"302","dutyRate":3.56,"litresProduced":5000.79,"litresOfPureAlcohol":100.58,"dutyDue":358.07}]},"underDeclaration":{"underDeclFilled":"1","reasonForUnderDecl":"Why under-declared","underDeclarationProducts":[{"returnPeriodAffected":"24AA","taxType":"301","dutyRate":5.27,"litresProduced":49000.78,"litresOfPureAlcohol":989,"dutyDue":5212.03}]},"spoiltProduct":{"spoiltProdFilled":"1","spoiltProductProducts":[{"returnPeriodAffected":"23AL","taxType":"305","dutyRate":1.75,"litresProduced":50000.69,"litresOfPureAlcohol":1000.94,"dutyDue":1751.65}]},"drawback":{"drawbackFilled":"1","drawbackProducts":[{"returnPeriodAffected":"23AK","taxType":"309","dutyRate":5.12,"litresProduced":60000.02,"litresOfPureAlcohol":1301.11,"dutyDue":6661.69}]},"repackagedDraught":{"repDraughtFilled":"1","repackagedDraughtProducts":[{"returnPeriodAffected":"23AJ","originaltaxType":"300","originaldutyRate":0.64,"newTaxType":"304","dutyRate":12.76,"litresOfRepackaging":5000.97,"litresOfPureAlcohol":100.81,"dutyDue":1221.82}]},"totalDutyDuebyTaxType":[{"taxType":"301","totalDutyDueTaxType":1}],"totalDutyDue":{"totalDutyDueAlcoholProducts":63456.07,"totalDutyOverDeclaration":358.07,"totalDutyUnderDeclaration":5212.03,"totalDutySpoiltProduct":1751.65,"totalDutyDrawback":6661.69,"totalDutyRepDraughtProducts":1221.82,"totalDutyDue":61118.51},"netDutySuspension":{"netDutySuspensionFilled":"1","netDutySuspensionProducts":{"totalLtsBeer":0.15,"totalLtsWine":0.44,"totalLtsCider":0.38,"totalLtsSpirit":0.02,"totalLtsOtherFermented":0.02,"totalLtsPureAlcoholBeer":0.4248,"totalLtsPureAlcoholWine":0.5965,"totalLtsPureAlcoholCider":0.0379,"totalLtsPureAlcoholSpirit":0.2492,"totalLtsPureAlcoholOtherFermented":0.1894}},"spiritsProduced":{"spiritsProdFilled":"1","spiritsProduced":{"totalSpirits":0.05,"scotchWhiskey":0.26,"irishWhisky":0.16,"typeOfSpirit":["03"],"typeOfSpiritOther":"Coco Pops Vodka","code1MaltedBarley":0.17,"code2Other":"1","maltedGrainQuantity":0.55,"maltedGrainType":"wheat","code3Wheat":0.8,"code4Maize":0.67,"code5Rye":0.13,"code6UnmaltedGrain":0.71,"code7EthyleneGas":0.45,"code8Molasses":0.31,"code9Beer":0.37,"code10Wine":0.76,"code11MadeWine":0.6,"code12CiderOrPerry":0.04,"code13Other":"1","otherMaterialsQuantity":0.26,"otherMaterialsUom":"01","otherMaterialsType":"Coco Pops"}}}}"""
    val adrReturnDetails  = convertedReturnDetails(periodKey, now)

    val nilReturnSuccessJson =
      s"""{"success":{"processingDate":"2024-06-11T15:07:47.838Z","idDetails":{"adReference":"$appaId","submissionID":"$submissionId"},"chargeDetails":{"periodKey":"24AC","periodFrom":"2024-03-01","periodTo":"2024-03-31","receiptDate":"2024-06-11T15:07:47.838Z"},"alcoholProducts":{"alcoholProductsProducedFilled":"0"},"overDeclaration":{"overDeclFilled":"0"},"underDeclaration":{"underDeclFilled":"0"},"spoiltProduct":{"spoiltProdFilled":"0"},"drawback":{"drawbackFilled":"0"},"repackagedDraught":{"repDraughtFilled":"0"},"totalDutyDue":{"totalDutyDueAlcoholProducts":0,"totalDutyOverDeclaration":0,"totalDutyUnderDeclaration":0,"totalDutySpoiltProduct":0,"totalDutyDrawback":0,"totalDutyRepDraughtProducts":0,"totalDutyDue":0},"netDutySuspension":{"netDutySuspensionFilled":"0"}}}"""
    val nilAdrReturnDetails  = nilReturnDetails(periodKey, now)

    val adrReturnsSubmission    = exampleReturnSubmissionRequest
    val nilAdrReturnsSubmission = exampleNilReturnSubmissionRequest
    val badAdrReturnsSubmission = exampleReturnSubmissionRequest.copy(totals =
      adrReturnsSubmission.totals.copy(totalDutyDue = BigDecimal("99.999"))
    )

    val returnSubmissionJson        =
      """{"periodKey":"24AC","alcoholProducts":{"alcoholProductsProducedFilled":"1","regularReturn":[{"taxType":"331","dutyRate":1.27,"litresProduced":1000.1,"litresOfPureAlcohol":100.101,"dutyDue":127.12},{"taxType":"332","dutyRate":1.57,"litresProduced":2000.21,"litresOfPureAlcohol":200.2022,"dutyDue":314.31}]},"overDeclaration":{"overDeclFilled":"1","reasonForOverDecl":"Submitted too much","overDeclarationProducts":[{"returnPeriodAffected":"24AD","taxType":"352","dutyRate":1.32,"litresProduced":400.04,"litresOfPureAlcohol":40.0404,"dutyDue":52.85}]},"underDeclaration":{"underDeclFilled":"1","reasonForUnderDecl":"Submitted too little","underDeclarationProducts":[{"returnPeriodAffected":"24AC","taxType":"351","dutyRate":2.32,"litresProduced":300.03,"litresOfPureAlcohol":30.0303,"dutyDue":69.67}]},"spoiltProduct":{"spoiltProdFilled":"1","spoiltProductProducts":[{"returnPeriodAffected":"24AE","taxType":"353","dutyRate":1.82,"litresProduced":500.05,"litresOfPureAlcohol":50.0505,"dutyDue":91.09}]},"drawback":{"drawbackFilled":"1","drawbackProducts":[{"returnPeriodAffected":"24AF","taxType":"361","dutyRate":2.21,"litresProduced":600.06,"litresOfPureAlcohol":60.0606,"dutyDue":132.73}]},"repackagedDraught":{"repDraughtFilled":"1","repackagedDraughtProducts":[{"returnPeriodAffected":"24AG","originaltaxType":"371","originaldutyRate":0.27,"newTaxType":"331","dutyRate":1.27,"litresOfRepackaging":700.07,"litresOfPureAlcohol":70.0707,"dutyDue":70.07}]},"totalDutyDuebyTaxType":[{"taxType":"332","totalDutyDueTaxType":314.31},{"taxType":"351","totalDutyDueTaxType":69.67},{"taxType":"361","totalDutyDueTaxType":-132.73},{"taxType":"353","totalDutyDueTaxType":-91.09},{"taxType":"352","totalDutyDueTaxType":-52.85},{"taxType":"331","totalDutyDueTaxType":197.19}],"totalDutyDue":{"totalDutyDueAlcoholProducts":441.53,"totalDutyOverDeclaration":52.85,"totalDutyUnderDeclaration":69.67,"totalDutySpoiltProduct":91.09,"totalDutyDrawback":132.73,"totalDutyRepDraughtProducts":70.07,"totalDutyDue":304.6},"netDutySuspension":{"netDutySuspensionFilled":"1","netDutySuspensionProducts":{"totalLtsBeer":101.1,"totalLtsWine":202.2,"totalLtsCider":303.3,"totalLtsSpirit":404.4,"totalLtsOtherFermented":505.5,"totalLtsPureAlcoholBeer":1010.1011,"totalLtsPureAlcoholWine":2020.2022,"totalLtsPureAlcoholCider":3030.3033,"totalLtsPureAlcoholSpirit":4040.4044,"totalLtsPureAlcoholOtherFermented":5050.5055}},"spiritsProduced":{"spiritsProdFilled":"1","spiritsProduced":{"totalSpirits":123.45,"scotchWhiskey":234.56,"irishWhisky":345.67,"typeOfSpirit":["01","05","08"],"typeOfSpiritOther":"MaltyBeer","code1MaltedBarley":10,"code2Other":"1","maltedGrainQuantity":11.11,"maltedGrainType":"Smarties","code3Wheat":22.22,"code4Maize":33.33,"code5Rye":44.44,"code6UnmaltedGrain":55.55,"code7EthyleneGas":10.1,"code8Molasses":20.2,"code9Beer":30.3,"code10Wine":40.4,"code11MadeWine":50.5,"code12CiderOrPerry":60.6,"code13Other":"1","otherMaterialsQuantity":70.7,"otherMaterialsUom":"01","otherMaterialsType":"Coco Pops"}}}"""
    val returnCreatedSuccessJson    =
      s"""{"success":{"processingDate":"2024-06-11T15:07:47.838Z","adReference":"$appaId","amount":12345.67,"chargeReference":"$chargeReference","paymentDueDate":"2024-04-25","submissionID":"$submissionId"}}"""
    val nilReturnSubmissionJson     =
      """{"periodKey":"24AC","alcoholProducts":{"alcoholProductsProducedFilled":"0"},"overDeclaration":{"overDeclFilled":"0"},"underDeclaration":{"underDeclFilled":"0"},"spoiltProduct":{"spoiltProdFilled":"0"},"drawback":{"drawbackFilled":"0"},"repackagedDraught":{"repDraughtFilled":"0"},"totalDutyDue":{"totalDutyDueAlcoholProducts":0,"totalDutyOverDeclaration":0,"totalDutyUnderDeclaration":0,"totalDutySpoiltProduct":0,"totalDutyDrawback":0,"totalDutyRepDraughtProducts":0,"totalDutyDue":0},"netDutySuspension":{"netDutySuspensionFilled":"0"}}"""
    val nilReturnCreatedSuccessJson =
      s"""{"success":{"processingDate":"2024-06-11T15:07:47.838Z","adReference":"$appaId","amount":0,"submissionID":"$submissionId"}}"""

    val adrReturnCreatedDetails    = exampleReturnCreatedDetails(periodKey, total, now, chargeReference)
    val nilAdrReturnCreatedDetails = exampleReturnCreatedDetails(periodKey, zero, now, chargeReference)
  }
}
