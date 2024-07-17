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
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatest.time.{Seconds, Span}
import play.api.libs.json.Json
import uk.gov.hmrc.alcoholdutyreturns.base.ISpecBase
import uk.gov.hmrc.alcoholdutyreturns.connector.helpers.{HIPHeaders, RandomUUIDGenerator}
import uk.gov.hmrc.alcoholdutyreturns.models.ErrorResponse

import java.time.Instant

class ReturnsConnectorSpec extends ISpecBase {
  protected val endpointName = "returns"

  "Returns Connector accounts" should {
    "successfully get a return" in new SetUp {
      stubGet(returnsUrl, OK, Json.toJson(returnsData).toString())
      whenReady(connector.getReturn(returnId).value, timeout = Timeout(Span(3, Seconds))) { result =>
        result mustBe Right(returnsData.success)
        verifyGet(returnsUrl)
      }
    }

    "return an InvalidJson error if the get subscription summary call returns an invalid response" in new SetUp {
      stubGet(returnsUrl, OK, "invalid")
      whenReady(connector.getReturn(returnId).value, timeout = Timeout(Span(3, Seconds))) { result =>
        result mustBe Left(ErrorResponse.InvalidJson)
        verifyGet(returnsUrl)
      }
    }

    "return a BadRequest error if the get subscription summary call returns a 400 response" in new SetUp {
      stubGet(returnsUrl, BAD_REQUEST, Json.toJson(processingError(now)).toString())
      whenReady(connector.getReturn(returnId).value, timeout = Timeout(Span(3, Seconds))) { result =>
        result mustBe Left(ErrorResponse.BadRequest)
        verifyGet(returnsUrl)
      }
    }

    "return a NotFound error if the get subscription summary call returns a 404 response" in new SetUp {
      stubGet(returnsUrl, NOT_FOUND, "")
      whenReady(connector.getReturn(returnId).value, timeout = Timeout(Span(3, Seconds))) { result =>
        result mustBe Left(ErrorResponse.EntityNotFound)
        verifyGet(returnsUrl)
      }
    }

    "return a UnexpectedResponse error if the get subscription summary call return a 500 response" in new SetUp {
      stubGet(returnsUrl, INTERNAL_SERVER_ERROR, Json.toJson(internalServerError).toString())
      whenReady(connector.getReturn(returnId).value) { result =>
        result mustBe Left(ErrorResponse.UnexpectedResponse)
        verifyGet(returnsUrl)
      }
    }
  }

  class SetUp extends ConnectorFixture {
    val connector = new ReturnsConnector(config = config, httpClient = httpClient, headers = new HIPHeaders(new RandomUUIDGenerator(), config, clock))
    val returnsUrl = config.getReturnsUrl(returnId)
    val periodKey = "24AC"

    val now = Instant.now()
    val returnsData = successfulReturnsExample(appaId, periodKey, submissionId, chargeReference, now)
  }
}
