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

package uk.gov.hmrc.alcoholdutyreturns.models

import play.api.libs.json.{Format, JsObject, JsPath, JsResult, JsValue, Json, OFormat, OWrites, Reads}

case class AlcoholRegimes(regimes: Set[AlcoholRegime])

object AlcoholRegimes {
  private[models] val reads: Reads[AlcoholRegimes] =
    (JsPath \ "regimes")
      .read[Set[AlcoholRegime]]
      .map(regimes =>
        if (regimes.nonEmpty) {
          AlcoholRegimes(regimes)
        } else {
          throw new IllegalArgumentException("Expecting at least one regime to be approved")
        }
      )

  private[models] val writes: OWrites[AlcoholRegimes] = Json.writes[AlcoholRegimes]

  private val format: Format[AlcoholRegimes] = Format[AlcoholRegimes](reads, writes)

  implicit val alcoholRegimesFormat: OFormat[AlcoholRegimes] = new OFormat[AlcoholRegimes] {
    override def writes(o: AlcoholRegimes): JsObject            = format.writes(o).as[JsObject]
    override def reads(json: JsValue): JsResult[AlcoholRegimes] = format.reads(json)
  }
}
