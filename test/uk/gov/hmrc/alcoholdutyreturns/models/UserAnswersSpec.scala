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

package uk.gov.hmrc.alcoholdutyreturns.models

import play.api.libs.json.Json
import uk.gov.hmrc.alcoholdutyreturns.base.SpecBase

import java.time.{Clock, Instant, LocalDate, ZoneId}

class UserAnswersSpec extends SpecBase {
  override def clock: Clock = Clock.fixed(Instant.ofEpochMilli(1718037305240L), ZoneId.of("UTC"))

  val ua = userAnswers.copy(validUntil = Some(Instant.now(clock).plusMillis(1)))

  "UserAnswers" should {
    val json =
      s"""{"_id":{"appaId":"$appaId","periodKey":"$periodKey"},"groupId":"$groupId","internalId":"$internalId","regimes":["Spirits","Beer","Wine","Cider","OtherFermentedProduct"],"data":{"obligationData":{"status":"Open","fromDate":"2024-06-10","toDate":"2024-06-11","dueDate":"2024-06-12"}},"lastUpdated":{"$$date":{"$$numberLong":"1718037305240"}},"validUntil":{"$$date":{"$$numberLong":"1718037305241"}}}"""

    "serialise to json" in {
      Json.toJson(ua).toString() shouldBe json
    }

    "deserialise from json" in {
      Json.parse(json).as[UserAnswers] shouldBe ua
    }

    "create a UserAnswers from components" in {
      val createdUserAnswers = UserAnswers.createUserAnswers(
        returnAndUserDetails,
        subscriptionSummary,
        getObligationData(LocalDate.now(clock))
      )
      createdUserAnswers.copy(lastUpdated = userAnswers.lastUpdated) shouldBe userAnswers
    }

  }
}
