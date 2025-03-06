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

package uk.gov.hmrc.alcoholdutyreturns.controllers

import cats.data.EitherT
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.alcoholdutyreturns.base.SpecBase
import uk.gov.hmrc.alcoholdutyreturns.connector.ReturnsConnector
import uk.gov.hmrc.alcoholdutyreturns.models.ErrorCodes
import uk.gov.hmrc.alcoholdutyreturns.models.returns.{GetReturnDetails, ReturnCreatedDetails}
import uk.gov.hmrc.alcoholdutyreturns.service.{FakeLockingService, LockingService, ReturnsService}
import uk.gov.hmrc.play.bootstrap.backend.http.ErrorResponse

import java.time.Instant
import scala.concurrent.Future

class ReturnsControllerSpec extends SpecBase {
  "ReturnsController when" - {
    "calling getReturn must" - {
      "return 200 OK and the return when successful" in new SetUp {
        when(mockReturnsConnector.getReturn(eqTo(returnId.copy(periodKey = periodKey)))(any()))
          .thenReturn(EitherT.rightT[Future, ErrorResponse](returnDetails.success))

        val result: Future[Result] =
          controller.getReturn(appaId, periodKey)(fakeRequest)

        status(result)        mustBe OK
        contentAsJson(result) mustBe Json.toJson(adrReturnDetails)
      }

      "return 400 BAD_REQUEST when there is a BAD_REQUEST" in new SetUp {
        when(mockReturnsConnector.getReturn(eqTo(returnId.copy(periodKey = periodKey)))(any()))
          .thenReturn(EitherT.leftT[Future, GetReturnDetails](ErrorCodes.badRequest))

        val result: Future[Result] =
          controller.getReturn(appaId, periodKey)(fakeRequest)

        status(result) mustBe BAD_REQUEST
      }

      "return 404 NOT_FOUND when not found" in new SetUp {
        when(mockReturnsConnector.getReturn(eqTo(returnId.copy(periodKey = periodKey)))(any()))
          .thenReturn(EitherT.leftT[Future, GetReturnDetails](ErrorCodes.entityNotFound))

        val result: Future[Result] =
          controller.getReturn(appaId, periodKey)(fakeRequest)

        status(result) mustBe NOT_FOUND
      }

      "return 500 INTERNAL_SERVER_ERROR when an unexpected response" in new SetUp {
        when(mockReturnsConnector.getReturn(eqTo(returnId.copy(periodKey = periodKey)))(any()))
          .thenReturn(EitherT.leftT[Future, GetReturnDetails](ErrorCodes.unexpectedResponse))

        val result: Future[Result] =
          controller.getReturn(appaId, periodKey)(fakeRequest)

        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }

    "calling submitReturn must" - {
      "return 201 CREATED and the submission created response when successful" in new SetUp {
        when(
          mockReturnsService.submitReturn(eqTo(adrReturnsSubmission), eqTo(returnId.copy(periodKey = periodKey)))(any())
        )
          .thenReturn(EitherT.rightT[Future, ErrorResponse](returnCreatedDetails))

        val result: Future[Result] =
          controller.submitReturn(appaId, periodKey)(
            fakeRequestWithJsonBody(Json.toJson(adrReturnsSubmission))
          )

        status(result)        mustBe CREATED
        contentAsJson(result) mustBe Json.toJson(adrReturnCreatedDetails)
      }

      "return 400 BAD_REQUEST when there is a BAD_REQUEST" in new SetUp {
        when(
          mockReturnsService.submitReturn(eqTo(adrReturnsSubmission), eqTo(returnId.copy(periodKey = periodKey)))(any())
        )
          .thenReturn(EitherT.leftT[Future, ReturnCreatedDetails](ErrorCodes.badRequest))

        val result: Future[Result] =
          controller.submitReturn(appaId, periodKey)(
            fakeRequestWithJsonBody(Json.toJson(adrReturnsSubmission))
          )

        status(result) mustBe BAD_REQUEST
      }

      "return 423 LOCKED when the return is locked by another user" in new SetUp {

        override val mockLockingService: LockingService = mock[LockingService]
        when(mockLockingService.withLockExecuteAndRelease(any(), any())(any())).thenReturn(Future.successful(None))

        override val controller =
          new ReturnsController(
            fakeAuthorisedAction,
            fakeCheckAppaIdAction,
            mockReturnsService,
            mockLockingService,
            mockReturnsConnector,
            cc
          )

        when(
          mockReturnsService.submitReturn(eqTo(adrReturnsSubmission), eqTo(returnId.copy(periodKey = periodKey)))(any())
        )
          .thenReturn(EitherT.leftT[Future, ReturnCreatedDetails](ErrorCodes.badRequest))

        val result: Future[Result] =
          controller.submitReturn(appaId, periodKey)(
            fakeRequestWithJsonBody(Json.toJson(adrReturnsSubmission))
          )

        status(result) mustBe LOCKED
      }

      "return 404 NOT_FOUND when not found" in new SetUp {
        when(
          mockReturnsService.submitReturn(eqTo(adrReturnsSubmission), eqTo(returnId.copy(periodKey = periodKey)))(any())
        )
          .thenReturn(EitherT.leftT[Future, ReturnCreatedDetails](ErrorCodes.entityNotFound))

        val result: Future[Result] =
          controller.submitReturn(appaId, periodKey)(
            fakeRequestWithJsonBody(Json.toJson(adrReturnsSubmission))
          )

        status(result) mustBe NOT_FOUND
      }

      "return 500 INTERNAL_SERVER_ERROR when an unexpected response" in new SetUp {
        when(
          mockReturnsService.submitReturn(eqTo(adrReturnsSubmission), eqTo(returnId.copy(periodKey = periodKey)))(any())
        )
          .thenReturn(EitherT.leftT[Future, ReturnCreatedDetails](ErrorCodes.unexpectedResponse))

        val result: Future[Result] =
          controller.submitReturn(appaId, periodKey)(
            fakeRequestWithJsonBody(Json.toJson(adrReturnsSubmission))
          )

        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

  class SetUp {
    val mockReturnsService: ReturnsService     = mock[ReturnsService]
    val mockReturnsConnector: ReturnsConnector = mock[ReturnsConnector]
    val mockLockingService: LockingService     = new FakeLockingService

    val controller =
      new ReturnsController(
        fakeAuthorisedAction,
        fakeCheckAppaIdAction,
        mockReturnsService,
        mockLockingService,
        mockReturnsConnector,
        cc
      )

    val periodKey: String = "24AA"
    val total             = BigDecimal("12345.67")
    val now               = Instant.now(clock)

    val returnDetails = successfulReturnExample(
      appaId,
      periodKey,
      submissionId,
      chargeReference,
      Instant.now(clock)
    )

    val adrReturnDetails = convertedReturnDetails(periodKey, Instant.now(clock))

    val adrReturnsSubmission = exampleReturnSubmissionRequest

    val returnCreatedDetails    =
      exampleReturnCreatedSuccessfulResponse(periodKey, total, now, chargeReference, submissionId).success
    val adrReturnCreatedDetails = exampleReturnCreatedDetails(periodKey, total, now, chargeReference)
  }
}
