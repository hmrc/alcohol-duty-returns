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
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import uk.gov.hmrc.alcoholdutyreturns.base.SpecBase
import uk.gov.hmrc.alcoholdutyreturns.connector.{CalculatorConnector, ReturnsConnector}
import uk.gov.hmrc.alcoholdutyreturns.models.audit.AuditReturnSubmitted
import uk.gov.hmrc.alcoholdutyreturns.models.{ErrorResponse, ReturnId}
import uk.gov.hmrc.alcoholdutyreturns.models.calculation.CalculatedDutyDueByTaxType
import uk.gov.hmrc.alcoholdutyreturns.models.returns.{AdrReturnCreatedDetails, ReturnCreate, ReturnCreatedDetails, TotalDutyDuebyTaxType}
import uk.gov.hmrc.alcoholdutyreturns.repositories.CacheRepository

import java.time.Instant
import scala.concurrent.Future

class ReturnsServiceSpec extends SpecBase {
  "ReturnsService" should {
    "calculate dutyDueByTaxType, validate against the schema, submit a return successfully, clear the cache and return the created response" in new SetUp {
      when(mockCalculatorConnector.calculateDutyDueByTaxType(any())(any()))
        .thenReturn(EitherT.right[ErrorResponse](Future.successful(calculatedDutyDueByTaxTypeForExampleSubmission)))

      when(mockSchemaValidationService.validateAgainstSchema(returnSubmission)).thenReturn(true)

      when(mockReturnsConnector.submitReturn(returnSubmission, retId.appaId))
        .thenReturn(EitherT.rightT[Future, ErrorResponse](returnCreatedDetails))

      when(mockCacheRespository.clearUserAnswersById(retId)).thenReturn(Future.unit)

      when(mockCacheRespository.get(retId)).thenReturn(Future.successful(Some(userAnswers)))

      whenReady(returnsService.submitReturn(adrReturnSubmission, retId).value) {
        _ shouldBe Right[ErrorResponse, ReturnCreatedDetails](returnCreatedDetails)
      }

      verify(mockCacheRespository).get(ReturnId(appaId, periodKey))

      verify(mockAuditService).audit(ArgumentMatchers.eq(expectedAuditEvent))(any(), any())

    }

    "return an audit event without user answers" in new SetUp {
      when(mockCalculatorConnector.calculateDutyDueByTaxType(any())(any()))
        .thenReturn(EitherT.right[ErrorResponse](Future.successful(calculatedDutyDueByTaxTypeForExampleSubmission)))

      when(mockSchemaValidationService.validateAgainstSchema(returnSubmission)).thenReturn(true)

      when(mockReturnsConnector.submitReturn(returnSubmission, retId.appaId))
        .thenReturn(EitherT.rightT[Future, ErrorResponse](returnCreatedDetails))

      when(mockCacheRespository.get(retId)).thenReturn(Future.successful(None))

      when(mockCacheRespository.clearUserAnswersById(retId)).thenReturn(Future.unit)

      whenReady(returnsService.submitReturn(adrReturnSubmission, retId).value) {
        _ shouldBe Right[ErrorResponse, ReturnCreatedDetails](returnCreatedDetails)
      }

      verify(mockCacheRespository).get(ReturnId(appaId, periodKey))
      verify(mockAuditService).audit(ArgumentMatchers.eq(expectedPartialAuditEvent))(any(), any())
    }

    "return any error from the calculator connector if failure" in new SetUp {
      when(mockCalculatorConnector.calculateDutyDueByTaxType(any())(any()))
        .thenReturn(EitherT.leftT[Future, CalculatedDutyDueByTaxType](ErrorResponse.EntityNotFound))

      when(mockSchemaValidationService.validateAgainstSchema(returnSubmission)).thenReturn(true)

      when(mockReturnsConnector.submitReturn(returnSubmission, retId.appaId))
        .thenReturn(EitherT.rightT[Future, ErrorResponse](returnCreatedDetails))

      when(mockCacheRespository.clearUserAnswersById(retId)).thenReturn(Future.unit)

      whenReady(returnsService.submitReturn(adrReturnSubmission, retId).value) {
        _ shouldBe Left[ErrorResponse, ReturnCreatedDetails](ErrorResponse.EntityNotFound)
      }
    }

    "return a failed failure if the calculator connector fails" in new SetUp {
      when(mockCalculatorConnector.calculateDutyDueByTaxType(any())(any()))
        .thenReturn(EitherT.left[CalculatedDutyDueByTaxType](Future.failed(new RuntimeException("Fail!"))))

      when(mockSchemaValidationService.validateAgainstSchema(returnSubmission)).thenReturn(true)

      when(mockReturnsConnector.submitReturn(returnSubmission, retId.appaId))
        .thenReturn(EitherT.rightT[Future, ErrorResponse](returnCreatedDetails))

      when(mockCacheRespository.clearUserAnswersById(retId)).thenReturn(Future.unit)

      whenReady(returnsService.submitReturn(adrReturnSubmission, retId).value.failed) {
        _ shouldBe a[RuntimeException]
      }
    }

    "not submit or clear the cache and return BadRequest if validation fails" in new SetUp {
      when(mockCalculatorConnector.calculateDutyDueByTaxType(any())(any()))
        .thenReturn(EitherT.right[ErrorResponse](Future.successful(calculatedDutyDueByTaxTypeForExampleSubmission)))

      when(mockSchemaValidationService.validateAgainstSchema(returnSubmission)).thenReturn(false)

      when(mockReturnsConnector.submitReturn(returnSubmission, retId.appaId))
        .thenReturn(EitherT.rightT[Future, ErrorResponse](returnCreatedDetails))

      when(mockCacheRespository.clearUserAnswersById(retId)).thenReturn(Future.unit)

      whenReady(returnsService.submitReturn(adrReturnSubmission, retId).value) {
        _ shouldBe Left[ErrorResponse, ReturnCreatedDetails](ErrorResponse.BadRequest)
      }

      verify(mockReturnsConnector, never).submitReturn(returnSubmission, retId.appaId)
      verify(mockCacheRespository, never).clearUserAnswersById(retId)
    }

    "return any error from the returns connector if failure" in new SetUp {
      when(mockCalculatorConnector.calculateDutyDueByTaxType(any())(any()))
        .thenReturn(EitherT.right[ErrorResponse](Future.successful(calculatedDutyDueByTaxTypeForExampleSubmission)))

      when(mockSchemaValidationService.validateAgainstSchema(returnSubmission)).thenReturn(true)

      when(mockReturnsConnector.submitReturn(returnSubmission, retId.appaId))
        .thenReturn(EitherT.leftT[Future, ReturnCreatedDetails](ErrorResponse.EntityNotFound))

      when(mockCacheRespository.clearUserAnswersById(retId)).thenReturn(Future.unit)

      whenReady(returnsService.submitReturn(adrReturnSubmission, retId).value) {
        _ shouldBe Left[ErrorResponse, ReturnCreatedDetails](ErrorResponse.EntityNotFound)
      }
    }

    "return a failed failure if the returns connector fails" in new SetUp {
      when(mockCalculatorConnector.calculateDutyDueByTaxType(any())(any()))
        .thenReturn(EitherT.right[ErrorResponse](Future.successful(calculatedDutyDueByTaxTypeForExampleSubmission)))

      when(mockSchemaValidationService.validateAgainstSchema(returnSubmission)).thenReturn(true)

      when(mockReturnsConnector.submitReturn(returnSubmission, retId.appaId))
        .thenReturn(EitherT.left[ReturnCreatedDetails](Future.failed(new RuntimeException("Fail!"))))

      when(mockCacheRespository.clearUserAnswersById(retId)).thenReturn(Future.unit)

      whenReady(returnsService.submitReturn(adrReturnSubmission, retId).value.failed) {
        _ shouldBe a[RuntimeException]
      }
    }

    "return any error if the cache repository couldn't clear user answers" in new SetUp {
      when(mockCalculatorConnector.calculateDutyDueByTaxType(any())(any()))
        .thenReturn(EitherT.right[ErrorResponse](Future.successful(calculatedDutyDueByTaxTypeForExampleSubmission)))

      when(mockSchemaValidationService.validateAgainstSchema(returnSubmission)).thenReturn(true)

      when(mockCalculatorConnector.calculateDutyDueByTaxType(any())(any()))
        .thenReturn(EitherT.right[ErrorResponse](Future.successful(calculatedDutyDueByTaxTypeForExampleSubmission)))

      when(mockCacheRespository.clearUserAnswersById(retId)).thenReturn(Future.failed(new RuntimeException("Fail!")))

      whenReady(returnsService.submitReturn(adrReturnSubmission, retId).value.failed) {
        _ shouldBe a[RuntimeException]
      }
    }
  }

