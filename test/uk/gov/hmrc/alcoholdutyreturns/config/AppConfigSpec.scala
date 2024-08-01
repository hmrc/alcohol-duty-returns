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
    "microservice.services.alcohol-duty-calculator.port"                          -> "http",
    "microservice.services.alcohol-duty-calculator.host"                          -> "calchost",
    "microservice.services.alcohol-duty-calculator.port"                          -> 16003,
    "microservice.services.alcohol-duty-calculator.url.calculateDutyDueByTaxType" -> "/alcohol-duty-calculator/calculate-duty-due-by-tax-type",
    "microservice.services.returns.port"                                          -> "http",
    "microservice.services.returns.host"                                          -> "host",
    "microservice.services.returns.port"                                          -> 12345,
    "microservice.services.returns.url.getReturn"                                 -> "/etmp/RESTAdapter/excise/{0}/return/{1}/{2}",
    "microservice.services.returns.url.submitReturn"                              -> "/etmp/RESTAdapter/excise/{0}/return",
    "downstream-apis.regimeType"                                                  -> regime
  )
}

class AppConfigSpec extends SpecBaseWithConfigOverrides {
  "AppConfig" should {
    "get the calculateDutyDueByTaxType url" in {
      appConfig.getCalculateDutyDueByTaxTypeUrl shouldBe "http://calchost:16003/alcohol-duty-calculator/calculate-duty-due-by-tax-type"
    }

    "get the getReturn url" in {
      appConfig.getReturnUrl(
        returnId
      ) shouldBe s"http://host:12345/etmp/RESTAdapter/excise/${regime.toLowerCase}/return/${returnId.appaId}/${returnId.periodKey}"
    }

    "get the submitReturn url" in {
      appConfig.submitReturnUrl shouldBe s"http://host:12345/etmp/RESTAdapter/excise/${regime.toLowerCase}/return"
    }
  }
}
