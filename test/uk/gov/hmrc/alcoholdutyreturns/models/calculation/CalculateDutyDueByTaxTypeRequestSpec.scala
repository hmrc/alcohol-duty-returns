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

package uk.gov.hmrc.alcoholdutyreturns.models.calculation

import play.api.libs.json.Json
import uk.gov.hmrc.alcoholdutyreturns.base.SpecBase

class CalculateDutyDueByTaxTypeRequestSpec extends SpecBase {
  "CalculatedDutyDueByTaxType" should {
    "serialise to json" in new SetUp {
      Json.toJson(calculateDutyDueByTaxTypeRequest).toString shouldBe json
    }

    "populate data from a returns submission when data is present" in {
      CalculateDutyDueByTaxTypeRequest.fromReturnsSubmission(exampleReturnSubmissionRequest) shouldBe Some(
        calculateDutyDueByTaxTypeRequestForExampleSubmission
      )
    }

    "not populate data from a returns submission when a nil return" in {
      CalculateDutyDueByTaxTypeRequest.fromReturnsSubmission(exampleNilSubmissionRequest) shouldBe None
    }
  }

  class SetUp {
    val json =
      """{"declarationOrAdjustmentItems":[{"taxType":"331","dutyDue":115.11},{"taxType":"332","dutyDue":321.88},{"taxType":"332","dutyDue":245.79},{"taxType":"331","dutyDue":8.12}]}"""
  }
}
