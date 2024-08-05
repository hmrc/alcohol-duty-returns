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

import play.api.libs.json.{JsString, Json}
import uk.gov.hmrc.alcoholdutyreturns.base.SpecBase

import java.time.YearMonth

class ReturnPeriodSpec extends SpecBase {
  "ReturnPeriod" should {
    "returning an error" when {
      "the key is more than 4 characters" in {
        ReturnPeriod.fromPeriodKey("24AC1") shouldBe None
      }

      "the key is less than 4 characters" in {
        ReturnPeriod.fromPeriodKey("24A") shouldBe None
      }

      "the key is empty" in {
        ReturnPeriod.fromPeriodKey("") shouldBe None
      }

      "the first character is not a digit" in {
        ReturnPeriod.fromPeriodKey("/4AC") shouldBe None
        ReturnPeriod.fromPeriodKey(":4AC") shouldBe None
        ReturnPeriod.fromPeriodKey("A4AC") shouldBe None
      }

      "the second character is not a digit" in {
        ReturnPeriod.fromPeriodKey("2/AC") shouldBe None
        ReturnPeriod.fromPeriodKey("2:AC") shouldBe None
        ReturnPeriod.fromPeriodKey("2AAC") shouldBe None
      }

      "the third character is not an A" in {
        ReturnPeriod.fromPeriodKey("24BC") shouldBe None
        ReturnPeriod.fromPeriodKey("244C") shouldBe None
        ReturnPeriod.fromPeriodKey("24aC") shouldBe None
      }

      "the fourth character is not a A-L" in {
        ReturnPeriod.fromPeriodKey("24A@") shouldBe None
        ReturnPeriod.fromPeriodKey("24AM") shouldBe None
        ReturnPeriod.fromPeriodKey("24Aa") shouldBe None
        ReturnPeriod.fromPeriodKey("24A9") shouldBe None
      }
    }

    "return a correct ReturnPeriod" when {
      "a valid period key is passed" in {
        ReturnPeriod.fromPeriodKey("24AA") shouldBe Some(ReturnPeriod(YearMonth.of(2024, 1)))
        ReturnPeriod.fromPeriodKey("24AL") shouldBe Some(ReturnPeriod(YearMonth.of(2024, 12)))
        ReturnPeriod.fromPeriodKey("28AC") shouldBe Some(ReturnPeriod(YearMonth.of(2028, 3)))
      }
    }

    "should parse ReturnPeriod with the right json value" in {
      val returnPeriod = ReturnPeriod(YearMonth.of(2024, 1))
      val result       = Json.toJson(returnPeriod)
      result shouldBe JsString("24AA")
    }

    "should transform a valid Period Key json string into a Return Period" in {
      val periodKey         = periodKeyGen.sample.get
      val periodKeyJsString = JsString(periodKey)
      val result            = periodKeyJsString.as[ReturnPeriod]
      result.toPeriodKey shouldBe periodKey
    }

    "should throw an IllegalArgumentException when an invalid period key json string is parsed" in {
      val invalidPeriodKey  = invalidPeriodKeyGen.sample.get
      val periodKeyJsString = JsString(invalidPeriodKey)

      val exception = intercept[IllegalArgumentException](
        periodKeyJsString.as[ReturnPeriod]
      )

      exception shouldBe an[IllegalArgumentException]
    }
  }
}
