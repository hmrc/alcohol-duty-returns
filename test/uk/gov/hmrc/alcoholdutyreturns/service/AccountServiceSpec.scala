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
import uk.gov.hmrc.alcoholdutyreturns.models.ObligationStatus.Fulfilled
import uk.gov.hmrc.alcoholdutyreturns.models.{ApprovalStatus, ErrorCodes}

import java.time.LocalDate

class AccountServiceSpec extends SpecBase {
  "getSubscriptionSummaryAndCheckStatus must" - {
    Seq[ApprovalStatus](Approved, Insolvent).foreach { status =>
      s"return the subscription summary if the status is ${status.entryName}" in new SetUp {
        val ss = subscriptionSummary.copy(approvalStatus = status)
        when(accountConnector.getSubscriptionSummary(eqTo(returnId.appaId))(any())).thenReturn(EitherT.rightT(ss))

        whenReady(accountService.getSubscriptionSummaryAndCheckStatus(returnId.appaId).value) { result =>
          result mustBe Right(ss)
        }
      }
    }

    Seq[ApprovalStatus](SmallCiderProducer, DeRegistered, Revoked).foreach { status =>
      s"return InvalidSubscriptionStatus if the status is ${status.entryName}" in new SetUp {
        val ss = subscriptionSummary.copy(approvalStatus = status)
        when(accountConnector.getSubscriptionSummary(eqTo(returnId.appaId))(any())).thenReturn(EitherT.rightT(ss))

        whenReady(accountService.getSubscriptionSummaryAndCheckStatus(returnId.appaId).value) { result =>
          result mustBe Left(ErrorCodes.invalidSubscriptionStatus(status))
        }
      }
    }

    "return an error the connector returns one" in new SetUp {
      when(accountConnector.getSubscriptionSummary(eqTo(returnId.appaId))(any()))
        .thenReturn(EitherT.leftT(ErrorCodes.invalidJson))

      whenReady(accountService.getSubscriptionSummaryAndCheckStatus(returnId.appaId).value) { result =>
        result mustBe Left(ErrorCodes.invalidJson)
      }
    }
  }

  "getOpenObligationData must" - {
    "return obligation data if Open" in new SetUp {
      when(accountConnector.getOpenObligationData(eqTo(returnId))(any()))
        .thenReturn(EitherT.rightT(obligationData))

      whenReady(accountService.getOpenObligation(returnId).value) { result =>
        result mustBe Right(obligationData)
      }
    }

    "return an error if Fulfilled" in new SetUp {
      when(accountConnector.getOpenObligationData(eqTo(returnId))(any()))
        .thenReturn(EitherT.rightT(obligationData.copy(status = Fulfilled)))

      whenReady(accountService.getOpenObligation(returnId).value) { result =>
        result mustBe Left(ErrorCodes.obligationFulfilled)
      }
    }

    "return an error if the connector returns one" in new SetUp {
      when(accountConnector.getOpenObligationData(eqTo(returnId))(any()))
        .thenReturn(EitherT.leftT(ErrorCodes.invalidJson))

      whenReady(accountService.getOpenObligation(returnId).value) { result =>
        result mustBe Left(ErrorCodes.invalidJson)
      }
    }
  }

  "getObligations must" - {
    "return a sequence of obligations when successful" in new SetUp {
      val expectedObligationData = Seq(fulfilledObligationData, obligationData)
      when(accountConnector.getObligationData(any())(any())).thenReturn(EitherT.rightT(expectedObligationData))
      whenReady(accountService.getObligations(appaId).value) { result =>
        result mustBe Right(expectedObligationData)
      }
    }

    "return unexpectedResponse if the connector returns an error" in new SetUp {
      when(accountConnector.getObligationData(any())(any())).thenReturn(EitherT.leftT(ErrorCodes.invalidJson))
      whenReady(accountService.getObligations(appaId).value) { result =>
        result mustBe Left(ErrorCodes.unexpectedResponse)
      }
    }
  }

  class SetUp {
    val accountConnector = mock[AccountConnector]

    val obligationData          = getObligationData(LocalDate.now(clock))
    val fulfilledObligationData = getFulfilledObligationData(LocalDate.now(clock))

    val accountService = new AccountService(accountConnector)
  }
}
