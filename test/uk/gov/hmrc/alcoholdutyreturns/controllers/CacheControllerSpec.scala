/*
 * Copyright 2023 HM Revenue & Customs
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
import generators.ModelGenerators
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.alcoholdutyreturns.base.SpecBase
import uk.gov.hmrc.alcoholdutyreturns.models.{ErrorResponse, ReturnId, UserAnswers}
import uk.gov.hmrc.alcoholdutyreturns.repositories.{CacheRepository, UpdateFailure, UpdateSuccess}
import uk.gov.hmrc.alcoholdutyreturns.service.AccountService

import scala.concurrent.Future

class CacheControllerSpec extends SpecBase {

  val mockCacheRepository: CacheRepository = mock[CacheRepository]
  val mockAccountService: AccountService   = mock[AccountService]

  private val appaId     = appaIdGen.sample.get
  private val periodKey  = periodKeyGen.sample.get
  private val groupId    = "groupId"
  private val internalId = "internalId"
  private val id         = ReturnId(appaId, periodKey)

  val emptyUserAnswers: UserAnswers = UserAnswers(
    id,
    groupId,
    internalId
  )

  val controller = new CacheController(
    fakeAuthorisedAction,
    mockCacheRepository,
    mockAccountService,
    cc
  )

  "get" should {
    "return 200 OK with an existing user answers when there is one for the id" in {
      when(mockCacheRepository.get(ArgumentMatchers.eq(id)))
        .thenReturn(Future.successful(Some(emptyUserAnswers)))

      val result: Future[Result] =
        controller.get(appaId, periodKey)(fakeRequest)

      status(result)        shouldBe OK
      contentAsJson(result) shouldBe Json.toJson(emptyUserAnswers)
    }

    "return 404 NOT_FOUND when there is no user answers for the id" in {
      when(mockCacheRepository.get(ArgumentMatchers.eq(id)))
        .thenReturn(Future.successful(None))

      val result: Future[Result] =
        controller.get(appaId, periodKey)(fakeRequest)

      status(result) shouldBe NOT_FOUND
    }
  }

  "set" should {
    "return 200 OK with the user answers that was updated" in {
      when(mockCacheRepository.set(any())).thenReturn(Future.successful(UpdateSuccess))

      val result: Future[Result] =
        controller.set()(
          fakeRequestWithJsonBody(Json.toJson(emptyUserAnswers))
        )

      status(result)        shouldBe OK
      contentAsJson(result) shouldBe Json.toJson(emptyUserAnswers)
    }

    "return 404 Not Found if the repository returns an error" in {
      when(mockCacheRepository.set(any())).thenReturn(Future.successful(UpdateFailure))

      val result: Future[Result] =
        controller.set()(
          fakeRequestWithJsonBody(Json.toJson(emptyUserAnswers))
        )

      status(result) shouldBe NOT_FOUND
    }
  }

  "add" should {
    "return 200 OK with the user answers that was inserted and the account service return a UserAnswers" in {
      when(mockCacheRepository.add(any())).thenReturn(Future.successful(true))
      when(mockAccountService.createUserAnswers(any())(any(), any())).thenReturn(EitherT.rightT(emptyUserAnswers))

      val result: Future[Result] =
        controller.add()(
          fakeRequestWithJsonBody(Json.toJson(emptyUserAnswers))
        )

      status(result)        shouldBe OK
      contentAsJson(result) shouldBe Json.toJson(emptyUserAnswers)
    }

    ErrorResponse.values.foreach { errorResponse =>
      s"return the status ${errorResponse.status} if the account service return the error ${errorResponse.entryName}" in {
        when(mockCacheRepository.add(any())).thenReturn(Future.successful(true))
        when(mockAccountService.createUserAnswers(any())(any(), any())).thenReturn(EitherT.leftT(errorResponse))

        val result: Future[Result] =
          controller.add()(
            fakeRequestWithJsonBody(Json.toJson(emptyUserAnswers))
          )

        status(result)        shouldBe errorResponse.status
        contentAsJson(result) shouldBe Json.toJson(errorResponse)
      }
    }

  }

}
