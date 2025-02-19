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
import uk.gov.hmrc.alcoholdutyreturns.models.{ErrorCodes, SubscriptionSummary}

import java.time.LocalDate

class AccountConnectorSpec extends ISpecBase {

  "getData must" - {
    "return an InvalidJson error if the call returns an invalid response" in new SetUp {
      stubGet(dataTestUrl, OK, "invalid")
      whenReady(connector.getData[DataTestType](dataTestUrl).value) { result =>
        result mustBe Left(ErrorCodes.invalidJson)
        verifyGet(dataTestUrl)
      }
    }

    "return a NotFound error if the call returns a 404 response" in new SetUp {
      stubGet(dataTestUrl, NOT_FOUND, "")
      whenReady(connector.getData[DataTestType](dataTestUrl).value) { result =>
        result mustBe Left(ErrorCodes.entityNotFound)
        verifyGet(dataTestUrl)
      }
    }

    "return an UnexpectedResponse error if the call returns a 500 response" in new SetUp {
      stubGet(dataTestUrl, INTERNAL_SERVER_ERROR, "")
      whenReady(connector.getData[DataTestType](dataTestUrl).value) { result =>
        result mustBe Left(ErrorCodes.unexpectedResponse)
        verifyGet(dataTestUrl)
      }
    }
  }

  "getSubscriptionSummary must" - {
    "successfully get a subscription summary" in new SetUp {
      stubGet(subscriptionUrl, OK, Json.toJson(subscriptionSummary).toString())
      whenReady(connector.getSubscriptionSummary(appaId).value, timeout = Timeout(Span(3, Seconds))) { result =>
        result mustBe Right(subscriptionSummary)
        verifyGet(subscriptionUrl)
      }
    }
  }

  "getOpenObligationData must" - {
    "successfully get an obligation" in new SetUp {
      stubGet(openObligationUrl, OK, Json.toJson(obligationData).toString())
      whenReady(connector.getOpenObligationData(returnId).value) { result =>
        result mustBe Right(obligationData)
        verifyGet(openObligationUrl)
      }
    }
  }

  "getObligationData must" - {
    "successfully get obligations" in new SetUp {
      stubGet(obligationUrl, OK, Json.toJson(Seq(obligationData)).toString())
      whenReady(connector.getObligationData(appaId).value) { result =>
        result mustBe Right(Seq(obligationData))
        verifyGet(obligationUrl)
      }
    }
  }

  class SetUp {
    val connector         = app.injector.instanceOf[AccountConnector]
    val subscriptionUrl   = config.getSubscriptionSummaryUrl(appaId)
    val openObligationUrl = config.getOpenObligationDataUrl(returnId)
    val obligationUrl     = config.getObligationDataUrl(appaId)
    val obligationData    = getObligationData(LocalDate.now())
    val dataTestUrl       = subscriptionUrl

    type DataTestType = SubscriptionSummary
  }
}
