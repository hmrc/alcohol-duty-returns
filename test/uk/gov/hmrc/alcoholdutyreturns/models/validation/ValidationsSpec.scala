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

import cats.data.Chain
import cats.data.Validated.Invalid
import uk.gov.hmrc.alcoholdutyreturns.base.SpecBase
import uk.gov.hmrc.alcoholdutyreturns.models.ReturnPeriod

class ValidationsSpec extends SpecBase {
  val positiveNumber: BigDecimal = BigDecimal("0.01")
  val negativeNumber: BigDecimal = BigDecimal("-0.01")
  val zero: BigDecimal           = BigDecimal(0)
  val emptyString                = ""
  val nonEmptyString             = "a"
  val field                      = "field"
  val field2                     = "field2"
  val taxCode                    = "331"
  val nonEmptyCollection         = Seq("a")

  "validatePositive" should {
    "succeed if positive" in {
      Validations.validatePositive(positiveNumber, field).isValid shouldBe true
    }

    "fail if negative" in {
      Validations.validatePositive(negativeNumber, field).isValid shouldBe false
    }

    "fail if zero" in {
      Validations.validatePositive(zero, field).isValid shouldBe false
    }

    "fail with right validation failure" in {
      Validations.validatePositive(zero, field) match {
        case Invalid(Chain(PositiveNumberExpected(s"$field was $zero"))) => ()
        case _                                                           => fail()
      }
    }
  }

  "validateNegative" should {
    "succeed if negative" in {
      Validations.validateNegative(negativeNumber, field).isValid shouldBe true
    }

    "fail if positive" in {
      Validations.validateNegative(positiveNumber, field).isValid shouldBe false
    }

    "fail if zero" in {
      Validations.validateNegative(zero, field).isValid shouldBe false
    }

    "fail with right validation failure" in {
      Validations.validateNegative(zero, field) match {
        case Invalid(Chain(NegativeNumberExpected(s"$field was $zero"))) => ()
        case _                                                           => fail()
      }
    }
  }

  "validateNonPositive" should {
    "succeed if negative" in {
      Validations.validateNonPositive(negativeNumber, field).isValid shouldBe true
    }

    "fail if positive" in {
      Validations.validateNonPositive(positiveNumber, field).isValid shouldBe false
    }

    "succeed if zero" in {
      Validations.validateNonPositive(zero, field).isValid shouldBe true
    }

    "fail with right validation failure" in {
      Validations.validateNonPositive(positiveNumber, field) match {
        case Invalid(Chain(NonPositiveNumberExpected(s"$field was $positiveNumber"))) => ()
        case _                                                                        => fail()
      }
    }
  }

  "validateNonNegative" should {
    "succeed if positive" in {
      Validations.validateNonNegative(positiveNumber, field).isValid shouldBe true
    }

    "fail if negative" in {
      Validations.validateNonNegative(negativeNumber, field).isValid shouldBe false
    }

    "succeed if zero" in {
      Validations.validateNonNegative(zero, field).isValid shouldBe true
    }

    "fail with right validation failure" in {
      Validations.validateNonNegative(negativeNumber, field) match {
        case Invalid(Chain(NonNegativeNumberExpected(s"$field was $negativeNumber"))) => ()
        case _                                                                        => fail()
      }
    }
  }

  "validateZero" should {
    "fail if positive" in {
      Validations.validateZero(positiveNumber, field).isValid shouldBe false
    }

    "fail if negative" in {
      Validations.validateZero(negativeNumber, field).isValid shouldBe false
    }

    "succeed if zero" in {
      Validations.validateZero(zero, field).isValid shouldBe true
    }

    "fail with right validation failure" in {
      Validations.validateZero(negativeNumber, field) match {
        case Invalid(Chain(ZeroExpected(s"$field was $zero"))) => ()
        case _                                                 => fail()
      }
    }
  }

  "validateGreaterThanOrEqualTo" should {
    "succeed if v1 > v2" in {
      Validations.validateGreaterThanOrEqualTo(BigDecimal(2), BigDecimal(1), field, field2).isValid shouldBe true
    }

    "succeed if v1 = v2" in {
      Validations.validateGreaterThanOrEqualTo(BigDecimal(2), BigDecimal(2), field, field2).isValid shouldBe true
    }

    "fail if v1 < v2" in {
      Validations.validateGreaterThanOrEqualTo(BigDecimal(1), BigDecimal(2), field, field2).isValid shouldBe false
    }

    "fail with right validation failure" in {
      Validations.validateGreaterThanOrEqualTo(BigDecimal(1), BigDecimal(2), field, field2) match {
        case Invalid(Chain(GreaterThanOrEqualToExpected(s"$field was 1; $field2 was 2", _, _))) => ()
        case _                                                                                  => fail()
      }
    }
  }

  "validateStringNonEmpty" should {
    "fail if empty" in {
      Validations.validateStringNonEmpty(emptyString, field).isValid shouldBe false
    }

    "succeed if not empty" in {
      Validations.validateStringNonEmpty(nonEmptyString, field).isValid shouldBe true
    }

    "fail with right validation failure" in {
      Validations.validateStringNonEmpty(emptyString, field) match {
        case Invalid(Chain(StringWasEmpty(s"$field was empty"))) => ()
        case _                                                   => fail()
      }
    }
  }

  "validateStringLength" should {
    "succeed if <= maxLength" in {
      Validations.validateStringLength(nonEmptyString, field, 1).isValid shouldBe true
    }

    "fail if longer than maxLength" in {
      Validations.validateStringLength(nonEmptyString, field, 0).isValid shouldBe false
    }

    "fail with right validation failure" in {
      Validations.validateStringLength(nonEmptyString, field, 0) match {
        case Invalid(Chain(StringTooLong(s"$field was $nonEmptyString", 0))) => ()
        case _                                                               => fail()
      }
    }
  }

