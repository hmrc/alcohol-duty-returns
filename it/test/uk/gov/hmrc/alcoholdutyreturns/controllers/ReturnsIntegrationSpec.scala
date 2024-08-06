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
  "ReturnsController" when {
    "calling getReturn" should {
      "return 200 OK and the return response when successful" in new SetUp {
        stubAuthorised()
        stubGet(getReturnUrl, OK, Json.toJson(returnSuccess).toString())

        val response = callRoute(
          FakeRequest("GET", routes.ReturnsController.getReturn(appaId, periodKey).url)
            .withHeaders("Authorization" -> "Bearer 12345")
        )

        status(response) shouldBe OK
        contentAsJson(response) shouldBe Json.toJson(adrReturnDetails)

        verifyGet(getReturnUrl)
      }

      "return 200 OK and the return response when successful for nil return" in new SetUp {
        stubAuthorised()
        stubGet(getReturnUrl, OK, Json.toJson(nilReturnSuccess).toString())

        val response = callRoute(
          FakeRequest("GET", routes.ReturnsController.getReturn(appaId, periodKey).url)
            .withHeaders("Authorization" -> "Bearer 12345")
        )

        status(response) shouldBe OK
        contentAsJson(response) shouldBe Json.toJson(nilAdrReturnDetails)

        verifyGet(getReturnUrl)
      }
    }

    "calling submitReturn" should {
      "return 201 CREATED and the submission created response when successful" in new SetUp {
        stubAuthorised()
        stubPost(calculateDutyDueByTaxTypeUrl, OK, Json.toJson(calculateDutyDueByTaxTypeRequestForExampleSubmission).toString(), Json.toJson(calculatedDutyDueByTaxTypeForExampleSubmission).toString())
        stubPost(submitReturnUrl, CREATED, Json.toJson(returnSubmission).toString(), Json.toJson(returnCreatedSuccess).toString())

        val response = callRoute(
          FakeRequest("POST", routes.ReturnsController.submitReturn(appaId, periodKey).url)
            .withHeaders("Authorization" -> "Bearer 12345")
            .withBody(Json.toJson(adrReturnsSubmission))
        )

        status(response) shouldBe CREATED
        contentAsJson(response) shouldBe Json.toJson(adrReturnCreatedDetails)

        verifyPost(calculateDutyDueByTaxTypeUrl)
        verifyPost(submitReturnUrl)
      }

      "return 201 CREATED and the submission created response when successful for a nilReturn" in new SetUp {
        stubAuthorised()
        stubPost(calculateDutyDueByTaxTypeUrl, OK, Json.toJson(calculateDutyDueByTaxTypeRequestForExampleSubmission).toString(), Json.toJson(calculatedDutyDueByTaxTypeForExampleSubmission).toString())
        stubPost(submitReturnUrl, CREATED, Json.toJson(nilReturnSubmission).toString(), Json.toJson(nilReturnCreatedSuccess).toString())

        val response = callRoute(
          FakeRequest("POST", routes.ReturnsController.submitReturn(appaId, periodKey).url)
            .withHeaders("Authorization" -> "Bearer 12345")
            .withBody(Json.toJson(nilAdrReturnsSubmission))
        )

        status(response) shouldBe CREATED
        contentAsJson(response) shouldBe Json.toJson(nilAdrReturnCreatedDetails)

        verifyPostNeverCalled(calculateDutyDueByTaxTypeUrl)
        verifyPost(submitReturnUrl)
      }

      "return 400 BAD_REQUEST when the response fails schema validation" in new SetUp {
        stubAuthorised()
        stubPost(calculateDutyDueByTaxTypeUrl, OK, Json.toJson(calculateDutyDueByTaxTypeRequestForExampleSubmission).toString(), Json.toJson(calculatedDutyDueByTaxTypeForExampleSubmission).toString())
        stubPost(submitReturnUrl, CREATED, Json.toJson(returnSubmission).toString(), Json.toJson(returnCreatedSuccess).toString())

        val response = callRoute(
          FakeRequest("POST", routes.ReturnsController.submitReturn(appaId, periodKey).url)
            .withHeaders("Authorization" -> "Bearer 12345")
            .withBody(Json.toJson(badAdrReturnsSubmission))
        )

        status(response) shouldBe BAD_REQUEST

        verifyPost(calculateDutyDueByTaxTypeUrl)
        verifyPostNeverCalled(submitReturnUrl)
      }
    }
  }

  class SetUp {
    val periodKey: String = "24AC"
    val retId = returnId.copy(periodKey = periodKey)
    val zero              = BigDecimal(0)
    val total             = BigDecimal("12345.67")
    val now               = Instant.now()

    val getReturnUrl = config.getReturnUrl(retId)
    val submitReturnUrl = config.submitReturnUrl
    val calculateDutyDueByTaxTypeUrl = config.getCalculateDutyDueByTaxTypeUrl

    val returnSuccess = successfulReturnExample(appaId, periodKey, submissionId, chargeReference, now)
    val adrReturnDetails = convertedReturnDetails(periodKey, now)

    val nilReturnSuccess = nilReturnExample(appaId, periodKey, submissionId, now)
    val nilAdrReturnDetails = nilReturnDetails(periodKey, now)

    val adrReturnsSubmission = exampleReturnSubmissionRequest
    val nilAdrReturnsSubmission = exampleNilReturnSubmissionRequest
    val badAdrReturnsSubmission = exampleReturnSubmissionRequest.copy(totals = adrReturnsSubmission.totals.copy(totalDutyDue = BigDecimal("99.999")))

    val returnSubmission = returnCreateSubmission(periodKey)
    val returnCreatedSuccess    =
      exampleReturnCreatedSuccessfulResponse(periodKey, total, now, chargeReference, submissionId)
    val nilReturnSubmission = nilReturnCreateSubmission(periodKey)
    val nilReturnCreatedSuccess    =
      exampleReturnCreatedSuccessfulResponse(periodKey, zero, now, chargeReference, submissionId)

    val adrReturnCreatedDetails = exampleReturnCreatedDetails(periodKey, total, now, chargeReference)
    val nilAdrReturnCreatedDetails = exampleReturnCreatedDetails(periodKey, zero, now, chargeReference)
  }
}
