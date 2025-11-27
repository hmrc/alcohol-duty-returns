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
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.when
import uk.gov.hmrc.alcoholdutyreturns.base.SpecBase
import uk.gov.hmrc.alcoholdutyreturns.connector.AccountConnector
import uk.gov.hmrc.alcoholdutyreturns.models.ApprovalStatus.{Approved, DeRegistered, Insolvent, Revoked, SmallCiderProducer}
import uk.gov.hmrc.alcoholdutyreturns.models.ObligationStatus.Fulfilled
import uk.gov.hmrc.alcoholdutyreturns.models.*
import uk.gov.hmrc.play.bootstrap.http.ErrorResponse

import java.time.LocalDate
import scala.concurrent.Future

class AccountServiceSpec extends SpecBase {
  "getSubscriptionSummaryAndCheckStatus must" - {
    Seq[ApprovalStatus](Approved, Insolvent).foreach { status =>
      s"return the subscription summary if the status is ${status.entryName}" in new SetUp {
        val ss = subscriptionSummary.copy(approvalStatus = status)
        when(accountConnector.getSubscriptionSummary(eqTo(returnId.appaId))(any()))
          .thenReturn(EitherT.rightT[Future, SubscriptionSummary](ss))

        whenReady(accountService.getSubscriptionSummaryAndCheckStatus(returnId.appaId).value) { result =>
          result mustBe Right(ss)
        }
      }
    }

    Seq[ApprovalStatus](SmallCiderProducer, DeRegistered, Revoked).foreach { status =>
      s"return InvalidSubscriptionStatus if the status is ${status.entryName}" in new SetUp {
        val ss = subscriptionSummary.copy(approvalStatus = status)
        when(accountConnector.getSubscriptionSummary(eqTo(returnId.appaId))(any()))
          .thenReturn(EitherT.rightT[Future, SubscriptionSummary](ss))

        whenReady(accountService.getSubscriptionSummaryAndCheckStatus(returnId.appaId).value) { result =>
          result mustBe Left(ErrorCodes.invalidSubscriptionStatus(status))
        }
      }
    }

    "return an error if the connector returns one" in new SetUp {
      when(accountConnector.getSubscriptionSummary(eqTo(returnId.appaId))(any()))
        .thenReturn(EitherT.leftT[Future, ErrorResponse](ErrorCodes.invalidJson))

      whenReady(accountService.getSubscriptionSummaryAndCheckStatus(returnId.appaId).value) { result =>
        result mustBe Left(ErrorCodes.invalidJson)
      }
    }
  }

  "getOpenObligation must" - {
    "return obligation data if Open" in new SetUp {
      when(accountConnector.getOpenObligationData(eqTo(returnId))(any()))
        .thenReturn(EitherT.rightT[Future, ObligationData](obligationData))

      whenReady(accountService.getOpenObligation(returnId).value) { result =>
        result mustBe Right(obligationData)
      }
    }

    "return an error if Fulfilled" in new SetUp {
      when(accountConnector.getOpenObligationData(eqTo(returnId))(any()))
        .thenReturn(EitherT.rightT[Future, ObligationData](obligationData.copy(status = Fulfilled)))

      whenReady(accountService.getOpenObligation(returnId).value) { result =>
        result mustBe Left(ErrorCodes.obligationFulfilled)
      }
    }

    "return an error if the connector returns one" in new SetUp {
      when(accountConnector.getOpenObligationData(eqTo(returnId))(any()))
        .thenReturn(EitherT.leftT[Future, ErrorResponse](ErrorCodes.invalidJson))

      whenReady(accountService.getOpenObligation(returnId).value) { result =>
        result mustBe Left(ErrorCodes.invalidJson)
      }
    }
  }

  "getOpenObligations must" - {
    "return a sequence of open obligations when successful" in new SetUp {
      when(accountConnector.getOpenObligations(any())(any()))
        .thenReturn(EitherT.rightT[Future, Seq[ObligationData]](openObligations))

      whenReady(accountService.getOpenObligations(appaId).value) { result =>
        result mustBe Right(openObligations)
      }
    }

    "return unexpectedResponse if the connector returns an error" in new SetUp {
      when(accountConnector.getOpenObligations(any())(any()))
        .thenReturn(EitherT.leftT[Future, ErrorResponse](ErrorCodes.invalidJson))

      whenReady(accountService.getOpenObligations(appaId).value) { result =>
        result mustBe Left(ErrorCodes.unexpectedResponse)
      }
    }
  }

  "getFulfilledObligations must" - {
    "return a sequence of fulfilled obligations by year when successful" in new SetUp {
      when(accountConnector.getFulfilledObligations(any())(any()))
        .thenReturn(EitherT.rightT[Future, Seq[FulfilledObligations]](fulfilledObligationData))

      whenReady(accountService.getFulfilledObligations(appaId).value) { result =>
        result mustBe Right(fulfilledObligationData)
      }
    }

    "return unexpectedResponse if the connector returns an error" in new SetUp {
      when(accountConnector.getFulfilledObligations(any())(any()))
        .thenReturn(EitherT.leftT[Future, ErrorResponse](ErrorCodes.badRequest))

      whenReady(accountService.getFulfilledObligations(appaId).value) { result =>
        result mustBe Left(ErrorCodes.unexpectedResponse)
      }
    }
  }

  class SetUp {
    val accountConnector = mock[AccountConnector]

    val obligationData = getObligationData(LocalDate.now(clock))

    val openObligations = Seq(obligationData, getObligationData(LocalDate.now(clock).minusMonths(1)))

    val accountService = new AccountService(accountConnector)
  }
}
