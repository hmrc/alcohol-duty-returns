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

import cats.data.{EitherT, OptionT}
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.alcoholdutyreturns.base.SpecBase
import uk.gov.hmrc.alcoholdutyreturns.connector.ReturnsConnector
import uk.gov.hmrc.alcoholdutyreturns.models.returns.{GetReturnDetails, ReturnCreatedDetails}
import uk.gov.hmrc.alcoholdutyreturns.models.{ErrorResponse, ReturnId}
import uk.gov.hmrc.alcoholdutyreturns.repositories.CacheRepository
import uk.gov.hmrc.alcoholdutyreturns.service.{AuditService, ReturnsService}

import java.time.Instant
import scala.concurrent.Future

class ReturnsControllerSpec extends SpecBase {
  "ReturnsController" when {
    "calling getReturn" should {
      "return 200 OK and the return when successful" in new SetUp {
        when(mockReturnsConnector.getReturn(eqTo(returnId.copy(periodKey = periodKey)))(any()))
          .thenReturn(EitherT.rightT[Future, ErrorResponse](returnDetails.success))

        val result: Future[Result] =
          controller.getReturn(appaId, periodKey)(fakeRequest)

        status(result)        shouldBe OK
        contentAsJson(result) shouldBe Json.toJson(adrReturnDetails)
      }

      "return 400 BAD_REQUEST when there is a BAD_REQUEST" in new SetUp {
        when(mockReturnsConnector.getReturn(eqTo(returnId.copy(periodKey = periodKey)))(any()))
          .thenReturn(EitherT.leftT[Future, GetReturnDetails](ErrorResponse.BadRequest))

        val result: Future[Result] =
          controller.getReturn(appaId, periodKey)(fakeRequest)

        status(result) shouldBe BAD_REQUEST
      }

      "return 404 NOT_FOUND when not found" in new SetUp {
        when(mockReturnsConnector.getReturn(eqTo(returnId.copy(periodKey = periodKey)))(any()))
          .thenReturn(EitherT.leftT[Future, GetReturnDetails](ErrorResponse.EntityNotFound))

        val result: Future[Result] =
          controller.getReturn(appaId, periodKey)(fakeRequest)

        status(result) shouldBe NOT_FOUND
      }

      "return 500 INTERNAL_SERVER_ERROR when an unexpected response" in new SetUp {
        when(mockReturnsConnector.getReturn(eqTo(returnId.copy(periodKey = periodKey)))(any()))
          .thenReturn(EitherT.leftT[Future, GetReturnDetails](ErrorResponse.UnexpectedResponse))

        val result: Future[Result] =
          controller.getReturn(appaId, periodKey)(fakeRequest)

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "calling submitReturn" should {
      "return 201 CREATED and the submission created response when successful" in new SetUp {
        when(
          mockReturnsService.submitReturn(eqTo(adrReturnsSubmission), eqTo(returnId.copy(periodKey = periodKey)))(any())
        )
          .thenReturn(EitherT.rightT[Future, ErrorResponse](returnCreatedDetails))

        when(mockCacheRepository.get(any())).thenReturn(OptionT.pure(userAnswers))

        val result: Future[Result] =
          controller.submitReturn(appaId, periodKey)(
            fakeRequestWithJsonBody(Json.toJson(adrReturnsSubmission))
          )

        status(result)        shouldBe CREATED
        contentAsJson(result) shouldBe Json.toJson(adrReturnCreatedDetails)

        verify(mockCacheRepository).get(ReturnId(appaId, periodKey))
      }

      "return 400 BAD_REQUEST when there is a BAD_REQUEST" in new SetUp {
        when(
          mockReturnsService.submitReturn(eqTo(adrReturnsSubmission), eqTo(returnId.copy(periodKey = periodKey)))(any())
        )
          .thenReturn(EitherT.leftT[Future, ReturnCreatedDetails](ErrorResponse.BadRequest))

        val result: Future[Result] =
          controller.submitReturn(appaId, periodKey)(
            fakeRequestWithJsonBody(Json.toJson(adrReturnsSubmission))
          )

        status(result) shouldBe BAD_REQUEST
      }

      "return 404 NOT_FOUND when not found" in new SetUp {
        when(
          mockReturnsService.submitReturn(eqTo(adrReturnsSubmission), eqTo(returnId.copy(periodKey = periodKey)))(any())
        )
          .thenReturn(EitherT.leftT[Future, ReturnCreatedDetails](ErrorResponse.EntityNotFound))

        val result: Future[Result] =
          controller.submitReturn(appaId, periodKey)(
            fakeRequestWithJsonBody(Json.toJson(adrReturnsSubmission))
          )

        status(result) shouldBe NOT_FOUND
      }

      "return 500 INTERNAL_SERVER_ERROR when an unexpected response" in new SetUp {
        when(
          mockReturnsService.submitReturn(eqTo(adrReturnsSubmission), eqTo(returnId.copy(periodKey = periodKey)))(any())
        )
          .thenReturn(EitherT.leftT[Future, ReturnCreatedDetails](ErrorResponse.UnexpectedResponse))

        val result: Future[Result] =
          controller.submitReturn(appaId, periodKey)(
            fakeRequestWithJsonBody(Json.toJson(adrReturnsSubmission))
          )

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }

  class SetUp {
    val mockReturnsService: ReturnsService     = mock[ReturnsService]
    val mockReturnsConnector: ReturnsConnector = mock[ReturnsConnector]
    val mockAuditService: AuditService         = mock[AuditService]
    val mockCacheRepository: CacheRepository   = mock[CacheRepository]

    val controller =
      new ReturnsController(
        fakeAuthorisedAction,
        mockCacheRepository,
        mockReturnsService,
        mockAuditService,
        mockReturnsConnector,
        cc
      )

    val periodKey: String = "24AC"
    val total             = BigDecimal("12345.67")
    val now               = Instant.now()

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
