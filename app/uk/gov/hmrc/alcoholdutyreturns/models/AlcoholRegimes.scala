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

import cats.data.NonEmptySet
import play.api.libs.json.{Format, JsObject, JsPath, JsResult, JsValue, OFormat, Reads, Writes}

import scala.collection.immutable.SortedSet

case class AlcoholRegimes(regimes: NonEmptySet[AlcoholRegime])

object AlcoholRegimes {
  private[models] val reads: Reads[AlcoholRegimes] =
    (JsPath \ "regimes")
      .read[SortedSet[AlcoholRegime]]
      .map(regimes =>
        AlcoholRegimes(NonEmptySet.fromSet(regimes).getOrElse(throw new IllegalArgumentException("No regimes found")))
      )

  private[models] val writes: Writes[AlcoholRegimes] =
    (JsPath \ "regimes").write[SortedSet[AlcoholRegime]].contramap(_.regimes.toSortedSet)

  private val format: Format[AlcoholRegimes] = Format[AlcoholRegimes](reads, writes)

  implicit val alcoholRegimesFormat: OFormat[AlcoholRegimes] = new OFormat[AlcoholRegimes] {
    override def writes(o: AlcoholRegimes): JsObject            = format.writes(o).as[JsObject]
    override def reads(json: JsValue): JsResult[AlcoholRegimes] = format.reads(json)
  }
}
