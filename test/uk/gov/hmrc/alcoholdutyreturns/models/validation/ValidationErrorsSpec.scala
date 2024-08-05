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

package uk.gov.hmrc.alcoholdutyreturns.models.validation

import uk.gov.hmrc.alcoholdutyreturns.base.SpecBase
import uk.gov.hmrc.alcoholdutyreturns.models.ReturnPeriod

class ValidationErrorsSpec extends SpecBase {
  val details   = "details"
  val maxLength = 5

  "ValidationErrors" when {
    "PositiveNumberExpected" should {
      "have the correct error message" in {
        PositiveNumberExpected(details).errorMessage shouldBe s"Positive non-zero number expected: $details"
      }
    }

    "NegativeNumberExpected" should {
      "have the correct error message" in {
        NegativeNumberExpected(details).errorMessage shouldBe s"Negative non-zero number expected: $details"
      }
    }

    "NonNegativeNumberExpected" should {
      "have the correct error message" in {
        NonNegativeNumberExpected(details).errorMessage shouldBe s"Non-negative number expected: $details"
      }
    }

    "NonPositiveNumberExpected" should {
      "have the correct error message" in {
        NonPositiveNumberExpected(details).errorMessage shouldBe s"Non-positive number expected: $details"
      }
    }

    "ZeroExpected" should {
      "have the correct error message" in {
        ZeroExpected(details).errorMessage shouldBe s"Zero expected: $details"
      }
    }

    "GreaterThanOrEqualToExpected" should {
      "have the correct error message" in {
        GreaterThanOrEqualToExpected(
          details,
          BigDecimal(1),
          BigDecimal(2)
        ).errorMessage shouldBe s"1 is not greater than or equal to 2: $details"
      }
    }

    "StringWasEmpty" should {
      "have the correct error message" in {
        StringWasEmpty(details).errorMessage shouldBe s"String was empty: $details"
      }
    }

    "StringTooLong" should {
      "have the correct error message" in {
        StringTooLong(details, maxLength).errorMessage shouldBe s"String was too long (max $maxLength): $details"
      }
    }

    "InvalidTaxCode" should {
      "have the correct error message" in {
        InvalidTaxCode(details).errorMessage shouldBe s"Invalid tax code: $details"
      }
    }

    "NoItemsExpected" should {
      "have the correct error message" in {
        NoItemsExpected(details).errorMessage shouldBe s"No items expected: $details"
      }
    }

    "ItemsExpected" should {
      "have the correct error message" in {
        ItemsExpected(details).errorMessage shouldBe s"Items expected: $details"
      }
    }

    "FieldNotExpected" should {
      "have the correct error message" in {
        FieldNotExpected(details).errorMessage shouldBe s"Field was not expected: $details"
      }
    }

    "FieldExpected" should {
      "have the correct error message" in {
        FieldExpected(details).errorMessage shouldBe s"Field was expected: $details"
      }
    }

    "NotValidSpiritsQuarter" should {
      "have the correct error message" in {
        NotValidSpiritsQuarter(
          details,
          ReturnPeriod.fromPeriodKey(periodKeyJan).get
        ).errorMessage shouldBe s"Not a valid spirits quarter $periodKeyJan: $details"
      }
    }
  }
}
