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
import uk.gov.hmrc.alcoholdutyreturns.models.{ErrorCodes, FulfilledObligations, ObligationData}
import uk.gov.hmrc.alcoholdutyreturns.service.AccountService
import play.api.libs.json.Json
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.when
import uk.gov.hmrc.play.bootstrap.http.ErrorResponse

import java.time.LocalDate
import scala.concurrent.Future

class ObligationControllerSpec extends SpecBase {
  "getOpenObligationDetails must" - {
    "return 200 OK with open obligations if successful" in new SetUp {
      when(mockAccountService.getOpenObligations(eqTo(appaId))(any(), any()))
        .thenReturn(EitherT.rightT[Future, ErrorResponse](Seq(obligationData)))

      val result: Future[Result] =
        controller.getOpenObligationDetails(appaId)(fakeRequest)

      status(result)        mustBe OK
      contentAsJson(result) mustBe Json.toJson(Seq(obligationData))
    }

    "return 404 NOT_FOUND when there is an issue" in new SetUp {
      when(mockAccountService.getOpenObligations(eqTo(appaId))(any(), any()))
        .thenReturn(EitherT.leftT[Future, Seq[ObligationData]](ErrorCodes.unexpectedResponse))

      val result: Future[Result] =
        controller.getOpenObligationDetails(appaId)(fakeRequest)

      status(result)          mustBe NOT_FOUND
      contentAsString(result) mustBe "Error: {500,Unexpected Response}"
    }
  }

  "getFulfilledObligationDetails must" - {
    "return 200 OK with fulfilled obligations if successful" in new SetUp {
      when(mockAccountService.getFulfilledObligations(eqTo(appaId))(any(), any()))
        .thenReturn(EitherT.rightT[Future, Seq[FulfilledObligations]](fulfilledObligationData))

      val result: Future[Result] =
        controller.getFulfilledObligationDetails(appaId)(fakeRequest)

      status(result)        mustBe OK
      contentAsJson(result) mustBe Json.toJson(fulfilledObligationData)
    }

    "return 500 INTERNAL_SERVER_ERROR when there is an issue" in new SetUp {
      when(mockAccountService.getFulfilledObligations(eqTo(appaId))(any(), any()))
        .thenReturn(EitherT.leftT[Future, Seq[FulfilledObligations]](ErrorCodes.unexpectedResponse))

      val result: Future[Result] =
        controller.getFulfilledObligationDetails(appaId)(fakeRequest)

      status(result)          mustBe INTERNAL_SERVER_ERROR
      contentAsString(result) mustBe
        "Error: Unable to get fulfilled obligations. Status: 500, Message: Unexpected Response"
    }
  }

  "getOpenObligation must" - {
    "return 200 OK with obligation data if successful" in new SetUp {
      when(mockAccountService.getOpenObligation(eqTo(returnId))(any(), any()))
        .thenReturn(EitherT.rightT[Future, ObligationData](obligationData))

      val result: Future[Result] =
        controller.getOpenObligation(appaId, periodKey)(fakeRequest)

      status(result)        mustBe OK
      contentAsJson(result) mustBe Json.toJson(obligationData)
    }

    Seq(
      ("EntityNotFound", ErrorCodes.entityNotFound),
      ("InvalidJson", ErrorCodes.invalidJson),
      ("UnexpectedResponse", ErrorCodes.unexpectedResponse),
      ("ObligationFulfilled", ErrorCodes.obligationFulfilled)
    ).foreach { case (errorName, errorResponse) =>
      s"return the status ${errorResponse.statusCode} if the account service returns the error $errorName when getting the open obligation" in new SetUp {
        when(mockAccountService.getOpenObligation(eqTo(returnId))(any(), any()))
          .thenReturn(EitherT.leftT[Future, ErrorResponse](errorResponse))

        val result: Future[Result] =
          controller.getOpenObligation(appaId, periodKey)(fakeRequest)

        status(result)          mustBe errorResponse.statusCode
        contentAsString(result) mustBe
          s"Error: Unable to get an open obligation. Status: ${errorResponse.statusCode}, Message: ${errorResponse.message}"
      }
    }
  }

  class SetUp {
    val mockAccountService: AccountService = mock[AccountService]

    val controller     = new ObligationController(fakeAuthorisedAction, fakeCheckAppaIdAction, mockAccountService, cc)
    val obligationData = getObligationData(LocalDate.now(clock))
  }
}
