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

package uk.gov.hmrc.alcoholdutyreturns.config

import uk.gov.hmrc.alcoholdutyreturns.base.SpecBase

class SpecBaseWithConfigOverrides extends SpecBase {
  override def configOverrides: Map[String, Any] = Map(
    "microservice.services.returns.port"             -> "http",
    "microservice.services.returns.host"             -> "host",
    "microservice.services.returns.port"             -> 12345,
    "microservice.services.returns.url.getReturn"    -> "/etmp/RESTAdapter/excise/{0}/return/{1}/{2}",
    "microservice.services.returns.url.submitReturn" -> "/etmp/RESTAdapter/excise/{0}/return",
    "downstream-apis.regimeType"                     -> regime
  )
}

class AppConfigSpec extends SpecBaseWithConfigOverrides {
  "AppConfig" should {
    "return the getReturn url" in {
      appConfig.getReturnUrl(
        returnId
      ) shouldBe s"http://host:12345/etmp/RESTAdapter/excise/${regime.toLowerCase}/return/${returnId.appaId}/${returnId.periodKey}"
    }

    "return the submitReturn url" in {
      appConfig.submitReturnUrl() shouldBe s"http://host:12345/etmp/RESTAdapter/excise/${regime.toLowerCase}/return"
    }
  }
}
