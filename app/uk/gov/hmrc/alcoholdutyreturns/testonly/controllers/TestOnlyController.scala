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

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.alcoholdutyreturns.controllers.actions.{AuthorisedAction, CheckAppaIdAction}
import uk.gov.hmrc.alcoholdutyreturns.models.AlcoholRegime._
import uk.gov.hmrc.alcoholdutyreturns.models.ObligationStatus.Open
import uk.gov.hmrc.alcoholdutyreturns.models._
import uk.gov.hmrc.alcoholdutyreturns.repositories.UserAnswersRepository
import uk.gov.hmrc.alcoholdutyreturns.service.LockingService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.time.{Clock, LocalDate}
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class TestOnlyController @Inject() (
  cc: ControllerComponents,
  authorise: AuthorisedAction,
  checkAppaId: CheckAppaIdAction,
  userAnswersRepository: UserAnswersRepository,
  lockingService: LockingService,
  clock: Clock
)(implicit ec: ExecutionContext)
    extends BackendController(cc) {

  def clearAllData: Action[AnyContent] = Action.async { _ =>
    for {
      _ <- userAnswersRepository.collection.drop().toFuture()
      _ <- lockingService.releaseAllLocks()
    } yield Ok("All data cleared")
  }

  def createUserAnswers(beer: Boolean, cider: Boolean, wine: Boolean, spirits: Boolean, OFP: Boolean): Action[JsValue] =
    authorise(parse.json).async { implicit request =>
      withJsonBody[ReturnAndUserDetails] { returnAndUserDetails =>
        val returnId = returnAndUserDetails.returnId
        val appaId   = returnId.appaId

        checkAppaId(appaId).invokeBlock[JsValue](
          request,
          { implicit request =>
            lockingService
              .withLock(returnId, request.userId) {
                val alcoholRegimes      = getAlcoholRegimes(beer, cider, wine, spirits, OFP)
                val subscriptionSummary = SubscriptionSummary(ApprovalStatus.Approved, alcoholRegimes)
                val obligationData      = getObligationData(returnId.periodKey, LocalDate.now(clock))
                val userAnswers         =
                  UserAnswers.createUserAnswers(returnAndUserDetails, subscriptionSummary, obligationData, clock)
                for {
                  _                  <- userAnswersRepository.clearUserAnswersById(returnId)
                  createdUserAnswers <- userAnswersRepository.add(userAnswers)
                } yield Created(Json.toJson(createdUserAnswers))
              }
              .map {
                case Some(result) => result
                case None         => Locked
              }
          }
        )
      }
    }

  private def getObligationData(periodKey: String, now: LocalDate): ObligationData = ObligationData(
    status = Open,
    fromDate = now,
    toDate = now.plusDays(1),
    dueDate = now.plusDays(2),
    periodKey = periodKey
  )

  private def getAlcoholRegimes(
    beer: Boolean,
    cider: Boolean,
    wine: Boolean,
    spirits: Boolean,
    OFP: Boolean
  ): Set[AlcoholRegime] =
    Set(
      Some(Beer).filter(_ => beer),
      Some(Cider).filter(_ => cider),
      Some(Wine).filter(_ => wine),
      Some(Spirits).filter(_ => spirits),
      Some(OtherFermentedProduct).filter(_ => OFP)
    ).flatten
}
