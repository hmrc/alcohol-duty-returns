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

import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.alcoholdutyreturns.base.SpecBase
import uk.gov.hmrc.alcoholdutyreturns.models.{ReturnId, UserAnswers}
import uk.gov.hmrc.alcoholdutyreturns.repositories.CacheRepository

import scala.concurrent.Future

class CacheControllerSpec extends SpecBase {

  val mockCacheRepository: CacheRepository = mock[CacheRepository]

  private val appaId     = "ADR0001"
  private val periodKey  = "24AA"
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

    "set" should {
      "return 200 OK with the user answers that was inserted" in {
        when(mockCacheRepository.set(any())).thenReturn(Future.successful(true))

        val result: Future[Result] =
          controller.set()(
            fakeRequestWithJsonBody(Json.toJson(emptyUserAnswers))
          )

        status(result)        shouldBe OK
        contentAsJson(result) shouldBe Json.toJson(emptyUserAnswers)
      }
    }
  }

}
