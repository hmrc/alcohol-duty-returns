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

package helpers

import java.time.{LocalDate, YearMonth}
import scala.util.matching.Regex

object PeriodKey {
  private val periodKeyPattern: Regex = """^(\d{2}A[A-L])$""".r

  def fromLocalDate(periodFrom: LocalDate): String =
    s"${periodFrom.getYear.toString.takeRight(2)}A${(periodFrom.getMonthValue + 64).toChar}"

  def toYearMonth(periodKey: String): YearMonth =
    periodKey match {
      case periodKeyPattern(_) =>
        val year  = periodKey.substring(0, 2).toInt + 2000
        val month = periodKey.charAt(3) - 'A' + 1
        YearMonth.of(year, month)
      case _                   => throw new IllegalArgumentException(s"Bad period key $periodKey")
    }
}
