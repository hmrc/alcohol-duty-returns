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

import org.mockito.ArgumentMatchers.any
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.alcoholdutyreturns.base.SpecBase
import uk.gov.hmrc.alcoholdutyreturns.config.AppConfig
import uk.gov.hmrc.alcoholdutyreturns.models.audit.AuditReturnStarted
import uk.gov.hmrc.alcoholdutyreturns.models.audit.AuditType.ReturnStarted
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.AuditExtensions._
import uk.gov.hmrc.play.audit.http.connector.AuditResult.{Disabled, Failure, Success}
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent

import java.time.{Instant, LocalDate}
import scala.concurrent.Future

class AuditServiceSpec extends SpecBase {
  "AuditService" should {
    "return true if Success is returned from the connector" in new SetUp {
      when(auditConnector.sendExtendedEvent(any)(any, any)).thenReturn(Future.successful(Success))
      whenReady(auditService.audit(auditDetail)) { _ shouldBe true}
    }

    "return true if Disabled is returned from the connector" in new SetUp {
      when(auditConnector.sendExtendedEvent(any)(any, any)).thenReturn(Future.successful(Disabled))
      whenReady(auditService.audit(auditDetail)) { _ shouldBe true}
    }

    "return false if Failure is returned from the connector with an exception" in new SetUp {
      when(auditConnector.sendExtendedEvent(any)(any, any)).thenReturn(Future.successful(Failure("failed", Some(new IllegalArgumentException("error")))))
      whenReady(auditService.audit(auditDetail)) { _ shouldBe false}
    }

    "return false if Failure is returned from the connector without an exception" in new SetUp {
      when(auditConnector.sendExtendedEvent(any)(any, any)).thenReturn(Future.successful(Failure("failed", None)))
      whenReady(auditService.audit(auditDetail)) { _ shouldBe false}
    }

    class SetUp {
      val now = Instant.now()
      val today = LocalDate.now()

      val auditDetail = AuditReturnStarted(
        "producerId",
        "periodKey",
        "governmentGatewayId",
        "governmentGatewayGroupId",
        "obligationDetails",
        "fromDate",
        "toDate",
        "dueDate",
        "alcoholRegime",
        now,
        today
      )

      val appName = "alcohol-duty-returns"
      implicit val hc: HeaderCarrier = HeaderCarrier()

      val appConfig = mock[AppConfig]
      val auditConnector = mock[AuditConnector]
      val auditService = new AuditService(appConfig, auditConnector)

      when(appConfig.appName).thenReturn(appName)
    }
  }
}
