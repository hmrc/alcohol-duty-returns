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
  "AdrReturnDetails must" - {
    "serialise to json" in new SetUp {
      Json.toJson(adrReturnDetails).toString mustBe json
    }

    "deserialise from json" in new SetUp {
      Json.parse(json).as[AdrReturnDetails] mustBe adrReturnDetails
    }

    "convert ReturnDetails to AdrReturnDetails" in new SetUp {
      AdrReturnDetails.fromGetReturnDetails(returnDetails.success) mustBe convertedReturnDetails(
        periodKey,
        now
      )
    }

    "convert ReturnDetails to AdrReturnDetails when not a spirits month" in new SetUp {
      AdrReturnDetails.fromGetReturnDetails(
        returnDetails.success.copy(spiritsProduced = None)
      ) mustBe convertedReturnDetails(
        periodKey,
        now
      ).copy(spirits = None)
    }

    "convert ReturnDetails to AdrReturnDetails when no spirits were produced" in new SetUp {
      AdrReturnDetails.fromGetReturnDetails(
        returnDetails.success
          .copy(spiritsProduced = Some(SpiritsProduced(spiritsProdFilled = false, spiritsProduced = None)))
      ) mustBe convertedReturnDetails(
        periodKey,
        now
      ).copy(spirits = None)
    }

    "convert a nil ReturnDetails to AdrReturnDetails" in new SetUp {
      AdrReturnDetails.fromGetReturnDetails(nilReturn.success) mustBe adrNilReturnDetails
    }

    "convert a nil ReturnDetails (with present item sequences but no entries) to AdrReturnDetails" in new SetUp {
      AdrReturnDetails.fromGetReturnDetails(nilReturnWithPresentItemSequences.success) mustBe adrNilReturnDetails
    }

    "throw an IllegalArgumentException if the adjustment key is invalid" in {
      val invalidKey = "invalidKey"
      val exception  = intercept[IllegalArgumentException] {
        AdrReturnAdjustments.isOwedToHmrc(invalidKey)
      }
      exception.getMessage mustBe "Bad adjustment key when checking if amount is owed to HMRC"
    }
  }

  class SetUp {
    val periodKey: String  = "24AA"
    val periodKey2: String = "23AL"
    val periodKey3: String = "23AK"
    val periodKey4: String = "23AJ"
    val periodKey5: String = "23AI"
    val now                = Instant.now(clock)

    val returnDetails = successfulReturnExample(
      appaId,
      periodKey,
      submissionId,
      chargeReference,
      now
    )

    val adrReturnDetails = exampleReturnDetails(periodKey, now)

    val nilReturn                         = nilReturnExample(appaId, periodKey, submissionId, now)
    val nilReturnWithPresentItemSequences =
      nilReturnWithPresentItemSequencesExample(appaId, periodKey, submissionId, now)
    val adrNilReturnDetails               = nilReturnDetails(periodKey, now)

    val json =
      s"""{"identification":{"periodKey":"$periodKey","chargeReference":"$chargeReference","submittedTime":"2024-06-11T15:07:47.838Z"},"alcoholDeclared":{"alcoholDeclaredDetails":[{"taxType":"311","litresOfPureAlcohol":450,"dutyRate":9.27,"dutyValue":4171.5},{"taxType":"321","litresOfPureAlcohol":450,"dutyRate":21.01,"dutyValue":9454.5},{"taxType":"331","litresOfPureAlcohol":450,"dutyRate":28.5,"dutyValue":12825},{"taxType":"341","litresOfPureAlcohol":450,"dutyRate":31.64,"dutyValue":14238},{"taxType":"351","litresOfPureAlcohol":450,"dutyRate":8.42,"dutyValue":3789},{"taxType":"356","litresOfPureAlcohol":450,"dutyRate":19.08,"dutyValue":8586},{"taxType":"361","litresOfPureAlcohol":450,"dutyRate":8.4,"dutyValue":3780},{"taxType":"366","litresOfPureAlcohol":450,"dutyRate":16.47,"dutyValue":7411.5},{"taxType":"371","litresOfPureAlcohol":450,"dutyRate":8.2,"dutyValue":3960},{"taxType":"376","litresOfPureAlcohol":450,"dutyRate":15.63,"dutyValue":7033.5}],"total":75249},"adjustments":{"adjustmentDetails":[{"adjustmentTypeKey":"underdeclaration","returnPeriodAffected":"$periodKey2","taxType":"321","litresOfPureAlcohol":150,"dutyRate":21.01,"dutyValue":3151.5},{"adjustmentTypeKey":"spoilt","taxType":"321","litresOfPureAlcohol":1150,"dutyValue":-24161.5},{"adjustmentTypeKey":"spoilt","returnPeriodAffected":"$periodKey4","taxType":"321","litresOfPureAlcohol":75,"dutyRate":21.01,"dutyValue":-1575.5},{"adjustmentTypeKey":"repackagedDraught","returnPeriodAffected":"$periodKey5","taxType":"321","litresOfPureAlcohol":150,"dutyRate":21.01,"dutyValue":3151.5}],"total":-19434},"totalDutyDue":{"totalDue":55815},"netDutySuspension":{"totalLtsBeer":0.15,"totalLtsWine":0.44,"totalLtsCider":0.38,"totalLtsSpirit":0.02,"totalLtsOtherFermented":0.02,"totalLtsPureAlcoholBeer":0.4248,"totalLtsPureAlcoholWine":0.5965,"totalLtsPureAlcoholCider":0.0379,"totalLtsPureAlcoholSpirit":0.2492,"totalLtsPureAlcoholOtherFermented":0.1894},"spirits":{"spiritsVolumes":{"totalSpirits":0.05,"scotchWhisky":0.26,"irishWhiskey":0.16},"typesOfSpirit":["NeutralAgricultural"],"otherSpiritTypeName":"Coco Pops Vodka"}}"""
  }
}
