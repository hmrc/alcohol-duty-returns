/*
 * Copyright 2026 HM Revenue & Customs
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

import play.api.libs.json.{Format, JsError, JsResult, JsString, JsSuccess, JsValue, Json, OFormat, Reads, Writes}
import java.time.LocalDate

enum ObligationStatus:
  case Open, Fulfilled

object ObligationStatus {
  given Format[ObligationStatus] = new Format[ObligationStatus] {
    def reads(json: JsValue): JsResult[ObligationStatus] = json match {
      case JsString("Open")      => JsSuccess(Open)
      case JsString("Fulfilled") => JsSuccess(Fulfilled)
      case _                     => JsError("Invalid Obligation Status")
    }

    def writes(obligationStatus: ObligationStatus): JsValue = JsString(obligationStatus.toString)
  }
}

case class ObligationData(
  status: ObligationStatus,
  fromDate: LocalDate,
  toDate: LocalDate,
  dueDate: LocalDate,
  periodKey: String
)

object ObligationData {
  implicit val format: OFormat[ObligationData] = Json.format[ObligationData]

  override def toString: String = "obligationData"
}

case class FulfilledObligations(
  year: Int,
  obligations: Seq[ObligationData]
)

object FulfilledObligations {
  implicit val fulfilledObligationsFormat: OFormat[FulfilledObligations] = Json.format[FulfilledObligations]
}
