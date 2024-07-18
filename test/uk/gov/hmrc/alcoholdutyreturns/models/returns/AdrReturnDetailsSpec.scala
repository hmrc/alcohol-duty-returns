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

package uk.gov.hmrc.alcoholdutyreturns.models.returns

import play.api.libs.json.Json
import uk.gov.hmrc.alcoholdutyreturns.base.SpecBase

import java.time.Instant

class AdrReturnDetailsSpec extends SpecBase {
  "AdrReturnDetails" when {
    "should serialise to json" in new SetUp {
      Json.toJson(adrReturnDetails).toString shouldBe json
    }

    "should deserialise from json" in new SetUp {
      Json.parse(json).as[AdrReturnDetails] shouldBe adrReturnDetails
    }

    "should convert ReturnDetails to AdrReturnDetails" in new SetUp {
      AdrReturnDetails.fromGetReturnDetails(returnDetails.success) shouldBe convertedReturnDetails(
        periodKey,
        Instant.now(clock)
      )
    }
  }

  class SetUp {
    val periodKey: String = "24AC"

    val returnDetails = successfulReturnsExample(
      appaId,
      periodKey,
      submissionId,
      chargeReference,
      Instant.now(clock)
    )

    val adrReturnDetails = exampleReturnDetails(periodKey, Instant.now(clock))

    val json =
      s"""{"identification":{"periodKey":"$periodKey","submittedTime":"2024-06-11T15:07:47.838Z"},"alcoholDeclared":{"alcoholDeclaredDetails":[{"taxType":"311","litresOfPureAlcohol":450,"dutyRate":9.27,"dutyValue":4171.5},{"taxType":"321","litresOfPureAlcohol":450,"dutyRate":21.01,"dutyValue":9454.5},{"taxType":"331","litresOfPureAlcohol":450,"dutyRate":28.5,"dutyValue":12825},{"taxType":"341","litresOfPureAlcohol":450,"dutyRate":31.64,"dutyValue":14238},{"taxType":"351","litresOfPureAlcohol":450,"dutyRate":8.42,"dutyValue":3789},{"taxType":"356","litresOfPureAlcohol":450,"dutyRate":19.08,"dutyValue":8586},{"taxType":"361","litresOfPureAlcohol":450,"dutyRate":8.4,"dutyValue":3780},{"taxType":"366","litresOfPureAlcohol":450,"dutyRate":16.47,"dutyValue":7411.5},{"taxType":"371","litresOfPureAlcohol":450,"dutyRate":8.2,"dutyValue":3960},{"taxType":"376","litresOfPureAlcohol":450,"dutyRate":15.63,"dutyValue":7033.5}],"total":75249},"adjustments":{"adjustmentDetails":[{"adjustmentTypeKey":"underdeclaration","taxType":"321","litresOfPureAlcohol":150,"dutyRate":21.01,"dutyValue":3151.5},{"adjustmentTypeKey":"spoilt","taxType":"321","litresOfPureAlcohol":1150,"dutyRate":21.01,"dutyValue":-24161.5},{"adjustmentTypeKey":"spoilt","taxType":"321","litresOfPureAlcohol":75,"dutyRate":21.01,"dutyValue":-1575.5},{"adjustmentTypeKey":"repackagedDraught","taxType":"321","litresOfPureAlcohol":150,"dutyRate":21.01,"dutyValue":3151.5}],"total":-19434},"totalDutyDue":{"totalDue":55815}}"""
  }
}
