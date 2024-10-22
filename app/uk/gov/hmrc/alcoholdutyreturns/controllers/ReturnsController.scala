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

package uk.gov.hmrc.alcoholdutyreturns.controllers

import org.apache.pekko.util.ByteString
import play.api.Logging
import play.api.http.HttpEntity
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import uk.gov.hmrc.alcoholdutyreturns.connector.ReturnsConnector
import uk.gov.hmrc.alcoholdutyreturns.controllers.actions.{AuthorisedAction, CheckAppaIdAction}
import uk.gov.hmrc.alcoholdutyreturns.models.returns.{AdrReturnCreatedDetails, AdrReturnDetails, AdrReturnSubmission}
import uk.gov.hmrc.alcoholdutyreturns.models.{ErrorResponse, ReturnId}
import uk.gov.hmrc.alcoholdutyreturns.service.{LockingService, ReturnsService}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ReturnsController @Inject() (
  authorise: AuthorisedAction,
  checkAppaId: CheckAppaIdAction,
  returnsService: ReturnsService,
  lockingService: LockingService,
  returnsConnector: ReturnsConnector,
  override val controllerComponents: ControllerComponents
)(implicit executionContext: ExecutionContext)
    extends BackendController(controllerComponents)
    with Logging {
  def getReturn(appaId: String, periodKey: String): Action[AnyContent] =
    (authorise andThen checkAppaId(appaId)).async { implicit request =>
      returnsConnector
        .getReturn(ReturnId(appaId, periodKey))
        .map(AdrReturnDetails.fromGetReturnDetails)
        .fold(
          e => {
            logger.warn(s"Unable to get return $periodKey for $appaId: $e")
            error(e)
          },
          returnDetails => Ok(Json.toJson(returnDetails))
        )
    }

  def submitReturn(appaId: String, periodKey: String): Action[JsValue] =
    (authorise(parse.json) andThen checkAppaId(appaId)).async { implicit request =>
      withJsonBody[AdrReturnSubmission] { returnSubmission =>
        val returnId = ReturnId(appaId, periodKey)
        lockingService
          .withLockExecuteAndRelease(returnId, request.userId) {
            returnsService
              .submitReturn(returnSubmission, returnId)
              .map(AdrReturnCreatedDetails.fromReturnCreatedDetails)
              .fold(
                e => {
                  logger.warn(s"Unable to submit return $periodKey for $appaId: $e")
                  error(e)
                },
                returnCreatedDetails => Created(Json.toJson(returnCreatedDetails))
              )
          }
          .map {
            case Some(result) => result
            case None         => Locked
          }
      }
    }

  def error(errorResponse: ErrorResponse): Result = Result(
    header = ResponseHeader(errorResponse.status),
    body = HttpEntity.Strict(ByteString(Json.toBytes(Json.toJson(errorResponse))), Some("application/json"))
  )
}
