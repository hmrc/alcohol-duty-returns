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

import play.api.libs.json.Json
import uk.gov.hmrc.alcoholdutyreturns.base.SpecBase

import java.time.Instant

class ErrorsSpec extends SpecBase {
  "ReturnDetailsError must" - {
    "serialise DownstreamErrors to json" in new SetUp {
      Json.toJson(downstreamErrors).toString mustBe downstreamErrorsJson
    }

    "deserialise DownstreamErrors from json" in new SetUp {
      Json.parse(downstreamErrorsJson).as[DownstreamErrors] mustBe downstreamErrors
    }

    "serialise DownstreamError to json" in new SetUp {
      Json.toJson(downstreamError).toString mustBe internalServerErrorJson
    }

    "deserialise DownstreamError from json" in new SetUp {
      Json.parse(internalServerErrorJson).as[DownstreamError] mustBe downstreamError
    }

    "serialise HIPFailureResponse to json" in new SetUp {
      Json.toJson(hipFailureResponse).toString mustBe hipFailureResponseJson
    }

    "deserialise HIPFailureResponse from json" in new SetUp {
      Json.parse(hipFailureResponseJson).as[HIPFailureResponse] mustBe hipFailureResponse
    }
  }

  class SetUp {
    val now        = Instant.now(clock)
    val errorCode  = "003"
    val text       = "Request could not be processed."
    val returnCode = "500"
    val message    = "error"
    val logId      = "id"
    val `type`     = "Errortype"
    val reason     = "There is no reason"

    val hipFailureResponse = HIPFailureResponse(Seq(HIPFailure(`type`, reason)))
    val downstreamErrors   = DownstreamErrors(Seq(DownstreamErrorsDetails(now, errorCode, text)))
    val downstreamError    = DownstreamError(DownstreamErrorDetails(returnCode, message, logId))

    val downstreamErrorsJson    =
      s"""{"errors":[{"processingDate":"2024-06-11T15:07:47.838Z","code":"$errorCode","text":"$text"}]}"""
    val internalServerErrorJson =
      s"""{"error":{"code":"$returnCode","message":"$message","logID":"$logId"}}"""
    val hipFailureResponseJson  = s"""{"failures":[{"type":"${`type`}","reason":"$reason"}]}"""
  }
}
