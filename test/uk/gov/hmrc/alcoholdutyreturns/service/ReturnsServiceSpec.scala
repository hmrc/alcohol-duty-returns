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
import uk.gov.hmrc.alcoholdutyreturns.base.SpecBase
import uk.gov.hmrc.alcoholdutyreturns.connector.ReturnsConnector
import uk.gov.hmrc.alcoholdutyreturns.models.ErrorResponse
import uk.gov.hmrc.alcoholdutyreturns.models.returns.ReturnCreatedDetails

import java.time.Instant
import scala.concurrent.Future

class ReturnsServiceSpec extends SpecBase {
  "ReturnsService" should {
    "submit a return successfully return the created response" in new SetUp {
      when(mockReturnsConnector.submitReturn(returnSubmission, retId.appaId))
        .thenReturn(EitherT.rightT[Future, ErrorResponse](returnCreatedDetails))

      whenReady(returnsService.submitReturn(adrReturnSubmission, retId).value) {
        _ shouldBe Right[ErrorResponse, ReturnCreatedDetails](returnCreatedDetails)
      }
    }

    "return any error from the connector if failure" in new SetUp {
      when(mockReturnsConnector.submitReturn(returnSubmission, retId.appaId))
        .thenReturn(EitherT.leftT[Future, ReturnCreatedDetails](ErrorResponse.EntityNotFound))

      whenReady(returnsService.submitReturn(adrReturnSubmission, retId).value) {
        _ shouldBe Left[ErrorResponse, ReturnCreatedDetails](ErrorResponse.EntityNotFound)
      }
    }
  }

  class SetUp {
    val mockReturnsConnector = mock[ReturnsConnector]
    val returnsService       = new ReturnsService(mockReturnsConnector)

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