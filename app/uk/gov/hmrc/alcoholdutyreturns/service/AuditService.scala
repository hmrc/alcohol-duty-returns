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

package uk.gov.hmrc.alcoholdutyreturns.service

import com.google.inject.Inject
import play.api.Logging
import play.api.libs.json.{JsObject, Json, Writes}
import uk.gov.hmrc.alcoholdutyreturns.config.AppConfig
import uk.gov.hmrc.alcoholdutyreturns.models.audit.AuditEventDetail
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent
import uk.gov.hmrc.play.audit.AuditExtensions._
import uk.gov.hmrc.play.audit.http.connector.AuditResult.Failure

import scala.concurrent.{ExecutionContext, Future}

class AuditService @Inject() (appConfig: AppConfig, auditConnector: AuditConnector)(implicit ec: ExecutionContext)
    extends Logging {
  def audit[T <: AuditEventDetail](detail: T)(implicit hc: HeaderCarrier, writes: Writes[T]): Future[Boolean] =
    auditConnector
      .sendExtendedEvent(
        ExtendedDataEvent(
          auditSource = appConfig.appName,
          auditType = detail.auditType,
          tags = hc.toAuditTags(),
          detail = Json.toJson(detail).as[JsObject]
        )
      ).map {
        case Failure(msg, nested) =>
          logger.warn(s"Unable to audit ${detail.auditType} - $msg${nested.fold("")(t => s": ${t.getMessage}")}")
          false
        case _                    => true
      }
}
