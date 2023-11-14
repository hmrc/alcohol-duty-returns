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

package uk.gov.hmrc.alcoholdutyreturns.controllers

import play.api.libs.json._
import play.api.mvc._
import uk.gov.hmrc.alcoholdutyreturns.controllers.actions.AuthorisedAction
import uk.gov.hmrc.alcoholdutyreturns.models.UserAnswers
import uk.gov.hmrc.alcoholdutyreturns.repositories.CacheRepository
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class CacheController @Inject() (
  authorise: AuthorisedAction,
  cacheRepository: CacheRepository,
  override val controllerComponents: ControllerComponents
)(implicit executionContext: ExecutionContext)
    extends BackendController(controllerComponents) {

  def get(internalId: String): Action[AnyContent] =
    authorise.async { _ =>
      cacheRepository.get(internalId).map {
        case Some(ua) => Ok(Json.toJson(ua))
        case None     => NotFound
      }
    }

  def set(internalId: String): Action[JsValue] =
    authorise(parse.json).async { implicit request =>
      withJsonBody[UserAnswers] { userAnswers =>
        cacheRepository.set(userAnswers).map(_ => Ok(Json.toJson(userAnswers)))
      }
    }
}
