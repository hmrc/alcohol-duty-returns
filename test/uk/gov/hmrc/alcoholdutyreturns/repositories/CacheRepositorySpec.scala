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

import uk.gov.hmrc.alcoholdutyreturns.base.SpecBase
import uk.gov.hmrc.mongo.test.MongoSupport

import java.time.{Clock, Instant, LocalDate, ZoneOffset}

class CacheRepositorySpec extends SpecBase with MongoSupport {
  "Cache Repository" should {
    "save the UserAnswer and with the correct values, then modify internalId successful" in new SetUp {
      val updatedUserAnswers = for {
        _      <- repository.add(userAnswers)
        result <- repository.get(returnId)
      } yield result

      whenReady(updatedUserAnswers) { ua =>
        ua                        shouldBe defined
        ua.get.returnId.appaId    shouldBe appaId
        ua.get.returnId.periodKey shouldBe periodKey
        ua.get.groupId            shouldBe groupId
        ua.get.internalId         shouldBe internalId
        ua.get.regimes            shouldBe userAnswers.regimes
        ua.get.data               shouldBe userAnswers.data
        ua.get.lastUpdated        shouldBe Instant.now(clock)
        ua.get.validUntil         shouldBe defined
        ua.get.validUntil.get     shouldBe Instant.now(clock).plusSeconds(appConfig.dbTimeToLiveInSeconds)
      }

      val newInternalId = "newInternalId"

      val updatedUserAnswers2 = for {
        _      <- repository.set(userAnswers.copy(internalId = newInternalId))
        result <- repository.get(returnId)
      } yield result

      whenReady(updatedUserAnswers2) { ua =>
        ua.get.internalId shouldBe newInternalId
      }
    }
  }

  class SetUp {
    val clock = Clock.fixed(LocalDate.of(2024, 2, 1).atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC)

    val repository = new CacheRepository(
      mongoComponent,
      appConfig,
      clock
    )
  }
}
