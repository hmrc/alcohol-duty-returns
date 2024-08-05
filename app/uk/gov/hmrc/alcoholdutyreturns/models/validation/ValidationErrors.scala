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

import uk.gov.hmrc.alcoholdutyreturns.models.ReturnPeriod

sealed trait ValidationError {
  val errorMessage: String
}

case class PositiveNumberExpected(details: String) extends ValidationError {
  val errorMessage: String = s"Positive non-zero number expected: $details"
}

case class NegativeNumberExpected(details: String) extends ValidationError {
  val errorMessage: String = s"Negative non-zero number expected: $details"
}

case class NonNegativeNumberExpected(details: String) extends ValidationError {
  val errorMessage: String = s"Non-negative number expected: $details"
}

case class NonPositiveNumberExpected(details: String) extends ValidationError {
  val errorMessage: String = s"Non-positive number expected: $details"
}

case class ZeroExpected(details: String) extends ValidationError {
  val errorMessage: String = s"Zero expected: $details"
}

case class GreaterThanOrEqualToExpected(details: String, v1: BigDecimal, v2: BigDecimal) extends ValidationError {
  val errorMessage: String = s"$v1 is not greater than or equal to $v2: $details"
}

case class StringWasEmpty(details: String) extends ValidationError {
  val errorMessage: String = s"String was empty: $details"
}

case class StringTooLong(details: String, maxLength: Int) extends ValidationError {
  val errorMessage: String = s"String was too long (max $maxLength): $details"
}

case class InvalidTaxCode(details: String) extends ValidationError {
  val errorMessage: String = s"Invalid tax code: $details"
}

case class NoItemsExpected(details: String) extends ValidationError {
  val errorMessage: String = s"No items expected: $details"
}

case class ItemsExpected(details: String) extends ValidationError {
  val errorMessage: String = s"Items expected: $details"
}

case class FieldNotExpected(details: String) extends ValidationError {
  val errorMessage: String = s"Field was not expected: $details"
}

case class FieldExpected(details: String) extends ValidationError {
  val errorMessage: String = s"Field was expected: $details"
}

case class NotValidSpiritsQuarter(details: String, returnPeriod: ReturnPeriod) extends ValidationError {
  val errorMessage: String = s"Not a valid spirits quarter ${returnPeriod.toPeriodKey}: $details"
}
