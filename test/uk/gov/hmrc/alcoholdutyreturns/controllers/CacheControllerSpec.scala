/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.alcoholdutyreturns.controllers

import cats.data.EitherT
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.alcoholdutyreturns.base.SpecBase
import uk.gov.hmrc.alcoholdutyreturns.models.ErrorResponse
import uk.gov.hmrc.alcoholdutyreturns.repositories.{CacheRepository, UpdateFailure, UpdateSuccess}
import uk.gov.hmrc.alcoholdutyreturns.service.{AccountService, AuditService}

import java.time.{Clock, Instant, LocalDate, ZoneId}
import scala.concurrent.Future

class CacheControllerSpec extends SpecBase {
  override def clock: Clock = Clock.fixed(Instant.ofEpochMilli(1718037305240L), ZoneId.of("UTC"))

  val mockCacheRepository: CacheRepository = mock[CacheRepository]
  val mockAccountService: AccountService   = mock[AccountService]
  val mockAuditService: AuditService       = mock[AuditService]

  val controller = new CacheController(
    fakeAuthorisedAction,
    mockCacheRepository,
    mockAccountService,
    mockAuditService,
    cc
  )

  "get" should {
    "return 200 OK with an existing user answers when there is one for the id" in {
      when(mockCacheRepository.get(ArgumentMatchers.eq(returnId)))
        .thenReturn(Future.successful(Some(emptyUserAnswers)))

      val result: Future[Result] =
        controller.get(appaId, periodKey)(fakeRequest)

      status(result)        shouldBe OK
      contentAsJson(result) shouldBe Json.toJson(emptyUserAnswers)
    }

    "return 404 NOT_FOUND when there is no user answers for the id" in {
      when(mockCacheRepository.get(ArgumentMatchers.eq(returnId)))
        .thenReturn(Future.successful(None))

      val result: Future[Result] =
        controller.get(appaId, periodKey)(fakeRequest)

      status(result) shouldBe NOT_FOUND
    }
  }

  "set" should {
    "return 200 OK with the user answers that was updated" in {
      when(mockCacheRepository.set(any())).thenReturn(Future.successful(UpdateSuccess))

      val result: Future[Result] =
        controller.set()(
          fakeRequestWithJsonBody(Json.toJson(emptyUserAnswers))
        )

      status(result)        shouldBe OK
      contentAsJson(result) shouldBe Json.toJson(emptyUserAnswers)
    }

    "return 404 Not Found if the repository returns an error" in {
      when(mockCacheRepository.set(any())).thenReturn(Future.successful(UpdateFailure))

      val result: Future[Result] =
        controller.set()(
          fakeRequestWithJsonBody(Json.toJson(emptyUserAnswers))
        )

      status(result) shouldBe NOT_FOUND
    }
  }

  "createUserAnswers" should {
    "return 200 OK with the user answers that was created" when {
      "the account service returns a valid UserAnswers" in {
        when(mockCacheRepository.add(any())).thenReturn(Future.successful(userAnswers))
        when(mockAccountService.getSubscriptionSummaryAndCheckStatus(eqTo(appaId))(any(), any()))
          .thenReturn(EitherT.rightT(subscriptionSummary))
        when(mockAccountService.getOpenObligation(eqTo(returnId))(any(), any()))
          .thenReturn(EitherT.rightT(getObligationData(LocalDate.now(clock))))

        val result: Future[Result] =
          controller.createUserAnswers()(
            fakeRequestWithJsonBody(Json.toJson(returnAndUserDetails))
          )

        status(result)        shouldBe OK
        contentAsJson(result) shouldBe Json.toJson(userAnswers)
      }
    }

    ErrorResponse.values.foreach { errorResponse =>
      s"return the status ${errorResponse.status} if the account service returns the error ${errorResponse.entryName} when getting the subscription summary" in {
        when(mockCacheRepository.add(any())).thenReturn(Future.successful(userAnswers))
        when(mockAccountService.getSubscriptionSummaryAndCheckStatus(eqTo(appaId))(any(), any()))
          .thenReturn(EitherT.leftT(errorResponse))

        val result: Future[Result] =
          controller.createUserAnswers()(
            fakeRequestWithJsonBody(Json.toJson(returnAndUserDetails))
          )

        status(result)        shouldBe errorResponse.status
        contentAsJson(result) shouldBe Json.toJson(errorResponse)
      }
    }

    ErrorResponse.values.foreach { errorResponse =>
      s"return the status ${errorResponse.status} if the account service returns the error ${errorResponse.entryName} when getting the open obligations" in {
        when(mockCacheRepository.add(any())).thenReturn(Future.successful(userAnswers))
        when(mockAccountService.getSubscriptionSummaryAndCheckStatus(eqTo(appaId))(any(), any()))
          .thenReturn(EitherT.rightT(subscriptionSummary))
        when(mockAccountService.getOpenObligation(eqTo(returnId))(any(), any()))
          .thenReturn(EitherT.leftT(errorResponse))

        val result: Future[Result] =
          controller.createUserAnswers()(
            fakeRequestWithJsonBody(Json.toJson(returnAndUserDetails))
          )

        status(result)        shouldBe errorResponse.status
        contentAsJson(result) shouldBe Json.toJson(errorResponse)
      }
    }
  }

}
