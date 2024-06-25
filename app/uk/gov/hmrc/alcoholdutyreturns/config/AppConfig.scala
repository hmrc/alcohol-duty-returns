/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.alcoholdutyreturns.config

import play.api.Configuration
import uk.gov.hmrc.alcoholdutyreturns.models.ReturnId
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}

@Singleton
class AppConfig @Inject() (config: Configuration, servicesConfig: ServicesConfig) {

  val appName: String = config.get[String]("appName")

  private lazy val adrAccountHost: String =
    servicesConfig.baseUrl("alcohol-duty-accounts")

  private lazy val returnsHost: String =
    servicesConfig.baseUrl("returns")

  val dbTimeToLiveInSeconds: Int = config.get[Int]("mongodb.timeToLiveInSeconds")

  def getSubscriptionSummaryUrl(appaId: String): String =
    s"$adrAccountHost/alcohol-duty-account/subscriptionSummary/$appaId"

  def getOpenObligationDataUrl(returnId: ReturnId) =
    s"$adrAccountHost/alcohol-duty-account/openObligationDetails/${returnId.appaId}/${returnId.periodKey}"

  def getObligationDataUrl(appaId: String): String =
    s"$adrAccountHost/alcohol-duty-account/obligationDetails/$appaId"

  def getReturnsUrl(returnId: ReturnId): String =
    s"$returnsHost/RESTAdapter/EXCISE/Return/$regime/${returnId.appaId}/${returnId.periodKey}"

  val enrolmentServiceName: String = config.get[String]("enrolment.serviceName")

  val regime: String = config.get[String]("downstream-apis.regimeType")
}
