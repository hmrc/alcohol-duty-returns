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
import uk.gov.hmrc.alcoholdutyreturns.base.SpecBase
import uk.gov.hmrc.alcoholdutyreturns.connector.{CalculatorConnector, ReturnsConnector}
import uk.gov.hmrc.alcoholdutyreturns.models.{ErrorCodes, ReturnId}
import uk.gov.hmrc.alcoholdutyreturns.models.calculation.CalculatedDutyDueByTaxType
import uk.gov.hmrc.alcoholdutyreturns.models.returns.{ReturnCreate, ReturnCreatedDetails, TotalDutyDuebyTaxType}
import uk.gov.hmrc.alcoholdutyreturns.repositories.UserAnswersRepository
import uk.gov.hmrc.play.bootstrap.backend.http.ErrorResponse

import java.time.Instant
import scala.concurrent.Future

class ReturnsServiceSpec extends SpecBase {
  "ReturnsService must" - {
    "calculate dutyDueByTaxType, validate against the schema, submit a return successfully, audit the event, " +
      "clear the user answers and return the created response" in new SetUp {
        when(mockCalculatorConnector.calculateDutyDueByTaxType(any())(any()))
          .thenReturn(EitherT.right[ErrorResponse](Future.successful(calculatedDutyDueByTaxTypeForExampleSubmission)))

        when(mockSchemaValidationService.validateAgainstSchema(returnSubmission)).thenReturn(true)

        when(mockReturnsConnector.submitReturn(returnSubmission, retId.appaId))
          .thenReturn(EitherT.rightT[Future, ErrorResponse](returnCreatedDetails))

        when(mockUserAnswersRespository.clearUserAnswersById(retId)).thenReturn(Future.unit)

        when(mockUserAnswersRespository.get(retId)).thenReturn(Future.successful(Some(userAnswers)))

        whenReady(returnsService.submitReturn(adrReturnSubmission, retId).value) {
          _ mustBe Right[ErrorResponse, ReturnCreatedDetails](returnCreatedDetails)
        }

        verify(mockSchemaValidationService).validateAgainstSchema(returnSubmission)
        verify(mockReturnsConnector).submitReturn(returnSubmission, retId.appaId)
        verify(mockUserAnswersRespository).get(ReturnId(appaId, periodKey))
        verify(mockUserAnswersRespository).clearUserAnswersById(retId)
      }

    "trigger an audit event when unable to get UserAnswers" in new SetUp {
      when(mockCalculatorConnector.calculateDutyDueByTaxType(any())(any()))
        .thenReturn(EitherT.right[ErrorResponse](Future.successful(calculatedDutyDueByTaxTypeForExampleSubmission)))

      when(mockSchemaValidationService.validateAgainstSchema(returnSubmission)).thenReturn(true)

      when(mockReturnsConnector.submitReturn(returnSubmission, retId.appaId))
        .thenReturn(EitherT.rightT[Future, ErrorResponse](returnCreatedDetails))

      when(mockUserAnswersRespository.get(retId)).thenReturn(Future.successful(None))

      when(mockUserAnswersRespository.clearUserAnswersById(retId)).thenReturn(Future.unit)

      whenReady(returnsService.submitReturn(adrReturnSubmission, retId).value) {
        _ mustBe Right[ErrorResponse, ReturnCreatedDetails](returnCreatedDetails)
      }

      verify(mockSchemaValidationService).validateAgainstSchema(returnSubmission)
      verify(mockReturnsConnector).submitReturn(returnSubmission, retId.appaId)
      verify(mockUserAnswersRespository).get(ReturnId(appaId, periodKey))
      verify(mockUserAnswersRespository).clearUserAnswersById(retId)
    }

    "trigger an audit event and clear the user answer even if the get method returns error" in new SetUp {
      when(mockCalculatorConnector.calculateDutyDueByTaxType(any())(any()))
        .thenReturn(EitherT.right[ErrorResponse](Future.successful(calculatedDutyDueByTaxTypeForExampleSubmission)))

      when(mockSchemaValidationService.validateAgainstSchema(returnSubmission)).thenReturn(true)

      when(mockReturnsConnector.submitReturn(returnSubmission, retId.appaId))
        .thenReturn(EitherT.rightT[Future, ErrorResponse](returnCreatedDetails))

      when(mockUserAnswersRespository.get(retId)).thenReturn(Future.failed(new RuntimeException("Fail!")))

      when(mockUserAnswersRespository.clearUserAnswersById(retId)).thenReturn(Future.unit)

      whenReady(returnsService.submitReturn(adrReturnSubmission, retId).value) {
        _ mustBe Right[ErrorResponse, ReturnCreatedDetails](returnCreatedDetails)
      }

      verify(mockSchemaValidationService).validateAgainstSchema(returnSubmission)
      verify(mockReturnsConnector).submitReturn(returnSubmission, retId.appaId)
      verify(mockUserAnswersRespository).get(ReturnId(appaId, periodKey))
      verify(mockUserAnswersRespository).clearUserAnswersById(retId)
    }

    "return any error from the calculator connector if failure" in new SetUp {
      when(mockCalculatorConnector.calculateDutyDueByTaxType(any())(any()))
        .thenReturn(EitherT.leftT[Future, CalculatedDutyDueByTaxType](ErrorCodes.entityNotFound))

      when(mockSchemaValidationService.validateAgainstSchema(returnSubmission)).thenReturn(true)

      when(mockReturnsConnector.submitReturn(returnSubmission, retId.appaId))
        .thenReturn(EitherT.rightT[Future, ErrorResponse](returnCreatedDetails))

      when(mockUserAnswersRespository.clearUserAnswersById(retId)).thenReturn(Future.unit)

      whenReady(returnsService.submitReturn(adrReturnSubmission, retId).value) {
        _ mustBe Left[ErrorResponse, ReturnCreatedDetails](ErrorCodes.entityNotFound)
      }

      verify(mockSchemaValidationService, never).validateAgainstSchema(returnSubmission)
      verify(mockReturnsConnector, never).submitReturn(any(), any())(any())
      verify(mockUserAnswersRespository, never).get(any())
      verify(mockUserAnswersRespository, never).clearUserAnswersById(any())
    }

    "return a failed failure if the calculator connector fails" in new SetUp {
      when(mockCalculatorConnector.calculateDutyDueByTaxType(any())(any()))
        .thenReturn(EitherT.left[CalculatedDutyDueByTaxType](Future.failed(new RuntimeException("Fail!"))))

      when(mockSchemaValidationService.validateAgainstSchema(returnSubmission)).thenReturn(true)

      when(mockReturnsConnector.submitReturn(returnSubmission, retId.appaId))
        .thenReturn(EitherT.rightT[Future, ErrorResponse](returnCreatedDetails))

      when(mockUserAnswersRespository.clearUserAnswersById(retId)).thenReturn(Future.unit)

      whenReady(returnsService.submitReturn(adrReturnSubmission, retId).value.failed) {
        _ mustBe a[RuntimeException]
      }

      verify(mockSchemaValidationService, never).validateAgainstSchema(returnSubmission)
      verify(mockReturnsConnector, never).submitReturn(any(), any())(any())
      verify(mockUserAnswersRespository, never).get(any())
      verify(mockUserAnswersRespository, never).clearUserAnswersById(any())
    }

    "not submit or clear the user answers and return BadRequest if validation fails" in new SetUp {
      when(mockCalculatorConnector.calculateDutyDueByTaxType(any())(any()))
        .thenReturn(EitherT.right[ErrorResponse](Future.successful(calculatedDutyDueByTaxTypeForExampleSubmission)))

      when(mockSchemaValidationService.validateAgainstSchema(returnSubmission)).thenReturn(false)

      when(mockReturnsConnector.submitReturn(returnSubmission, retId.appaId))
        .thenReturn(EitherT.rightT[Future, ErrorResponse](returnCreatedDetails))

      when(mockUserAnswersRespository.clearUserAnswersById(retId)).thenReturn(Future.unit)

      whenReady(returnsService.submitReturn(adrReturnSubmission, retId).value) {
        _ mustBe Left[ErrorResponse, ReturnCreatedDetails](ErrorCodes.badRequest)
      }

      verify(mockSchemaValidationService).validateAgainstSchema(returnSubmission)
      verify(mockReturnsConnector, never).submitReturn(any(), any())(any())
      verify(mockUserAnswersRespository, never).get(any())
      verify(mockUserAnswersRespository, never).clearUserAnswersById(any())
    }

    "return any error from the returns connector if failure" in new SetUp {
      when(mockCalculatorConnector.calculateDutyDueByTaxType(any())(any()))
        .thenReturn(EitherT.right[ErrorResponse](Future.successful(calculatedDutyDueByTaxTypeForExampleSubmission)))

      when(mockSchemaValidationService.validateAgainstSchema(returnSubmission)).thenReturn(true)

      when(mockReturnsConnector.submitReturn(returnSubmission, retId.appaId))
        .thenReturn(EitherT.leftT[Future, ReturnCreatedDetails](ErrorCodes.entityNotFound))

      when(mockUserAnswersRespository.clearUserAnswersById(retId)).thenReturn(Future.unit)

      whenReady(returnsService.submitReturn(adrReturnSubmission, retId).value) {
        _ mustBe Left[ErrorResponse, ReturnCreatedDetails](ErrorCodes.entityNotFound)
      }

      verify(mockSchemaValidationService).validateAgainstSchema(returnSubmission)
      verify(mockReturnsConnector).submitReturn(returnSubmission, retId.appaId)
      verify(mockUserAnswersRespository, never).get(any())
      verify(mockUserAnswersRespository, never).clearUserAnswersById(any())
    }

    "return a failed failure if the returns connector fails" in new SetUp {
      when(mockCalculatorConnector.calculateDutyDueByTaxType(any())(any()))
        .thenReturn(EitherT.right[ErrorResponse](Future.successful(calculatedDutyDueByTaxTypeForExampleSubmission)))

      when(mockSchemaValidationService.validateAgainstSchema(returnSubmission)).thenReturn(true)

      when(mockReturnsConnector.submitReturn(returnSubmission, retId.appaId))
        .thenReturn(EitherT.left[ReturnCreatedDetails](Future.failed(new RuntimeException("Fail!"))))

      when(mockUserAnswersRespository.clearUserAnswersById(retId)).thenReturn(Future.unit)

      whenReady(returnsService.submitReturn(adrReturnSubmission, retId).value.failed) {
        _ mustBe a[RuntimeException]
      }

      verify(mockSchemaValidationService).validateAgainstSchema(returnSubmission)
      verify(mockReturnsConnector).submitReturn(returnSubmission, retId.appaId)
      verify(mockUserAnswersRespository, never).get(any())
      verify(mockUserAnswersRespository, never).clearUserAnswersById(any())
    }

    "return any error if the user answers repository couldn't clear the entry" in new SetUp {
      when(mockCalculatorConnector.calculateDutyDueByTaxType(any())(any()))
        .thenReturn(EitherT.right[ErrorResponse](Future.successful(calculatedDutyDueByTaxTypeForExampleSubmission)))

      when(mockSchemaValidationService.validateAgainstSchema(returnSubmission)).thenReturn(true)

      when(mockReturnsConnector.submitReturn(returnSubmission, retId.appaId))
        .thenReturn(EitherT.rightT[Future, ErrorResponse](returnCreatedDetails))

      when(mockUserAnswersRespository.get(retId)).thenReturn(Future.successful(Some(userAnswers)))

      when(mockCalculatorConnector.calculateDutyDueByTaxType(any())(any()))
        .thenReturn(EitherT.right[ErrorResponse](Future.successful(calculatedDutyDueByTaxTypeForExampleSubmission)))

      when(mockUserAnswersRespository.clearUserAnswersById(retId))
        .thenReturn(Future.failed(new RuntimeException("Fail!")))

      whenReady(returnsService.submitReturn(adrReturnSubmission, retId).value.failed) {
        _ mustBe a[RuntimeException]
      }

      verify(mockSchemaValidationService).validateAgainstSchema(returnSubmission)
      verify(mockReturnsConnector).submitReturn(returnSubmission, retId.appaId)
      verify(mockUserAnswersRespository).get(ReturnId(appaId, periodKey))
      verify(mockUserAnswersRespository).clearUserAnswersById(retId)
    }
  }

  class SetUp {
    val mockReturnsConnector        = mock[ReturnsConnector]
    val mockCalculatorConnector     = mock[CalculatorConnector]
    val mockUserAnswersRespository  = mock[UserAnswersRepository]
    val mockSchemaValidationService = mock[SchemaValidationService]

    val returnsService = new ReturnsService(
      mockReturnsConnector,
      mockCalculatorConnector,
      mockUserAnswersRespository,
      mockSchemaValidationService
    )

    val periodKey = "24AA"
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
  }
}
