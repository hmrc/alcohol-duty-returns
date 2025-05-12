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

package uk.gov.hmrc.alcoholdutyreturns.connector

import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.time.{Seconds, Span}
import play.api.libs.json.Json
import uk.gov.hmrc.alcoholdutyreturns.base.ISpecBase
import uk.gov.hmrc.alcoholdutyreturns.models.{DownstreamErrorsDetails, DuplicateSubmissionError, ErrorCodes}

import java.time.Instant

class ReturnsConnectorSpec extends ISpecBase {
  "Returns Connector when" - {
    "getReturn is called must" - {
      "successfully get a return" in new SetUp {
        stubGet(getReturnUrl, OK, Json.toJson(returnData).toString())
        whenReady(connector.getReturn(returnId), timeout = Timeout(Span(3, Seconds))) { result =>
          result mustBe Right(returnData.success)
          verifyGet(getReturnUrl)
        }
      }

      "return an InvalidJson error if the call returns an invalid response" in new SetUp {
        stubGet(getReturnUrl, OK, "invalid")
        whenReady(connector.getReturn(returnId), timeout = Timeout(Span(3, Seconds))) { result =>
          result mustBe Left(ErrorCodes.invalidJson)
          verifyGet(getReturnUrl)
        }
      }

      "return a BadRequest error if the call returns a 400 response without retry" in new SetUp {
        stubGet(getReturnUrl, BAD_REQUEST, Json.toJson(processingError(now)).toString())
        whenReady(connectorWithRetry.getReturn(returnId), timeout = Timeout(Span(3, Seconds))) { result =>
          result mustBe Left(ErrorCodes.badRequest)
          verifyGetWithoutRetry(getReturnUrl)
        }
      }

      "return a NotFound error if the call returns a 404 response without retry" in new SetUp {
        stubGet(getReturnUrl, NOT_FOUND, "")
        whenReady(connectorWithRetry.getReturn(returnId), timeout = Timeout(Span(3, Seconds))) { result =>
          result mustBe Left(ErrorCodes.entityNotFound)
          verifyGetWithoutRetry(getReturnUrl)
        }
      }

      "return an UnexpectedResponse error if the call returns a 422 response without retry" in new SetUp {
        stubGet(getReturnUrl, UNPROCESSABLE_ENTITY, "")
        whenReady(connectorWithRetry.getReturn(returnId), timeout = Timeout(Span(3, Seconds))) { result =>
          result mustBe Left(ErrorCodes.unexpectedResponse)
          verifyGetWithoutRetry(getReturnUrl)
        }
      }

      "return an UnexpectedResponse error if the call return a 500 response with retry" in new SetUp {
        stubGet(getReturnUrl, INTERNAL_SERVER_ERROR, Json.toJson(internalServerError).toString())
        whenReady(connectorWithRetry.getReturn(returnId)) { result =>
          result mustBe Left(ErrorCodes.unexpectedResponse)
          verifyGetWithRetry(getReturnUrl)
        }
      }
    }

    "submitReturn is called must" - {
      "successfully submit a return" in new SetUp {
        stubPost(
          submitReturnUrl,
          CREATED,
          Json.toJson(returnSubmission).toString(),
          Json.toJson(returnCreated).toString()
        )
        whenReady(connector.submitReturn(returnSubmission, appaId).value, timeout = Timeout(Span(3, Seconds))) {
          result =>
            result mustBe Right(returnCreated.success)
            verifyPost(submitReturnUrl)
        }
      }

      "return an UnexpectedResponse error if the call returns an invalid response json" in new SetUp {
        stubPost(submitReturnUrl, CREATED, Json.toJson(returnSubmission).toString(), "invalid")
        whenReady(connector.submitReturn(returnSubmission, id).value, timeout = Timeout(Span(3, Seconds))) { result =>
          result mustBe Left(ErrorCodes.unexpectedResponse)
          verifyPost(submitReturnUrl)
        }
      }

      "return a BadRequest error if the call returns a 400 response" in new SetUp {
        stubPost(
          submitReturnUrl,
          BAD_REQUEST,
          Json.toJson(returnSubmission).toString(),
          Json.toJson(processingError(now)).toString()
        )
        whenReady(connector.submitReturn(returnSubmission, id).value, timeout = Timeout(Span(3, Seconds))) { result =>
          result mustBe Left(ErrorCodes.badRequest)
          verifyPost(submitReturnUrl)
        }
      }

      "return a NotFound error if the call returns a 404 response" in new SetUp {
        stubPost(submitReturnUrl, NOT_FOUND, Json.toJson(returnSubmission).toString(), "")
        whenReady(connector.submitReturn(returnSubmission, id).value, timeout = Timeout(Span(3, Seconds))) { result =>
          result mustBe Left(ErrorCodes.entityNotFound)
          verifyPost(submitReturnUrl)
        }
      }

      "return an UnprocessableEntity if the call returns a 422 indicating a duplicate submission" in new SetUp {
        stubPost(
          submitReturnUrl,
          UNPROCESSABLE_ENTITY,
          Json.toJson(returnSubmission).toString(),
          Json.toJson(duplicateSubmission044).toString()
        )
        whenReady(connector.submitReturn(returnSubmission, id).value, timeout = Timeout(Span(3, Seconds))) { result =>
          result mustBe Left(ErrorCodes.duplicateSubmission)
          verifyPost(submitReturnUrl)
        }
      }

      "return an UnexpectedResponse error if the call returns a 422 with a different code in the response body" in new SetUp {
        stubPost(
          submitReturnUrl,
          UNPROCESSABLE_ENTITY,
          Json.toJson(returnSubmission).toString(),
          Json.toJson(duplicateSubmissionWrongCode).toString()
        )
        whenReady(connector.submitReturn(returnSubmission, id).value, timeout = Timeout(Span(3, Seconds))) { result =>
          result mustBe Left(ErrorCodes.unexpectedResponse)
          verifyPost(submitReturnUrl)
        }
      }

      "return an UnexpectedResponse error if the call returns a 500 response" in new SetUp {
        stubPost(
          submitReturnUrl,
          INTERNAL_SERVER_ERROR,
          Json.toJson(returnSubmission).toString(),
          Json.toJson(internalServerError).toString()
        )
        whenReady(connector.submitReturn(returnSubmission, id).value) { result =>
          result mustBe Left(ErrorCodes.unexpectedResponse)
          verifyPost(submitReturnUrl)
        }
      }
    }
  }

  class SetUp {
    val connector       = appWithHttpClientV2.injector.instanceOf[ReturnsConnector]
    val connectorWithRetry = appWithHttpClientV2WithRetry.injector.instanceOf[ReturnsConnector]
    val getReturnUrl    = config.getReturnUrl(returnId)
    val submitReturnUrl = config.submitReturnUrl

    val periodKey = "24AA"
    val id        = appaId
    val now       = Instant.now(clock)

    val returnData       = successfulReturnExample(id, periodKey, submissionId, chargeReference, now)
    val returnSubmission = returnCreateSubmission(periodKey)
    val returnCreated    = exampleReturnCreatedSuccessfulResponse(
      periodKey,
      returnSubmission.totalDutyDue.totalDutyDue,
      now,
      chargeReference,
      submissionId
    )

    val duplicateSubmission044       = DuplicateSubmissionError(
      DownstreamErrorsDetails(now, "044", "Tax Obligation Already Fulfilled")
    )
    val duplicateSubmissionWrongCode = DuplicateSubmissionError(
      DownstreamErrorsDetails(now, "888", "Wrong code")
    )
  }
}
