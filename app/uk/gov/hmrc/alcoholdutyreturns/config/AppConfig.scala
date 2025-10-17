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

import java.text.MessageFormat
import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.FiniteDuration

@Singleton
class AppConfig @Inject() (config: Configuration, servicesConfig: ServicesConfig) {
  val appName: String = config.get[String]("appName")

  private lazy val adrAccountHost: String =
    servicesConfig.baseUrl("alcohol-duty-accounts")

  private lazy val adrCalculatorHost: String =
    servicesConfig.baseUrl("alcohol-duty-calculator")

  private lazy val returnsHost: String =
    servicesConfig.baseUrl("returns")

  lazy val returnsClientId              = getConfStringAndThrowIfNotFound("returns.clientId")
  lazy val returnsSecret                = getConfStringAndThrowIfNotFound("returns.secret")
  lazy val returnsGetReturnUrlFormat    = new MessageFormat(getConfStringAndThrowIfNotFound("returns.url.getReturn"))
  lazy val returnsSubmitReturnUrlFormat = new MessageFormat(getConfStringAndThrowIfNotFound("returns.url.submitReturn"))

  val dbTimeToLiveInSeconds: Int    = config.get[Int]("mongodb.timeToLiveInSeconds")
  val lockingDurationInSeconds: Int = config.get[Int]("mongodb.lockingDurationInSeconds")

  def getSubscriptionSummaryUrl(appaId: String): String =
    s"$adrAccountHost/alcohol-duty-account/subscriptionSummary/$appaId"

  def getOpenObligationDataUrl(returnId: ReturnId) =
    s"$adrAccountHost/alcohol-duty-account/openObligationDetails/${returnId.appaId}/${returnId.periodKey}"

  def getOpenObligationsUrl(appaId: String): String =
    s"$adrAccountHost/alcohol-duty-account/obligationDetails/open/$appaId"

  def getFulfilledObligationsUrl(appaId: String): String =
    s"$adrAccountHost/alcohol-duty-account/obligationDetails/fulfilled/$appaId"

  def getCalculateDutyDueByTaxTypeUrl: String = {
    val url = getConfStringAndThrowIfNotFound("alcohol-duty-calculator.url.calculateDutyDueByTaxType")
    s"$adrCalculatorHost$url"
  }

  def getReturnUrl(returnId: ReturnId): String = {
    val url = returnsGetReturnUrlFormat.format(Array(regime.toLowerCase, returnId.appaId, returnId.periodKey))
    s"$returnsHost$url"
  }

  def submitReturnUrl: String = {
    val url = returnsSubmitReturnUrlFormat.format(Array(regime.toLowerCase))
    s"$returnsHost$url"
  }

  val enrolmentServiceName: String = config.get[String]("enrolment.serviceName")
  val enrolmentIdentifierKey       = config.get[String]("enrolment.identifierKey")

  val regime: String = config.get[String]("downstream-apis.regimeType")

  lazy val schemaDir = config.get[String]("schema-directory")

  private def getConfStringAndThrowIfNotFound(key: String) =
    servicesConfig.getConfString(key, throw new RuntimeException(s"Could not find services config key '$key'"))

  // API retry attempts
  lazy val retryAttempts: Int                 = config.get[Int]("microservice.services.retry.retry-attempts")
  lazy val retryAttemptsPost: Int             = config.get[Int]("microservice.services.retry.retry-attempts-post")
  lazy val retryAttemptsDelay: FiniteDuration =
    config.get[FiniteDuration]("microservice.services.retry.retry-attempts-delay")

  // Circuit breaker
  lazy val maxFailures: Int             = config.get[Int]("microservice.services.circuit-breaker.max-failures")
  lazy val callTimeout: FiniteDuration  =
    config.get[FiniteDuration]("microservice.services.circuit-breaker.call-timeout")
  lazy val resetTimeout: FiniteDuration =
    config.get[FiniteDuration]("microservice.services.circuit-breaker.reset-timeout")
}
