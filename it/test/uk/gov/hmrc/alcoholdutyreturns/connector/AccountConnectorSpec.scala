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

import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.libs.json.Json
import uk.gov.hmrc.alcoholdutyreturns.models.AlcoholRegime.{Beer, Cider, OtherFermentedProduct, Spirits, Wine}
import uk.gov.hmrc.alcoholdutyreturns.models.ApprovalStatus.Approved
import uk.gov.hmrc.alcoholdutyreturns.models.ErrorResponse.{EntityNotFound, InvalidJson, UnexpectedResponse}
import uk.gov.hmrc.alcoholdutyreturns.models.ObligationStatus.Open
import uk.gov.hmrc.alcoholdutyreturns.models.{ObligationData, ReturnId, SubscriptionSummary}

class AccountConnectorSpec extends ConnectorBase {

  protected val endpointName = "alcohol-duty-accounts"

  "Alcohol Duty Connector accounts" should {

    val appaId         = "ADR00001"
    val periodKey      = "24AA"
    val alcoholRegimes = Seq(Beer, Cider, Spirits, Wine, OtherFermentedProduct)

    val subscriptionSummary = SubscriptionSummary(Approved, alcoholRegimes)
    val obligationData      = ObligationData(Open)

    "successfully get a subscription summary" in new ConnectorFixture {
      val connector = new AccountConnector(config = config, httpClient = httpClient)

      val url = config.getSubscriptionUrl(appaId)
      stubGet(url, OK, Json.toJson(subscriptionSummary).toString())

      whenReady(connector.getSubscriptionSummary(appaId).value) { result =>
        result mustBe Right(subscriptionSummary)
        verifyGet(url)
      }
    }

    "return an InvalidJson error if the get subscription summary call return an invalid response" in new ConnectorFixture {
      val connector = new AccountConnector(config = config, httpClient = httpClient)

      val url = config.getSubscriptionUrl(appaId)
      stubGet(url, OK, "invalid")

      whenReady(connector.getSubscriptionSummary(appaId).value) { result =>
        result mustBe Left(InvalidJson)
        verifyGet(url)
      }
    }

    "return a NotFound error if the get subscription summary call return a 404 response" in new ConnectorFixture {
      val connector = new AccountConnector(config = config, httpClient = httpClient)

      val url = config.getSubscriptionUrl(appaId)
      stubGet(url, NOT_FOUND, "")

      whenReady(connector.getSubscriptionSummary(appaId).value) { result =>
        result mustBe Left(EntityNotFound)
        verifyGet(url)
      }
    }

    "return a UnexpectedResponse error if the get subscription summary call return a 500 response" in new ConnectorFixture {
      val connector = new AccountConnector(config = config, httpClient = httpClient)

      val url = config.getSubscriptionUrl(appaId)
      stubGet(url, INTERNAL_SERVER_ERROR, "")

      whenReady(connector.getSubscriptionSummary(appaId).value) { result =>
        result mustBe Left(UnexpectedResponse)
        verifyGet(url)
      }
    }

    "successfully get an obligation" in new ConnectorFixture {
      val connector = new AccountConnector(config = config, httpClient = httpClient)

      val url = config.getObligationUrl(appaId, periodKey)
      stubGet(url, OK, Json.toJson(obligationData).toString())

      whenReady(connector.getObligationData(ReturnId(appaId, periodKey)).value) { result =>
        result mustBe Right(obligationData)
        verifyGet(url)
      }
    }

    "return a InvalidJson error if the get obligation data call return an invalid response" in new ConnectorFixture {
      val connector = new AccountConnector(config = config, httpClient = httpClient)

      val url = config.getObligationUrl(appaId, periodKey)
      stubGet(url, OK, "invalid")

      whenReady(connector.getObligationData(ReturnId(appaId, periodKey)).value) { result =>
        result mustBe Left(InvalidJson)
        verifyGet(url)
      }
    }

    "return a NotFound error if the get obligation data call return a 404 response" in new ConnectorFixture {
      val connector = new AccountConnector(config = config, httpClient = httpClient)

      val url = config.getObligationUrl(appaId, periodKey)
      stubGet(url, NOT_FOUND, "")

      whenReady(connector.getObligationData(ReturnId(appaId, periodKey)).value) { result =>
        result mustBe Left(EntityNotFound)
        verifyGet(url)
      }
    }

    "return a UnexpectedResponse error if the get obligation data call return a 500 response" in new ConnectorFixture {
      val connector = new AccountConnector(config = config, httpClient = httpClient)

      val url = config.getObligationUrl(appaId, periodKey)
      stubGet(url, INTERNAL_SERVER_ERROR, "")

      whenReady(connector.getObligationData(ReturnId(appaId, periodKey)).value) { result =>
        result mustBe Left(UnexpectedResponse)
        verifyGet(url)
      }
    }
  }
}