  class SetUp {
    val mockReturnsConnector        = mock[ReturnsConnector]
    val mockCalculatorConnector     = mock[CalculatorConnector]
    val mockCacheRespository        = mock[CacheRepository]
    val mockAuditService            = mock[AuditService]
    val mockSchemaValidationService = mock[SchemaValidationService]

    val returnsService = new ReturnsService(
      mockReturnsConnector,
      mockCalculatorConnector,
      mockCacheRespository,
      mockAuditService,
      mockSchemaValidationService
    )

    val periodKey = "24AC"
    val retId     = returnId.copy(periodKey = periodKey)
    val total     = BigDecimal("12345.67")
    val now       = Instant.now()

    val adrReturnSubmission = exampleReturnSubmissionRequest
    val returnSubmission    = returnCreateSubmission(periodKey)

    val returnCreatedDetails =
      exampleReturnCreatedSuccessfulResponse(periodKey, total, now, chargeReference, submissionId).success

    val returnToSubmit = ReturnCreate
      .fromAdrReturnSubmission(adrReturnSubmission, periodKey)
      .copy(totalDutyDuebyTaxType =
        Some(
          List(
            TotalDutyDuebyTaxType("332", 314.31),
            TotalDutyDuebyTaxType("351", 69.67),
            TotalDutyDuebyTaxType("361", -132.73),
            TotalDutyDuebyTaxType("353", -91.09),
            TotalDutyDuebyTaxType("352", -52.85),
            TotalDutyDuebyTaxType("331", 197.19)
          )
        )
      )

    val expectedAuditEvent = AuditReturnSubmitted(
      appaId = retId.appaId,
      periodKey = periodKey,
      governmentGatewayId = Some(userAnswers.internalId),
      governmentGatewayGroupId = Some(userAnswers.groupId),
      returnSubmittedTime = returnCreatedDetails.processingDate,
      alcoholRegimes = Some(userAnswers.regimes.regimes),
      requestPayload = returnToSubmit,
      responsePayload = AdrReturnCreatedDetails.fromReturnCreatedDetails(returnCreatedDetails)
    )

    val expectedPartialAuditEvent = AuditReturnSubmitted(
      appaId = retId.appaId,
      periodKey = periodKey,
      governmentGatewayId = None,
      governmentGatewayGroupId = None,
      returnSubmittedTime = returnCreatedDetails.processingDate,
      alcoholRegimes = None,
      requestPayload = returnToSubmit,
      responsePayload = AdrReturnCreatedDetails.fromReturnCreatedDetails(returnCreatedDetails)
    )

  }
}
