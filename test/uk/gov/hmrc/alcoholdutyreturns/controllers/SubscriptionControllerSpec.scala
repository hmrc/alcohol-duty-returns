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
import uk.gov.hmrc.alcoholdutyreturns.models.{ApprovalStatus, ErrorCodes}
import uk.gov.hmrc.alcoholdutyreturns.service.AccountService

import scala.concurrent.Future

class SubscriptionControllerSpec extends SpecBase {
  "getValidSubscriptionRegimes must" - {
    "return 200 OK with subscription regimes if successful" in new SetUp {
      when(mockAccountService.getSubscriptionSummaryAndCheckStatus(eqTo(appaId))(any(), any()))
        .thenReturn(EitherT.rightT(subscriptionSummary))

      val result: Future[Result] =
        controller.getValidSubscriptionRegimes(appaId)(fakeRequest)

      status(result)        mustBe OK
      contentAsJson(result) mustBe Json.toJson(subscriptionSummary.regimes)
    }

    Seq(
      ("EntityNotFound", ErrorCodes.entityNotFound),
      ("InvalidJson", ErrorCodes.invalidJson),
      ("UnexpectedResponse", ErrorCodes.unexpectedResponse),
      ("InvalidSubscriptionStatus(Revoked)", ErrorCodes.invalidSubscriptionStatus(ApprovalStatus.Revoked))
    ).foreach { case (errorName, errorResponse) =>
      s"return status ${errorResponse.statusCode} if the account service returns the error $errorName when getting the subscription summary" in new SetUp {
        when(mockAccountService.getSubscriptionSummaryAndCheckStatus(eqTo(appaId))(any(), any()))
          .thenReturn(EitherT.leftT(errorResponse))

        val result: Future[Result] =
          controller.getValidSubscriptionRegimes(appaId)(fakeRequest)

        status(result) mustBe errorResponse.statusCode
        contentAsString(
          result
        )              mustBe s"Error: Unable to get a valid subscription. Status: ${errorResponse.statusCode}, Message: ${errorResponse.message}"
      }
    }
  }

  class SetUp {
    val mockAccountService: AccountService = mock[AccountService]

    val controller = new SubscriptionController(fakeAuthorisedAction, fakeCheckAppaIdAction, mockAccountService, cc)
  }
}
