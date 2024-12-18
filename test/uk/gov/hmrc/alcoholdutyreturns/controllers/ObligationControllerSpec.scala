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
import play.api.mvc.Result
import uk.gov.hmrc.alcoholdutyreturns.base.SpecBase
import uk.gov.hmrc.alcoholdutyreturns.models.{ErrorCodes, ObligationData}
import uk.gov.hmrc.alcoholdutyreturns.service.AccountService
import play.api.libs.json.Json
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import uk.gov.hmrc.play.bootstrap.backend.http.ErrorResponse

import java.time.LocalDate
import scala.concurrent.Future

class ObligationControllerSpec extends SpecBase {
  "ObligationController must" - {
    "return 200 OK with obligations if successful" in new SetUp {
      when(mockAccountService.getObligations(eqTo(appaId))(any(), any()))
        .thenReturn(EitherT.rightT[Future, ErrorResponse](Seq(obligationData)))

      val result: Future[Result] =
        controller.getObligationDetails(appaId)(fakeRequest)

      status(result)        mustBe OK
      contentAsJson(result) mustBe Json.toJson(Seq(obligationData))
    }

    "return 404 NOT_FOUND when there is an issue" in new SetUp {
      when(mockAccountService.getObligations(eqTo(appaId))(any(), any()))
        .thenReturn(EitherT.leftT[Future, Seq[ObligationData]](ErrorCodes.unexpectedResponse))

      val result: Future[Result] =
        controller.getObligationDetails(appaId)(fakeRequest)

      status(result)          mustBe NOT_FOUND
      contentAsString(result) mustBe "Error: {500,Unexpected Response}"
    }
  }

  class SetUp {
    val mockAccountService: AccountService = mock[AccountService]

    val controller     = new ObligationController(fakeAuthorisedAction, fakeCheckAppaIdAction, mockAccountService, cc)
    val obligationData = getObligationData(LocalDate.now())
  }
}
