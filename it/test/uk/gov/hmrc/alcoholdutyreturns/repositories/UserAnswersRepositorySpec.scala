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

import org.mongodb.scala.model.Filters
import uk.gov.hmrc.alcoholdutyreturns.base.ISpecBase
import uk.gov.hmrc.alcoholdutyreturns.config.AppConfig
import uk.gov.hmrc.alcoholdutyreturns.models.{ReturnId, UserAnswers}
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant, ZoneId}

class UserAnswersRepositorySpec extends ISpecBase
    with DefaultPlayMongoRepositorySupport[UserAnswers] {
  private val instant          = Instant.now
  private val stubClock: Clock = Clock.fixed(instant, ZoneId.systemDefault)

  private val DB_TTL_IN_SEC = 100

  private val mockAppConfig = mock[AppConfig]
  when(mockAppConfig.dbTimeToLiveInSeconds) thenReturn DB_TTL_IN_SEC

  protected override val repository = new UserAnswersRepository(
    mongoComponent = mongoComponent,
    appConfig = mockAppConfig,
    clock = stubClock
  )

  "add must" - {

    "set the last updated time on the supplied user answers to `now`, and save them" in {

      val expectedAddedUserAnswers = userAnswers.copy(
        lastUpdated = instant,
        validUntil = Some(instant.plusSeconds(DB_TTL_IN_SEC))
      )

      val expectedResult = expectedAddedUserAnswers.copy(
        lastUpdated = expectedAddedUserAnswers.lastUpdated.truncatedTo(ChronoUnit.MILLIS),
        validUntil = expectedAddedUserAnswers.validUntil.map(_.truncatedTo(ChronoUnit.MILLIS))
      )

      val updatedUserAnswers     = repository.add(userAnswers).futureValue
      val updatedRecord = find(Filters.equal("_id", ReturnId(appaId, periodKey))).futureValue.headOption.value

      updatedUserAnswers mustEqual expectedAddedUserAnswers
      verifyUserAnswerResult(updatedRecord, expectedResult)
    }
  }

  "set must" - {
    "set the last updated time on the supplied user answers to `now`, and update them" in {
      val updatedUserAnswers     = repository.add(userAnswers).futureValue

      val updatedResult = userAnswers.copy(
        internalId = "new-internal-id"
      )

      val expectedAddedUserAnswers = userAnswers.copy(
        lastUpdated = instant,
        validUntil = Some(instant.plusSeconds(DB_TTL_IN_SEC))
      )

      val expectedResult = expectedAddedUserAnswers.copy(
        internalId = "new-internal-id",
        lastUpdated = expectedAddedUserAnswers.lastUpdated.truncatedTo(ChronoUnit.MILLIS),
        validUntil = expectedAddedUserAnswers.validUntil.map(_.truncatedTo(ChronoUnit.MILLIS))
      )

      val setResult     = repository.set(updatedResult).futureValue
      val updatedRecord = find(Filters.equal("_id", ReturnId(appaId, periodKey))).futureValue.headOption.value

      updatedUserAnswers mustEqual expectedAddedUserAnswers
      setResult mustEqual UpdateSuccess
      verifyUserAnswerResult(updatedRecord, expectedResult)
    }

    "fail to update a user answer if it wasn't previously saved" in {
      val newUserAnswers = userAnswers.copy(returnId = ReturnId("new-appa-id", "new-period-key"))
      val setResult     = repository.set(newUserAnswers).futureValue
      setResult mustEqual UpdateFailure
    }
  }

  "get when" - {
    "there is a record for this id must" - {
      "update the lastUpdated time, validUntil time, and get the record" in {
        insert(userAnswers).futureValue

        val result         = repository.get(userAnswers.returnId).futureValue
        val expectedResult = userAnswers.copy(
          lastUpdated = instant,
          validUntil = Some(instant.plusSeconds(DB_TTL_IN_SEC))
        )

        verifyUserAnswerResult(result.value, expectedResult)
      }
    }

    "there is no record for this id must" - {
      "return None" in {
        repository.get(ReturnId("APPA id that does not exist", "period key that does not exist")).futureValue must not be defined
      }
    }
  }

  "keepAlive when" - {
    "there is a record for this id must" - {
      "update its lastUpdated to `now` and return true" in {
        insert(userAnswers).futureValue

        val result = repository.keepAlive(userAnswers.returnId).futureValue

        val expectedUpdatedAnswers = userAnswers.copy(
          lastUpdated = instant,
          validUntil = Some(instant.plusSeconds(DB_TTL_IN_SEC))
        )

        result mustEqual true
        val updatedAnswers = find(Filters.equal("_id", ReturnId(appaId, periodKey))
        ).futureValue.headOption.value

        verifyUserAnswerResult(updatedAnswers, expectedUpdatedAnswers)
      }
    }

    "there is no record for this id must" - {
      "return true" in {
        repository.keepAlive(ReturnId("APPA id that does not exist", "period key that does not exist")).futureValue mustEqual true
      }
    }
  }

  "clearUserAnswersById must" - {
    "clear down existing user answers" in {
      insert(userAnswers).futureValue
      repository.get(userAnswers.returnId).futureValue.isEmpty mustBe false
      repository.clearUserAnswersById(userAnswers.returnId).futureValue mustBe ()
      repository.get(userAnswers.returnId).futureValue.isEmpty mustBe true
    }

    "not fail if user answers doesn't exist" in {
      repository.get(userAnswers.returnId).futureValue.isEmpty mustBe true
      repository.clearUserAnswersById(userAnswers.returnId).futureValue mustBe ()
    }
  }

  def verifyUserAnswerResult(actual: UserAnswers, expected: UserAnswers) = {
    actual.returnId mustEqual expected.returnId
    actual.groupId mustEqual expected.groupId
    actual.internalId mustEqual expected.internalId
    actual.data mustEqual expected.data
    actual.lastUpdated.truncatedTo(ChronoUnit.MILLIS) mustEqual expected.lastUpdated.truncatedTo(ChronoUnit.MILLIS)
    actual.validUntil.get.truncatedTo(ChronoUnit.MILLIS) mustEqual expected.validUntil.get.truncatedTo(ChronoUnit.MILLIS)
  }
}
