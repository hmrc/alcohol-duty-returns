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

import play.api.libs.json.{Format, JsError, JsResult, JsString, JsSuccess, JsValue, Reads, Writes}

enum AlcoholRegime:
  case Beer, Cider, Wine, Spirits, OtherFermentedProduct

object AlcoholRegime {
  given Format[AlcoholRegime] = new Format[AlcoholRegime] {
    def reads(json: JsValue): JsResult[AlcoholRegime] = json match {
      case JsString("Beer")                  => JsSuccess(Beer)
      case JsString("Cider")                 => JsSuccess(Cider)
      case JsString("Wine")                  => JsSuccess(Wine)
      case JsString("Spirits")               => JsSuccess(Spirits)
      case JsString("OtherFermentedProduct") => JsSuccess(OtherFermentedProduct)
      case _                                 => JsError("Invalid Alcohol Regime")
    }

    def writes(alcoholRegime: AlcoholRegime): JsValue = JsString(alcoholRegime.toString)
  }
}
