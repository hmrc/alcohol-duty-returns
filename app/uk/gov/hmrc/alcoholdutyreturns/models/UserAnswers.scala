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

package uk.gov.hmrc.alcoholdutyreturns.models

import play.api.libs.json._
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant

case class ReturnId(
  appaId: String,
  periodKey: String
)

object ReturnId {

  val reads: Reads[ReturnId] = {
    import play.api.libs.functional.syntax._

    (
      (__ \ "appaId").read[String] and
        (__ \ "periodKey").read[String]
    )(ReturnId.apply _)
  }

  val writes: OWrites[ReturnId] = {

    import play.api.libs.functional.syntax._
    (
      (__ \ "appaId").write[String] and
        (__ \ "periodKey").write[String]
    )(unlift(ReturnId.unapply))
  }

  implicit val format: OFormat[ReturnId] = OFormat(reads, writes)
}

case class UserAnswers(
  id: ReturnId,
  groupId: String,
  internalId: String,
  data: JsObject = Json.obj(),
  lastUpdated: Instant = Instant.now,
  validUntil: Option[Instant] = None
)

object UserAnswers {

  val reads: Reads[UserAnswers] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "_id").read[ReturnId] and
        (__ \ "groupId").read[String] and
        (__ \ "internalId").read[String] and
        (__ \ "data").read[JsObject] and
        (__ \ "lastUpdated").read(MongoJavatimeFormats.instantFormat) and
        (__ \ "validUntil").readNullable(MongoJavatimeFormats.instantFormat)
    )(UserAnswers.apply _)
  }

  val writes: OWrites[UserAnswers] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "_id").write[ReturnId] and
        (__ \ "groupId").write[String] and
        (__ \ "internalId").write[String] and
        (__ \ "data").write[JsObject] and
        (__ \ "lastUpdated").write(MongoJavatimeFormats.instantFormat) and
        (__ \ "validUntil").writeNullable(MongoJavatimeFormats.instantFormat)
    )(unlift(UserAnswers.unapply))
  }

  implicit val format: OFormat[UserAnswers] = OFormat(reads, writes)

}
