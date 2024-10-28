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
import uk.gov.hmrc.alcoholdutyreturns.models.{ApprovalStatus, ErrorCodes}
import uk.gov.hmrc.alcoholdutyreturns.repositories.{CacheRepository, UpdateFailure, UpdateSuccess}
import uk.gov.hmrc.alcoholdutyreturns.service.{AccountService, FakeLockingService, LockingService}

import java.time.LocalDate
import scala.concurrent.Future

class CacheControllerSpec extends SpecBase {
  val mockCacheRepository: CacheRepository = mock[CacheRepository]
  val mockAccountService: AccountService   = mock[AccountService]
  val mockLockingService: LockingService   = new FakeLockingService

  val controller = new CacheController(
    fakeAuthorisedAction,
    fakeCheckAppaIdAction,
    mockCacheRepository,
    mockLockingService,
    mockAccountService,
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

    "return 423 Locked when the user answers are locked by another user" in {
      val mockLockingService = mock[LockingService]
      when(mockLockingService.withLock(any(), any())(any())).thenReturn(Future.successful(None))

      val controller = new CacheController(
        fakeAuthorisedAction,
        fakeCheckAppaIdAction,
        mockCacheRepository,
        mockLockingService,
        mockAccountService,
        cc
      )

      when(mockCacheRepository.get(ArgumentMatchers.eq(returnId)))
        .thenReturn(Future.successful(None))

      val result: Future[Result] =
        controller.get(appaId, periodKey)(fakeRequest)

      status(result) shouldBe LOCKED
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

    "return 423 Locked if the user answers is locked by another user" in {
      val lockingService = mock[LockingService]
      when(lockingService.withLock(any(), any())(any())).thenReturn(Future.successful(None))
      when(mockCacheRepository.set(any())).thenReturn(Future.successful(UpdateSuccess))

      val controller = new CacheController(
        fakeAuthorisedAction,
        fakeCheckAppaIdAction,
        mockCacheRepository,
        lockingService,
        mockAccountService,
        cc
      )

      val result: Future[Result] =
        controller.set()(
          fakeRequestWithJsonBody(Json.toJson(emptyUserAnswers))
        )

      status(result) shouldBe LOCKED
    }
  }

  "createUserAnswers" should {
    "return 201 CREATED with the user answers that was created" when {
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

        status(result)        shouldBe CREATED
        contentAsJson(result) shouldBe Json.toJson(userAnswers)
      }
    }

    "return 423 Not Found if the repository returns an error" in {
      val mockLockingService = mock[LockingService]
      when(mockLockingService.withLock(any(), any())(any())).thenReturn(Future.successful(None))

      val controller = new CacheController(
        fakeAuthorisedAction,
        fakeCheckAppaIdAction,
        mockCacheRepository,
        mockLockingService,
        mockAccountService,
        cc
      )

      when(mockCacheRepository.add(any())).thenReturn(Future.successful(userAnswers))
      when(mockAccountService.getSubscriptionSummaryAndCheckStatus(eqTo(appaId))(any(), any()))
        .thenReturn(EitherT.rightT(subscriptionSummary))
      when(mockAccountService.getOpenObligation(eqTo(returnId))(any(), any()))
        .thenReturn(EitherT.rightT(getObligationData(LocalDate.now(clock))))

      val result: Future[Result] =
        controller.createUserAnswers()(
          fakeRequestWithJsonBody(Json.toJson(returnAndUserDetails))
        )

      status(result) shouldBe LOCKED
    }

    Seq(
      ("EntityNotFound", ErrorCodes.entityNotFound),
      ("InvalidJson", ErrorCodes.invalidJson),
      ("UnexpectedResponse", ErrorCodes.unexpectedResponse),
      ("InvalidSubscriptionStatus(Insolvent)", ErrorCodes.invalidSubscriptionStatus(ApprovalStatus.Insolvent))
    ).foreach { case (errorName, errorResponse) =>
      s"return status ${errorResponse.statusCode} if the account service returns the error $errorName when getting the subscription summary" in {
        when(mockCacheRepository.add(any())).thenReturn(Future.successful(userAnswers))
        when(mockAccountService.getSubscriptionSummaryAndCheckStatus(eqTo(appaId))(any(), any()))
          .thenReturn(EitherT.leftT(errorResponse))

        val result: Future[Result] =
          controller.createUserAnswers()(
            fakeRequestWithJsonBody(Json.toJson(returnAndUserDetails))
          )

        status(result)        shouldBe errorResponse.statusCode
        contentAsJson(result) shouldBe Json.toJson(errorResponse)
      }
    }

    Seq(
      ("EntityNotFound", ErrorCodes.entityNotFound),
      ("InvalidJson", ErrorCodes.invalidJson),
      ("UnexpectedResponse", ErrorCodes.unexpectedResponse),
      ("ObligationFulfilled", ErrorCodes.obligationFulfilled),
      ("InvalidSubscriptionStatus(Insolvent)", ErrorCodes.invalidSubscriptionStatus(ApprovalStatus.Insolvent))
    ).foreach { case (errorName, errorResponse) =>
      s"return the status ${errorResponse.statusCode} if the account service returns the error $errorName when getting the open obligations" in {
        when(mockCacheRepository.add(any())).thenReturn(Future.successful(userAnswers))
        when(mockAccountService.getSubscriptionSummaryAndCheckStatus(eqTo(appaId))(any(), any()))
          .thenReturn(EitherT.rightT(subscriptionSummary))
        when(mockAccountService.getOpenObligation(eqTo(returnId))(any(), any()))
          .thenReturn(EitherT.leftT(errorResponse))

        val result: Future[Result] =
          controller.createUserAnswers()(
            fakeRequestWithJsonBody(Json.toJson(returnAndUserDetails))
          )

        status(result)        shouldBe errorResponse.statusCode
        contentAsJson(result) shouldBe Json.toJson(errorResponse)
      }
    }
  }

  "releaseReturnLock" should {
    "release the lock for a return and return 200 OK" in {
      val mockLockingService = mock[LockingService]
      when(mockLockingService.releaseLock(any(), any())).thenReturn(Future.successful(()))

      val controller = new CacheController(
        fakeAuthorisedAction,
        fakeCheckAppaIdAction,
        mockCacheRepository,
        mockLockingService,
        mockAccountService,
        cc
      )

      val result: Future[Result] =
        controller.releaseReturnLock(appaId, periodKey)(fakeRequest)

      status(result) shouldBe OK
    }
  }

  "keepAlive" should {
    "call the method keepAlive in the locking service and return 200 OK if the lock is refreshed" in {
      val mockLockingService = mock[LockingService]
      when(mockLockingService.keepAlive(any(), any())).thenReturn(Future.successful(true))

      val controller = new CacheController(
        fakeAuthorisedAction,
        fakeCheckAppaIdAction,
        mockCacheRepository,
        mockLockingService,
        mockAccountService,
        cc
      )

      val result: Future[Result] =
        controller.keepAlive(appaId, periodKey)(fakeRequest)

      status(result) shouldBe OK
    }
  }

  "call the method keepAlive in the locking service and return 423 LOCKED if the lock is not refreshed" in {
    val mockLockingService = mock[LockingService]
    when(mockLockingService.keepAlive(any(), any())).thenReturn(Future.successful(false))

    val controller = new CacheController(
      fakeAuthorisedAction,
      fakeCheckAppaIdAction,
      mockCacheRepository,
      mockLockingService,
      mockAccountService,
      cc
    )

    val result: Future[Result] =
      controller.keepAlive(appaId, periodKey)(fakeRequest)

    status(result) shouldBe LOCKED
  }
}
