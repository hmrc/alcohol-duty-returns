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

import play.api.libs.json.{Json, OFormat}

import java.time.Instant

case class HIPFailureResponse(failures: Seq[HIPFailure])

object HIPFailureResponse {
  implicit val HIPFailureResponseWrites: OFormat[HIPFailureResponse] = Json.format[HIPFailureResponse]
}

case class HIPFailure(`type`: String, reason: String)

object HIPFailure {
  implicit val HIPFailureWrites: OFormat[HIPFailure] = Json.format[HIPFailure]
}

case class DownstreamErrors(errors: Seq[DownstreamErrorsDetails])

object DownstreamErrors {
  implicit val downstreamErrorsWrites: OFormat[DownstreamErrors] = Json.format[DownstreamErrors]
}

case class DownstreamErrorsDetails(processingDate: Instant, code: String, text: String)

object DownstreamErrorsDetails {
  implicit val downstreamErrorsDetailsWrites: OFormat[DownstreamErrorsDetails] = Json.format[DownstreamErrorsDetails]
}

case class DownstreamError(error: DownstreamErrorDetails)

object DownstreamError {
  implicit val downstreamErrorWrites: OFormat[DownstreamError] = Json.format[DownstreamError]
}

case class DownstreamErrorDetails(code: String, message: String, logID: String)

object DownstreamErrorDetails {
  implicit val downstreamErrorDetailsWrites: OFormat[DownstreamErrorDetails] = Json.format[DownstreamErrorDetails]
}

case class DuplicateSubmissionError(errors: DownstreamErrorsDetails)

object DuplicateSubmissionError {
  implicit val duplicateSubmissionErrorWrites: OFormat[DuplicateSubmissionError] = Json.format[DuplicateSubmissionError]
}
