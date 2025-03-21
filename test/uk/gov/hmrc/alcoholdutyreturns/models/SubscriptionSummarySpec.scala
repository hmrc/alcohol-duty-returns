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

class SubscriptionSummarySpec extends SpecBase {
  "SubscriptionSummary must" - {
    val json          =
      """{"approvalStatus":"Approved","regimes":["Spirits","Wine","Cider","OtherFermentedProduct","Beer"]}"""
    val noRegimesJson =
      """{"approvalStatus":"Approved","regimes":[]}"""

    "serialise to json" in {
      Json.toJson(subscriptionSummary).toString() mustBe json
    }

    "deserialise from json" in {
      Json.parse(json).as[SubscriptionSummary] mustBe subscriptionSummary
    }

    "throw an error if no regimes" in {
      an[IllegalArgumentException] mustBe thrownBy(Json.parse(noRegimesJson).as[SubscriptionSummary])
    }
  }
}
