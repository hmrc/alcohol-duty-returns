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

package uk.gov.hmrc.alcoholdutyreturns.testonly.controllers

import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.alcoholdutyreturns.repositories.UserAnswersRepository
import uk.gov.hmrc.alcoholdutyreturns.service.LockingService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class TestOnlyController @Inject() (
  cc: ControllerComponents,
  userAnswersRepository: UserAnswersRepository,
  lockingService: LockingService
)(implicit ec: ExecutionContext)
    extends BackendController(cc) {

  def clearAllData: Action[AnyContent] = Action.async { _ =>
    for {
      _ <- userAnswersRepository.collection.drop().toFuture()
      _ <- lockingService.releaseAllLocks()
    } yield Ok("All data cleared")
  }
}
