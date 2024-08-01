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
import uk.gov.hmrc.alcoholdutyreturns.models.ErrorResponse
import uk.gov.hmrc.alcoholdutyreturns.models.calculation.CalculatedDutyDueByTaxType
import uk.gov.hmrc.alcoholdutyreturns.models.returns.ReturnCreatedDetails
import uk.gov.hmrc.alcoholdutyreturns.repositories.CacheRepository

import java.time.Instant
import scala.concurrent.Future

class ReturnsServiceSpec extends SpecBase {
  "ReturnsService" should {
    "calculate dutyDueByTaxType, submit a return successfully, clear the cache and return the created response" in new SetUp {
      when(mockReturnsConnector.submitReturn(returnSubmission, retId.appaId))
        .thenReturn(EitherT.rightT[Future, ErrorResponse](returnCreatedDetails))

      when(mockCalculatorConnector.calculateDutyDueByTaxType(any())(any()))
        .thenReturn(EitherT.right[ErrorResponse](Future.successful(calculatedDutyDueByTaxTypeForExampleSubmission)))

      when(mockCacheRespository.clearUserAnswersById(retId)).thenReturn(Future.unit)

      whenReady(returnsService.submitReturn(adrReturnSubmission, retId).value) {
        _ shouldBe Right[ErrorResponse, ReturnCreatedDetails](returnCreatedDetails)
      }
    }

    "return any error from the calculator connector if failure" in new SetUp {
      when(mockCalculatorConnector.calculateDutyDueByTaxType(any())(any()))
        .thenReturn(EitherT.leftT[Future, CalculatedDutyDueByTaxType](ErrorResponse.EntityNotFound))

      whenReady(returnsService.submitReturn(adrReturnSubmission, retId).value) {
        _ shouldBe Left[ErrorResponse, ReturnCreatedDetails](ErrorResponse.EntityNotFound)
      }
    }

    "return a failed failure if the calculator connector fails" in new SetUp {
      when(mockCalculatorConnector.calculateDutyDueByTaxType(any())(any()))
        .thenReturn(EitherT.left[CalculatedDutyDueByTaxType](Future.failed(new RuntimeException("Fail!"))))

      whenReady(returnsService.submitReturn(adrReturnSubmission, retId).value.failed) {
        _ shouldBe a[RuntimeException]
      }
    }

    "return any error from the returns connector if failure" in new SetUp {
      when(mockCalculatorConnector.calculateDutyDueByTaxType(any())(any()))
        .thenReturn(EitherT.right[ErrorResponse](Future.successful(calculatedDutyDueByTaxTypeForExampleSubmission)))

      when(mockReturnsConnector.submitReturn(returnSubmission, retId.appaId))
        .thenReturn(EitherT.leftT[Future, ReturnCreatedDetails](ErrorResponse.EntityNotFound))

      whenReady(returnsService.submitReturn(adrReturnSubmission, retId).value) {
        _ shouldBe Left[ErrorResponse, ReturnCreatedDetails](ErrorResponse.EntityNotFound)
      }
    }

    "return a failed failure if the returns connector fails" in new SetUp {
      when(mockCalculatorConnector.calculateDutyDueByTaxType(any())(any()))
        .thenReturn(EitherT.right[ErrorResponse](Future.successful(calculatedDutyDueByTaxTypeForExampleSubmission)))

      when(mockReturnsConnector.submitReturn(returnSubmission, retId.appaId))
        .thenReturn(EitherT.left[ReturnCreatedDetails](Future.failed(new RuntimeException("Fail!"))))

      whenReady(returnsService.submitReturn(adrReturnSubmission, retId).value.failed) {
        _ shouldBe a[RuntimeException]
      }
    }

    "return any error if the cache repository couldn't clear user answers" in new SetUp {
      when(mockCalculatorConnector.calculateDutyDueByTaxType(any())(any()))
        .thenReturn(EitherT.right[ErrorResponse](Future.successful(calculatedDutyDueByTaxTypeForExampleSubmission)))

      when(mockCacheRespository.clearUserAnswersById(retId)).thenReturn(Future.failed(new RuntimeException("Fail!")))

      whenReady(returnsService.submitReturn(adrReturnSubmission, retId).value.failed) {
        _ shouldBe a[RuntimeException]
      }
    }
  }

  class SetUp {
    val mockReturnsConnector    = mock[ReturnsConnector]
    val mockCalculatorConnector = mock[CalculatorConnector]
    val mockCacheRespository    = mock[CacheRepository]
    val returnsService          = new ReturnsService(mockReturnsConnector, mockCalculatorConnector, mockCacheRespository)

    val periodKey = "24AC"
    val retId     = returnId.copy(periodKey = periodKey)
    val total     = BigDecimal("12345.67")
    val now       = Instant.now()

    val adrReturnSubmission = exampleReturnSubmissionRequest
    val returnSubmission    = returnCreateSubmission(periodKey)

    val returnCreatedDetails =
      exampleReturnCreatedSuccessfulResponse(periodKey, total, now, chargeReference, submissionId).success
  }
}
