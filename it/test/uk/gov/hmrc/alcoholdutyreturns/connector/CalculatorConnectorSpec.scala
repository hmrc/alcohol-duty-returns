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
import uk.gov.hmrc.alcoholdutyreturns.models.ErrorResponse

class CalculatorConnectorSpec extends ISpecBase {
  "Calculator Connector" when {
    "calculateDutyDueByTaxType is called" should {
      "successfully calculate duty due by tax type" in new SetUp {
        stubPost(calculateDutyDueByTaxTypeUrl, OK, Json.toJson(calculateDutyDueByTaxTypeRequest).toString(), Json.toJson(calculatedDutyDueByTaxType).toString())
        whenReady(connector.calculateDutyDueByTaxType(calculateDutyDueByTaxTypeRequest).value, timeout = Timeout(Span(3, Seconds))) { result =>
          result mustBe Right(calculatedDutyDueByTaxType)
          verifyPost(calculateDutyDueByTaxTypeUrl)
        }
      }

      "return an InvalidJson error if the call returns an invalid response" in new SetUp {
        stubPost(calculateDutyDueByTaxTypeUrl, OK, Json.toJson(calculateDutyDueByTaxTypeRequest).toString(), "invalid")
        whenReady(connector.calculateDutyDueByTaxType(calculateDutyDueByTaxTypeRequest).value, timeout = Timeout(Span(3, Seconds))) { result =>
          result mustBe Left(ErrorResponse.InvalidJson)
          verifyPost(calculateDutyDueByTaxTypeUrl)
        }
      }

      "return a BadRequest error if the call returns a 400 response" in new SetUp {
        stubPost(calculateDutyDueByTaxTypeUrl, BAD_REQUEST, Json.toJson(calculateDutyDueByTaxTypeRequest).toString(), "")
        whenReady(connector.calculateDutyDueByTaxType(calculateDutyDueByTaxTypeRequest).value, timeout = Timeout(Span(3, Seconds))) { result =>
          result mustBe Left(ErrorResponse.BadRequest)
          verifyPost(calculateDutyDueByTaxTypeUrl)
        }
      }

      "return a UnexpectedResponse error if the call returns a 500 response" in new SetUp {
        stubPost(calculateDutyDueByTaxTypeUrl, INTERNAL_SERVER_ERROR, Json.toJson(calculateDutyDueByTaxTypeRequest).toString(), Json.toJson(internalServerError).toString())
        whenReady(connector.calculateDutyDueByTaxType(calculateDutyDueByTaxTypeRequest).value) { result =>
          result mustBe Left(ErrorResponse.UnexpectedResponse)
          verifyPost(calculateDutyDueByTaxTypeUrl)
        }
      }
    }
  }

  class SetUp {
    val connector = app.injector.instanceOf[CalculatorConnector]
    val calculateDutyDueByTaxTypeUrl = config.getCalculateDutyDueByTaxTypeUrl
  }
}
