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

import enumeratum.PlayJsonEnum
import enumeratum.{Enum, EnumEntry}
import play.api.http.Status.{FORBIDDEN, GONE, INTERNAL_SERVER_ERROR, NOT_FOUND, UNPROCESSABLE_ENTITY}

sealed abstract class ErrorResponse(val status: Int, val body: String) extends EnumEntry

object ErrorResponse extends Enum[ErrorResponse] with PlayJsonEnum[ErrorResponse] {
  val values = findValues

  case object EntityNotFound extends ErrorResponse(NOT_FOUND, "Entity not found")
  case object InvalidJson extends ErrorResponse(UNPROCESSABLE_ENTITY, "Invalid Json received")
  case object UnexpectedResponse extends ErrorResponse(INTERNAL_SERVER_ERROR, "Unexpected Response")
  case object ObligationFulfilled extends ErrorResponse(GONE, "Obligation fulfilled")
  case class InvalidSubscriptionStatus(approvalStatus: ApprovalStatus)
      extends ErrorResponse(FORBIDDEN, s"Invalid subscription status: $approvalStatus")
}
