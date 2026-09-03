/*
 * Copyright 2026 HM Revenue & Customs
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

import org.mockito.Mockito.when
import uk.gov.hmrc.alcoholdutyreturns.base.ISpecBase
import uk.gov.hmrc.alcoholdutyreturns.config.AppConfig
import uk.gov.hmrc.alcoholdutyreturns.models.ContactPreferenceAsked
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

class ContactPreferenceAskedRepositorySpec
    extends ISpecBase
    with DefaultPlayMongoRepositorySupport[ContactPreferenceAsked] {

  private val TTL_IN_SEC: Long = 100

  private val mockAppConfig = mock[AppConfig]
  when(mockAppConfig.contactPreferenceAskedTtlInSeconds) thenReturn TTL_IN_SEC

  protected override val repository: ContactPreferenceAskedRepository = new ContactPreferenceAskedRepository(
    mongoComponent = mongoComponent,
    appConfig = mockAppConfig,
    clock = clock
  )

  "hasBeenAskedRecently must" - {
    "return false if the appaId has not been asked" in {
      repository.hasBeenAskedRecently(appaId).futureValue mustBe false
    }

    "return true if the appaId has been asked" in {
      repository.markAsked(appaId).futureValue
      repository.hasBeenAskedRecently(appaId).futureValue mustBe true
    }

    "not be affected by a different appaId being asked" in {
      repository.markAsked("otherAppaId").futureValue
      repository.hasBeenAskedRecently(appaId).futureValue mustBe false
    }
  }

  "markAsked must" - {
    "not fail (upsert) if called twice for the same appaId" in {
      repository.markAsked(appaId).futureValue
      repository.markAsked(appaId).futureValue

      repository.hasBeenAskedRecently(appaId).futureValue mustBe true
    }
  }
}
