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

import cats.data.Validated
import cats.implicits._
import uk.gov.hmrc.alcoholdutyreturns.models.ReturnPeriod

/**
  * Positive and negative tests do not include 0 as valid
  */
object Validations {
  def validatePositive(v: BigDecimal, field: String): ValidationResult =
    if (v > 0) Validated.unit else PositiveNumberExpected(s"$field was $v").invalidNec

  def validateNegative(v: BigDecimal, field: String): ValidationResult =
    if (v < 0) Validated.unit else NegativeNumberExpected(s"$field was $v").invalidNec

  def validateNonNegative(v: BigDecimal, field: String): ValidationResult =
    if (v >= 0) Validated.unit else NonNegativeNumberExpected(s"$field was $v").invalidNec

  def validateNonPositive(v: BigDecimal, field: String): ValidationResult =
    if (v <= 0) Validated.unit else NonPositiveNumberExpected(s"$field was $v").invalidNec

  def validateZero(v: BigDecimal, field: String): ValidationResult =
    if (v == 0) Validated.unit else ZeroExpected(s"$field was $v").invalidNec

  def validateGreaterThanOrEqualTo(v1: BigDecimal, v2: BigDecimal, field1: String, field2: String): ValidationResult =
    if (v1 >= v2) Validated.unit
    else GreaterThanOrEqualToExpected(s"$field1 was $v1; $field2 was $v2", v1, v2).invalidNec

  def validateStringNonEmpty(str: String, field: String): ValidationResult =
    if (str.nonEmpty) {
      Validated.unit
    } else {
      StringWasEmpty(s"$field was empty").invalidNec
    }

  def validateStringLength(str: String, field: String, maxLength: Int): ValidationResult =
    if (str.length > maxLength) {
      StringTooLong(s"$field was $str", maxLength).invalidNec
    } else {
      Validated.unit
    }

  def validateTaxCode(code: String, field: String): ValidationResult =
    if (code.length == 3 && code.forall(c => c >= '0' && c <= '9')) Validated.unit
    else InvalidTaxCode(s"$field was $code").invalidNec

  def validateNoItemsExpected[A](items: Iterable[A], field: String): ValidationResult =
    if (items.isEmpty) Validated.unit else NoItemsExpected(s"$field had ${items.size} item(s)").invalidNec

  def validateItemsExpected[A](items: Iterable[A], field: String): ValidationResult =
    if (items.nonEmpty) Validated.unit else ItemsExpected(s"$field had no items").invalidNec

  def validateFieldNotExpected[A](maybeField: Option[A], field: String): ValidationResult =
    if (maybeField.isEmpty) Validated.unit else FieldNotExpected(s"$field was present").invalidNec

  def validateFieldExpected[A](maybeField: Option[A], field: String): ValidationResult =
    if (maybeField.nonEmpty) Validated.unit else FieldExpected(s"$field was None").invalidNec

  def validateSpiritsQuarter[A](returnPeriod: ReturnPeriod, field: String): ValidationResult =
    if (returnPeriod.hasQuarterlySpirits) Validated.unit
    else NotValidSpiritsQuarter(s"$field was present", returnPeriod).invalidNec
}
