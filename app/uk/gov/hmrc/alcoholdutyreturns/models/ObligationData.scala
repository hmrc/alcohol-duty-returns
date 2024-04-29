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

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}
import play.api.libs.json.{Format, Json}

sealed trait ObligationStatus extends EnumEntry
object ObligationStatus extends Enum[ObligationStatus] with PlayJsonEnum[ObligationStatus] {
  val values = findValues

  case object Open extends ObligationStatus
  case object Fulfilled extends ObligationStatus
}

case class ObligationData(status: ObligationStatus)

object ObligationData {
  implicit val format: Format[ObligationData] = Json.format[ObligationData]
}