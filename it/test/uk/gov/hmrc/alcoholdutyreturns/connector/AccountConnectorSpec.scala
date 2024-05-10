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
import uk.gov.hmrc.alcoholdutyreturns.models.AlcoholRegime.{Beer, Cider, OtherFermentedProduct, Spirits, Wine}
import uk.gov.hmrc.alcoholdutyreturns.models.ApprovalStatus.Approved
import uk.gov.hmrc.alcoholdutyreturns.models.ErrorResponse.{EntityNotFound, InvalidJson, UnexpectedResponse}
import uk.gov.hmrc.alcoholdutyreturns.models.ObligationStatus.Open
import uk.gov.hmrc.alcoholdutyreturns.models.{ObligationData, ReturnId, SubscriptionSummary}

import java.time.LocalDate

class AccountConnectorSpec extends ISpecBase {

  protected val endpointName = "alcohol-duty-accounts"

  val appaId         = appaIdGen.sample.get
  val periodKey      = periodKeyGen.sample.get
  val alcoholRegimes = Seq(Beer, Cider, Spirits, Wine, OtherFermentedProduct)

  val subscriptionSummary = SubscriptionSummary(Approved, alcoholRegimes)
  val obligationData = ObligationData(
    status = Open,
    fromDate = LocalDate.now(),
    toDate = LocalDate.now(),
    dueDate = LocalDate.now()
  )

  "Alcohol Duty Connector accounts" should {

    "successfully get a subscription summary" in new SetUp {
      stubGet(subscriptionUrl, OK, Json.toJson(subscriptionSummary).toString())
      whenReady(connector.getSubscriptionSummary(appaId).value, timeout = Timeout(Span(3, Seconds))) { result =>
        result mustBe Right(subscriptionSummary)
        verifyGet(subscriptionUrl)
      }
    }

    "return an InvalidJson error if the get subscription summary call return an invalid response" in new SetUp {
      stubGet(subscriptionUrl, OK, "invalid")
      whenReady(connector.getSubscriptionSummary(appaId).value) { result =>
        result mustBe Left(InvalidJson)
        verifyGet(subscriptionUrl)
      }
    }

    "return a NotFound error if the get subscription summary call return a 404 response" in new SetUp {
      stubGet(subscriptionUrl, NOT_FOUND, "")
      whenReady(connector.getSubscriptionSummary(appaId).value) { result =>
        result mustBe Left(EntityNotFound)
        verifyGet(subscriptionUrl)
      }
    }

    "return a UnexpectedResponse error if the get subscription summary call return a 500 response" in new SetUp {
      stubGet(subscriptionUrl, INTERNAL_SERVER_ERROR, "")
      whenReady(connector.getSubscriptionSummary(appaId).value) { result =>
        result mustBe Left(UnexpectedResponse)
        verifyGet(subscriptionUrl)
      }
    }

    "successfully get an obligation" in new SetUp {
      stubGet(obligationUrl, OK, Json.toJson(obligationData).toString())
      whenReady(connector.getOpenObligationData(ReturnId(appaId, periodKey)).value) { result =>
        result mustBe Right(obligationData)
        verifyGet(obligationUrl)
      }
    }

    "return a InvalidJson error if the get obligation data call return an invalid response" in new SetUp {
      stubGet(obligationUrl, OK, "invalid")
      whenReady(connector.getOpenObligationData(ReturnId(appaId, periodKey)).value) { result =>
        result mustBe Left(InvalidJson)
        verifyGet(obligationUrl)
      }
    }

    "return a NotFound error if the get obligation data call return a 404 response" in new SetUp {
      stubGet(obligationUrl, NOT_FOUND, "")
      whenReady(connector.getOpenObligationData(ReturnId(appaId, periodKey)).value) { result =>
        result mustBe Left(EntityNotFound)
        verifyGet(obligationUrl)
      }
    }

    "return a UnexpectedResponse error if the get obligation data call return a 500 response" in new SetUp {
      stubGet(obligationUrl, INTERNAL_SERVER_ERROR, "")
      whenReady(connector.getOpenObligationData(ReturnId(appaId, periodKey)).value) { result =>
        result mustBe Left(UnexpectedResponse)
        verifyGet(obligationUrl)
      }
    }
  }

  class SetUp extends ConnectorFixture {
    val connector = new AccountConnector(config = config, httpClient = httpClient)
    val subscriptionUrl = config.getSubscriptionSummaryUrl(appaId)
    val obligationUrl = config.getOpenObligationDataUrl(appaId, periodKey)
  }
}
