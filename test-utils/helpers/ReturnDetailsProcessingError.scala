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

package helpers

import play.api.libs.json.{Json, OWrites}

import java.time.Instant

case class ReturnDetailsProcessingError(errors: ReturnDetailsProcessingErrorInternal)

object ReturnDetailsProcessingError {
  implicit val returnDetailsProcessingErrorWrites: OWrites[ReturnDetailsProcessingError] =
    Json.writes[ReturnDetailsProcessingError]
}

case class ReturnDetailsProcessingErrorInternal(processingDate: Instant, code: String, text: String)

object ReturnDetailsProcessingErrorInternal {
  implicit val returnDetailsProcessingErrorInternalWrites: OWrites[ReturnDetailsProcessingErrorInternal] =
    Json.writes[ReturnDetailsProcessingErrorInternal]
}

case class ReturnDetailsInternalServerError(error: ReturnDetailsInternalServerErrorInternal)

object ReturnDetailsInternalServerError {
  implicit val returnDetailsProcessingErrorWrites: OWrites[ReturnDetailsInternalServerError] =
    Json.writes[ReturnDetailsInternalServerError]
}

case class ReturnDetailsInternalServerErrorInternal(code: String, message: String, logID: String)

object ReturnDetailsInternalServerErrorInternal {
  implicit val returnDetailsProcessingErrorInternalWrites: OWrites[ReturnDetailsInternalServerErrorInternal] =
    Json.writes[ReturnDetailsInternalServerErrorInternal]
}
