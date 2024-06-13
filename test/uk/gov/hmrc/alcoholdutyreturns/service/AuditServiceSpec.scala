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
import org.scalatest.concurrent.Eventually.eventually
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.time.{Second, Span}
import uk.gov.hmrc.alcoholdutyreturns.base.SpecBase
import uk.gov.hmrc.alcoholdutyreturns.config.AppConfig
import uk.gov.hmrc.alcoholdutyreturns.models.audit.AuditReturnStarted
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import java.time.{Instant, LocalDate}

class AuditServiceSpec extends SpecBase {
  "AuditService" should {
    "call the audit connector" in new SetUp {
      doNothing.when(auditConnector).sendExplicitAudit(any, any[AuditReturnStarted])(any, any, any)

      auditService.audit(auditDetail)

      eventually(timeout) {
        verify(auditConnector, times(1)).sendExplicitAudit(any, any[AuditReturnStarted])(any, any, any)
      }
    }

    class SetUp {
      val timeout = Timeout(Span(1, Second))

      val now   = Instant.now()
      val today = LocalDate.now()

      val auditDetail = AuditReturnStarted(
        appaId = appaIdGen.sample.get,
        periodKey = "24AC",
        governmentGatewayId = "governmentGatewayId",
        governmentGatewayGroupId = "governmentGatewayGroupId",
        obligationData = getObligationData(today),
        alcoholRegimes = allAlcoholRegimes.regimes,
        returnStartedTime = now,
        returnValidUntilTime = Some(now)
      )

      val appName                    = "alcohol-duty-returns"
      implicit val hc: HeaderCarrier = HeaderCarrier()

      val appConfig      = mock[AppConfig]
      val auditConnector = mock[AuditConnector]
      val auditService   = new AuditService(auditConnector)

      when(appConfig.appName).thenReturn(appName)
    }
  }
}
