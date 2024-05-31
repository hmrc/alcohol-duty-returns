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

import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.alcoholdutyreturns.controllers.actions.AuthorisedAction
import uk.gov.hmrc.alcoholdutyreturns.service.AccountService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import play.api.libs.json._

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ObligationController @Inject() (
  authorise: AuthorisedAction,
  accountService: AccountService,
  override val controllerComponents: ControllerComponents
)(implicit executionContext: ExecutionContext)
    extends BackendController(controllerComponents) {
  def getObligationDetails(appaId: String): Action[AnyContent] =
    authorise.async { implicit request =>
      accountService.getObligations(appaId).value.map {
        case Left(errorResponse) => NotFound(s"Error: {$errorResponse}")
        case Right(obligations)  => Ok(Json.toJson(obligations))
      }
    }

}
