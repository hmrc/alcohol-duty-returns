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

package uk.gov.hmrc.alcoholdutyreturns.repositories

import org.mockito.MockitoSugar
import org.mongodb.scala.model.Filters
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json
import uk.gov.hmrc.alcoholdutyreturns.config.AppConfig
import uk.gov.hmrc.alcoholdutyreturns.models.UserAnswers
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant, ZoneId}
import scala.concurrent.ExecutionContext

class CacheRepositorySpec
  extends AnyFreeSpec
    with Matchers
    with DefaultPlayMongoRepositorySupport[UserAnswers]
    with ScalaFutures
    with IntegrationPatience
    with OptionValues
    with MockitoSugar {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  private val instant = Instant.now
  private val stubClock: Clock = Clock.fixed(instant, ZoneId.systemDefault)

  private val userAnswers = UserAnswers("id", Json.obj("foo" -> "bar"), Instant.ofEpochSecond(1))

  private val mockAppConfig = mock[AppConfig]
  when(mockAppConfig.dbTimeToLiveInSeconds) thenReturn 1

  protected override val repository = new CacheRepository(
    mongoComponent = mongoComponent,
    appConfig      = mockAppConfig,
    clock          = stubClock
  )

  ".set" - {

    "must set the last updated time on the supplied user answers to `now`, and save them" in {

      val expectedResult = userAnswers copy (lastUpdated = instant.truncatedTo(ChronoUnit.MILLIS))

      val setResult     = repository.set(userAnswers).futureValue
      val updatedRecord = find(Filters.equal("_id", userAnswers.id)).futureValue.headOption.value

      setResult mustEqual true
      verifyUserAnswerResult(updatedRecord, expectedResult)
    }
  }

  ".get" - {

    "when there is a record for this id" - {
      "must update the lastUpdated time and get the record" in {

        insert(userAnswers).futureValue

        val result         = repository.get(userAnswers.id).futureValue
        val expectedResult = userAnswers copy (lastUpdated = instant)

        verifyUserAnswerResult(result.value, expectedResult)
      }
    }

    "when there is no record for this id" - {

      "must return None" in {

        repository.get("id that does not exist").futureValue must not be defined
      }
    }
  }



  ".keepAlive" - {

    "when there is a record for this id" - {

      "must update its lastUpdated to `now` and return true" in {

        insert(userAnswers).futureValue

        val result = repository.keepAlive(userAnswers.id).futureValue

        val expectedUpdatedAnswers = userAnswers copy (lastUpdated = instant)

        result mustEqual true
        val updatedAnswers = find(Filters.equal("_id", userAnswers.id)).futureValue.headOption.value

        verifyUserAnswerResult(updatedAnswers, expectedUpdatedAnswers)
      }
    }

    "when there is no record for this id" - {

      "must return true" in {

        repository.keepAlive("id that does not exist").futureValue mustEqual true
      }
    }
  }

  def verifyUserAnswerResult(actual: UserAnswers, expected: UserAnswers) = {
    actual.id mustEqual expected.id
    actual.data mustEqual expected.data
    actual.lastUpdated.truncatedTo(ChronoUnit.MILLIS) mustEqual expected.lastUpdated.truncatedTo(ChronoUnit.MILLIS)
  }
}