  "validateTaxCode" should {
    "succeed if 3 digits" in {
      Validations.validateTaxCode(taxCode, field).isValid shouldBe true
    }

    "fail if too long or too short" in {
      Validations.validateTaxCode(taxCode + "1", field).isValid   shouldBe false
      Validations.validateTaxCode(taxCode.drop(1), field).isValid shouldBe false
    }

    "fail if any character is not an ASCII digit" in {
      Validations.validateTaxCode("/" + taxCode.drop(1), field).isValid                   shouldBe false
      Validations.validateTaxCode(":" + taxCode.drop(1), field).isValid                   shouldBe false
      Validations.validateTaxCode(taxCode.take(1) + "/" + taxCode.drop(2), field).isValid shouldBe false
      Validations.validateTaxCode(taxCode.take(1) + ":" + taxCode.drop(2), field).isValid shouldBe false
      Validations.validateTaxCode(taxCode.take(2) + "/", field).isValid                   shouldBe false
      Validations.validateTaxCode(taxCode.take(2) + ":", field).isValid                   shouldBe false
    }

    "fail with right validation failure" in {
      val invalidTaxCode = taxCode.drop(1)
      Validations.validateTaxCode(invalidTaxCode, field) match {
        case Invalid(Chain(InvalidTaxCode(s"$field was $invalidTaxCode"))) => ()
        case _                                                             => fail()
      }
    }
  }

  "validateNoItemsExpected" should {
    "succeed if the collection is empty" in {
      Validations.validateNoItemsExpected(Seq.empty, field).isValid shouldBe true
    }

    "fail if the collection is not empty" in {
      Validations.validateNoItemsExpected(nonEmptyCollection, field).isValid shouldBe false
    }

    "fail with right validation failure" in {
      Validations.validateNoItemsExpected(nonEmptyCollection, field) match {
        case Invalid(Chain(NoItemsExpected(s"$field had 1 item(s)"))) => ()
        case _                                                        => fail()
      }
    }
  }

  "validateItemsExpected" should {
    "fail if the collection is empty" in {
      Validations.validateItemsExpected(Seq.empty, field).isValid shouldBe false
    }

    "succeed if the collection is not empty" in {
      Validations.validateItemsExpected(nonEmptyCollection, field).isValid shouldBe true
    }

    "fail with right validation failure" in {
      Validations.validateItemsExpected(Seq.empty, field) match {
        case Invalid(Chain(ItemsExpected(s"$field had no items"))) => ()
        case _                                                     => fail()
      }
    }
  }

  "validateFieldNotExpected" should {
    "fail if the field is not empty" in {
      Validations.validateFieldNotExpected(Some(nonEmptyString), field).isValid shouldBe false
    }

    "succeed if field is empty" in {
      Validations.validateFieldNotExpected(Option.empty[String], field).isValid shouldBe true
    }

    "fail with right validation failure" in {
      Validations.validateFieldNotExpected(Some(nonEmptyString), field) match {
        case Invalid(Chain(FieldNotExpected(s"$field was present"))) => ()
        case _                                                       => fail()
      }
    }
  }

  "validateFieldExpected" should {
    "succeed if the field is not empty" in {
      Validations.validateFieldExpected(Some(nonEmptyString), field).isValid shouldBe true
    }

    "fail if field is empty" in {
      Validations.validateFieldExpected(Option.empty[String], field).isValid shouldBe false
    }

    "fail with right validation failure" in {
      Validations.validateFieldExpected(Option.empty[String], field) match {
        case Invalid(Chain(FieldExpected(s"$field was None"))) => ()
        case _                                                 => fail()
      }
    }
  }

  "validateSpiritsQuarter" should {
    "succeed if the period key is on a quarter" in {
      Validations.validateSpiritsQuarter(ReturnPeriod.fromPeriodKey(periodKeyMar).get, field).isValid shouldBe true
      Validations.validateSpiritsQuarter(ReturnPeriod.fromPeriodKey(periodKeyJun).get, field).isValid shouldBe true
      Validations.validateSpiritsQuarter(ReturnPeriod.fromPeriodKey(periodKeySep).get, field).isValid shouldBe true
      Validations.validateSpiritsQuarter(ReturnPeriod.fromPeriodKey(periodKeyDec).get, field).isValid shouldBe true
    }

    "fail if the period key is not on a quarter" in {
      Validations.validateSpiritsQuarter(ReturnPeriod.fromPeriodKey(periodKeyJan).get, field).isValid shouldBe false
      Validations.validateSpiritsQuarter(ReturnPeriod.fromPeriodKey(periodKeyFeb).get, field).isValid shouldBe false
      Validations.validateSpiritsQuarter(ReturnPeriod.fromPeriodKey(periodKeyApr).get, field).isValid shouldBe false
      Validations.validateSpiritsQuarter(ReturnPeriod.fromPeriodKey(periodKeyMay).get, field).isValid shouldBe false
      Validations.validateSpiritsQuarter(ReturnPeriod.fromPeriodKey(periodKeyJul).get, field).isValid shouldBe false
      Validations.validateSpiritsQuarter(ReturnPeriod.fromPeriodKey(periodKeyAug).get, field).isValid shouldBe false
      Validations.validateSpiritsQuarter(ReturnPeriod.fromPeriodKey(periodKeyOct).get, field).isValid shouldBe false
      Validations.validateSpiritsQuarter(ReturnPeriod.fromPeriodKey(periodKeyNov).get, field).isValid shouldBe false
    }
  }
}
