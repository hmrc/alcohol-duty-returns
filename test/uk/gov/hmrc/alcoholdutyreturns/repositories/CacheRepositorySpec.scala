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

package uk.gov.hmrc.alcoholdutyreturns.repositories

import play.api.libs.json.Json
import uk.gov.hmrc.alcoholdutyreturns.base.SpecBase
import uk.gov.hmrc.alcoholdutyreturns.models.{ReturnId, UserAnswers}
import uk.gov.hmrc.mongo.test.MongoSupport

import java.time.{Clock, Instant, LocalDate, ZoneOffset}

class CacheRepositorySpec extends SpecBase with MongoSupport {

  private val clock = Clock.fixed(LocalDate.of(2024, 2, 1).atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC)

  val repository = new CacheRepository(
    mongoComponent,
    appConfig,
    clock
  )

  "Cache Repository" should {
    "save the UserAnswer and with the correct values" in {
      val appaId     = "ADR0001"
      val periodKey  = "24AA"
      val id         = ReturnId(appaId, periodKey)
      val groupId    = "groupId"
      val internalId = "internalId"
      val data       = Json.obj("foo" -> "bar")

      val userAnswers = UserAnswers(id, groupId, internalId, data = data)

      val updatedUserAnswers = for {
        _      <- repository.set(userAnswers)
        result <- repository.get(id)
      } yield result

      whenReady(updatedUserAnswers) { ua =>
        ua                    shouldBe defined
        ua.get.id.appaId      shouldBe appaId
        ua.get.id.periodKey   shouldBe periodKey
        ua.get.groupId        shouldBe groupId
        ua.get.internalId     shouldBe internalId
        ua.get.data           shouldBe data
        ua.get.lastUpdated    shouldBe Instant.now(clock)
        ua.get.validUntil     shouldBe defined
        ua.get.validUntil.get shouldBe Instant.now(clock).plusSeconds(appConfig.dbTimeToLiveInSeconds)
      }
    }

    "save the UserAnswer and overriding lastUpdated and validUntil fields" in {
      val appaId      = "ADR0001"
      val periodKey   = "24AA"
      val id          = ReturnId(appaId, periodKey)
      val groupId     = "groupId"
      val internalId  = "internalId"
      val data        = Json.obj("foo" -> "bar")
      val userAnswers =
        UserAnswers(
          ReturnId(appaId, periodKey),
          groupId = groupId,
          internalId = internalId,
          data = data,
          lastUpdated = Instant.now(clock).plusSeconds(appConfig.dbTimeToLiveInSeconds * 2),
          validUntil = Some(Instant.now(clock).plusSeconds(appConfig.dbTimeToLiveInSeconds * 3))
        )

      val updatedUserAnswers = for {
        _      <- repository.set(userAnswers)
        result <- repository.get(id)
      } yield result

      whenReady(updatedUserAnswers) { ua =>
        ua                    shouldBe defined
        ua.get.id.appaId      shouldBe appaId
        ua.get.id.periodKey   shouldBe periodKey
        ua.get.groupId        shouldBe groupId
        ua.get.internalId     shouldBe internalId
        ua.get.data           shouldBe data
        ua.get.lastUpdated    shouldBe Instant.now(clock)
        ua.get.validUntil     shouldBe defined
        ua.get.validUntil.get shouldBe Instant.now(clock).plusSeconds(appConfig.dbTimeToLiveInSeconds)
      }
    }
  }
}
