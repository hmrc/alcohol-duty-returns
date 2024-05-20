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

package uk.gov.hmrc.alcoholdutyreturns.models.audit

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.alcoholdutyreturns.models.audit.AuditType.ReturnStarted

import java.time.{Instant, LocalDate}

case class AuditReturnStarted(
  producerId: String,
  periodKey: String,
  governmentGatewayId: String,
  governmentGatewayGroupId: String,
  obligationDetails: String,
  fromDate: String,
  toDate: String,
  dueDate: String,
  alcoholRegime: String,
  returnStartedTime: Instant,
  returnValidUntilDate: LocalDate
) extends AuditEventDetail {
  protected val _auditType = ReturnStarted
}

object AuditReturnStarted {
  implicit val format: OFormat[AuditReturnStarted] = Json.format[AuditReturnStarted]
}
