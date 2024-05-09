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

package uk.gov.hmrc.alcoholdutyreturns.service

import cats.data.EitherT
import org.mockito.ArgumentMatchers.any
import play.api.libs.json.Json
import uk.gov.hmrc.alcoholdutyreturns.base.SpecBase
import uk.gov.hmrc.alcoholdutyreturns.connector.AccountConnector
import uk.gov.hmrc.alcoholdutyreturns.models.AlcoholRegime.{Beer, Cider, OtherFermentedProduct, Spirits, Wine}
import uk.gov.hmrc.alcoholdutyreturns.models.ApprovalStatus.{Approved, DeRegistered, Insolvent, Revoked, SmallCiderProducer}
import uk.gov.hmrc.alcoholdutyreturns.models.ErrorResponse.{EntityNotFound, InvalidSubscriptionStatus, ObligationFulfilled, UnexpectedResponse}
import uk.gov.hmrc.alcoholdutyreturns.models.ObligationStatus.{Fulfilled, Open}
import uk.gov.hmrc.alcoholdutyreturns.models.{AlcoholRegime, ObligationData, ReturnId, SubscriptionSummary, UserAnswers}

import java.time.LocalDate

class AccountServiceSpec extends SpecBase {

  private val appaId         = appaIdGen.sample.get
  private val periodKey      = periodKeyGen.sample.get
  private val groupId        = "groupId"
  private val internalId     = "internalId"
  private val id             = ReturnId(appaId, periodKey)
  private val obligationData = ObligationData(
    status = Open,
    fromDate = LocalDate.now(),
    toDate = LocalDate.now(),
    dueDate = LocalDate.now()
  )

  private val fulfilledObligationData = ObligationData(
    status = Fulfilled,
    fromDate = LocalDate.now(),
    toDate = LocalDate.now(),
    dueDate = LocalDate.now()
  )

  val emptyUserAnswers: UserAnswers = UserAnswers(
    id,
    groupId,
    internalId
  )

  val alcoholRegimes = Seq(Beer, Cider, Spirits, Wine, OtherFermentedProduct)

  "AccountEntryService" should {
    val accountConnector = mock[AccountConnector]

    "create user answer method return a user answer if the Subscription Status is Approved and the period Obligation is open" in {

      val subscriptionSummary =
        SubscriptionSummary(Approved, alcoholRegimes)
      when(accountConnector.getSubscriptionSummary(any())(any())).thenReturn(EitherT.rightT(subscriptionSummary))
      when(accountConnector.getOpenObligationData(any())(any())).thenReturn(EitherT.rightT(obligationData))

      val service = new AccountServiceImpl(accountConnector)

      val expectedAnswer = emptyUserAnswers.copy(data =
        Json.obj(
          (AlcoholRegime.toString, Json.toJson(alcoholRegimes)),
          (ObligationData.toString, Json.toJson(obligationData))
        )
      )

      whenReady(service.createUserAnswers(emptyUserAnswers).value) { result =>
        result shouldBe Right(expectedAnswer)
      }
    }

    "create user answer method return a user answer if the Subscription Status is Insolvent and the period Obligation is open" in {

      val subscriptionSummary =
        SubscriptionSummary(Insolvent, alcoholRegimes)
      when(accountConnector.getSubscriptionSummary(any())(any())).thenReturn(EitherT.rightT(subscriptionSummary))
      when(accountConnector.getOpenObligationData(any())(any())).thenReturn(EitherT.rightT(obligationData))

      val service = new AccountServiceImpl(accountConnector)

      val expectedAnswer = emptyUserAnswers.copy(data =
        Json.obj(
          (AlcoholRegime.toString, Json.toJson(alcoholRegimes)),
          (ObligationData.toString, Json.toJson(obligationData))
        )
      )

      whenReady(service.createUserAnswers(emptyUserAnswers).value) { result =>
        result shouldBe Right(expectedAnswer)
      }
    }

    Seq(Revoked, DeRegistered, SmallCiderProducer).foreach { subStatus =>
      s"return an error if the Subscription Summary return a status is $subStatus" in {
        val subscriptionSummary =
          SubscriptionSummary(subStatus, Seq.empty)
        when(accountConnector.getSubscriptionSummary(any())(any())).thenReturn(EitherT.rightT(subscriptionSummary))

        val service = new AccountServiceImpl(accountConnector)

        whenReady(service.createUserAnswers(emptyUserAnswers).value) { result =>
          result shouldBe Left(InvalidSubscriptionStatus(subStatus))
        }
      }
    }

    "return an error if the Subscription Summary return a NotFound status" in {
      when(accountConnector.getSubscriptionSummary(any())(any())).thenReturn(EitherT.leftT(EntityNotFound))

      val service = new AccountServiceImpl(accountConnector)

      whenReady(service.createUserAnswers(emptyUserAnswers).value) { result =>
        result shouldBe Left(EntityNotFound)
      }
    }

    "return an error if the Subscription Summary return an error" in {
      when(accountConnector.getSubscriptionSummary(any())(any())).thenReturn(EitherT.leftT(UnexpectedResponse))

      val service = new AccountServiceImpl(accountConnector)

      whenReady(service.createUserAnswers(emptyUserAnswers).value) { result =>
        result shouldBe Left(UnexpectedResponse)
      }
    }

    "return an error if the subscription status is Fulfilled" in {

      val subscriptionSummary =
        SubscriptionSummary(Approved, alcoholRegimes)
      when(accountConnector.getSubscriptionSummary(any())(any())).thenReturn(EitherT.rightT(subscriptionSummary))
      when(accountConnector.getOpenObligationData(any())(any())).thenReturn(EitherT.rightT(fulfilledObligationData))

      val service = new AccountServiceImpl(accountConnector)

      whenReady(service.createUserAnswers(emptyUserAnswers).value) { result =>
        result shouldBe Left(ObligationFulfilled)
      }
    }

    "return an error if the obligation is not found" in {

      val subscriptionSummary =
        SubscriptionSummary(Approved, alcoholRegimes)
      when(accountConnector.getSubscriptionSummary(any())(any())).thenReturn(EitherT.rightT(subscriptionSummary))
      when(accountConnector.getOpenObligationData(any())(any())).thenReturn(EitherT.leftT(EntityNotFound))

      val service = new AccountServiceImpl(accountConnector)

      whenReady(service.createUserAnswers(emptyUserAnswers).value) { result =>
        result shouldBe Left(EntityNotFound)
      }
    }
  }
}
