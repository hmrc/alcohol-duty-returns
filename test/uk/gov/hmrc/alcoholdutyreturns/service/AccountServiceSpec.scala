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
import org.mockito.ArgumentMatchersSugar.eqTo
import uk.gov.hmrc.alcoholdutyreturns.base.SpecBase
import uk.gov.hmrc.alcoholdutyreturns.connector.AccountConnector
import uk.gov.hmrc.alcoholdutyreturns.models.ApprovalStatus.{Approved, DeRegistered, Insolvent, Revoked, SmallCiderProducer}
import uk.gov.hmrc.alcoholdutyreturns.models.ErrorResponse.{EntityNotFound, InvalidJson, InvalidSubscriptionStatus, ObligationFulfilled, UnexpectedResponse}
import uk.gov.hmrc.alcoholdutyreturns.models.ObligationStatus.Fulfilled
import uk.gov.hmrc.alcoholdutyreturns.models.{ApprovalStatus, ErrorResponse}

import java.time.{Clock, Instant, LocalDate, ZoneId}

class AccountServiceSpec extends SpecBase {
  override def clock: Clock = Clock.fixed(Instant.ofEpochMilli(1718037305240L), ZoneId.of("UTC"))

  "getSubscriptionSummaryAndCheckStatus" should {
    Seq[ApprovalStatus](Approved, Insolvent).foreach { status =>
      s"return the subscription summary if the status is ${status.entryName}" in new SetUp {
        val ss = subscriptionSummary.copy(approvalStatus = status)
        when(accountConnector.getSubscriptionSummary(eqTo(returnId.appaId))(any())).thenReturn(EitherT.rightT(ss))

        whenReady(accountService.getSubscriptionSummaryAndCheckStatus(returnId.appaId).value) { result =>
          result shouldBe Right(ss)
        }
      }
    }

    Seq[ApprovalStatus](SmallCiderProducer, DeRegistered, Revoked).foreach { status =>
      s"return InvalidSubscriptionStatus if the status is ${status.entryName}" in new SetUp {
        val ss = subscriptionSummary.copy(approvalStatus = status)
        when(accountConnector.getSubscriptionSummary(eqTo(returnId.appaId))(any())).thenReturn(EitherT.rightT(ss))

        whenReady(accountService.getSubscriptionSummaryAndCheckStatus(returnId.appaId).value) { result =>
          result shouldBe Left(InvalidSubscriptionStatus(status))
        }
      }
    }

    Seq[ErrorResponse](InvalidJson, EntityNotFound, UnexpectedResponse).foreach { error =>
      s"return ${error.entryName} if the connector returns ${error.entryName}" in new SetUp {
        when(accountConnector.getSubscriptionSummary(eqTo(returnId.appaId))(any())).thenReturn(EitherT.leftT(error))

        whenReady(accountService.getSubscriptionSummaryAndCheckStatus(returnId.appaId).value) { result =>
          result shouldBe Left(error)
        }
      }
    }
  }

  "getOpenObligationData" should {
    "return obligation data if Open" in new SetUp {
      when(accountConnector.getOpenObligationData(eqTo(returnId))(any()))
        .thenReturn(EitherT.rightT(obligationData))

      whenReady(accountService.getOpenObligation(returnId).value) { result =>
        result shouldBe Right(obligationData)
      }
    }

    "return an error if Fulfilled" in new SetUp {
      when(accountConnector.getOpenObligationData(eqTo(returnId))(any()))
        .thenReturn(EitherT.rightT(obligationData.copy(status = Fulfilled)))

      whenReady(accountService.getOpenObligation(returnId).value) { result =>
        result shouldBe Left(ObligationFulfilled)
      }
    }

    Seq[ErrorResponse](InvalidJson, EntityNotFound, UnexpectedResponse).foreach { error =>
      s"return ${error.entryName} if the connector returns ${error.entryName}" in new SetUp {
        when(accountConnector.getOpenObligationData(eqTo(returnId))(any())).thenReturn(EitherT.leftT(error))

        whenReady(accountService.getOpenObligation(returnId).value) { result =>
          result shouldBe Left(error)
        }
      }
    }
  }

  class SetUp {
    val accountConnector = mock[AccountConnector]

    val obligationData = getObligationData(LocalDate.now(clock))

    val accountService = new AccountService(accountConnector)
  }
}
