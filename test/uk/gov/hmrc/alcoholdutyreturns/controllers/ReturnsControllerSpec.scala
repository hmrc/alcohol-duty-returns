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
import uk.gov.hmrc.alcoholdutyreturns.models.returns.GetReturnDetails
import uk.gov.hmrc.alcoholdutyreturns.models.ErrorResponse

import java.time.Instant
import scala.concurrent.Future

class ReturnsControllerSpec extends SpecBase {
  "ReturnsController" when {
    "calling getReturn" should {
      "return 200 OK and the return" in new SetUp {
        when(mockReturnsConnector.getReturn(eqTo(returnId.copy(periodKey = periodKey)))(any()))
          .thenReturn(EitherT.rightT[Future, ErrorResponse](returnDetails.success))

        val result: Future[Result] =
          controller.getReturn(appaId, periodKey)(fakeRequest)

        status(result)        shouldBe OK
        contentAsJson(result) shouldBe Json.toJson(adrReturnDetails)
      }

      "return 404 NOT_FOUND when there is an issue" in new SetUp {
        when(mockReturnsConnector.getReturn(eqTo(returnId.copy(periodKey = periodKey)))(any()))
          .thenReturn(EitherT.leftT[Future, GetReturnDetails](ErrorResponse.EntityNotFound))

        val result: Future[Result] =
          controller.getReturn(appaId, periodKey)(fakeRequest)

        status(result) shouldBe NOT_FOUND
      }
    }
  }

  class SetUp {
    val mockReturnsConnector: ReturnsConnector = mock[ReturnsConnector]

    val controller = new ReturnsController(fakeAuthorisedAction, mockReturnsConnector, cc)

    val periodKey: String = "24AC"

    val returnDetails = successfulReturnsExample(
      appaId,
      periodKey,
      submissionId,
      chargeReference,
      Instant.now(clock)
    )

    val adrReturnDetails = convertedReturnDetails(periodKey, Instant.now(clock))
  }
}
