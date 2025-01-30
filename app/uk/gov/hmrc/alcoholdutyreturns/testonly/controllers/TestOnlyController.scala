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
import uk.gov.hmrc.alcoholdutyreturns.models.ObligationStatus.{Fulfilled, Open}
import uk.gov.hmrc.alcoholdutyreturns.models.{AlcoholRegime, ApprovalStatus, ObligationData, ReturnAndUserDetails, SubscriptionSummary, UserAnswers}
import uk.gov.hmrc.alcoholdutyreturns.repositories.UserAnswersRepository
import uk.gov.hmrc.alcoholdutyreturns.service.LockingService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class TestOnlyController @Inject() (
  cc: ControllerComponents,
  authorise: AuthorisedAction,
  checkAppaId: CheckAppaIdAction,
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

  def createUserAnswers(regimes: String): Action[JsValue] =
    authorise(parse.json).async { implicit request =>
      withJsonBody[ReturnAndUserDetails] { returnAndUserDetails =>
        val returnId = returnAndUserDetails.returnId
        val appaId   = returnId.appaId

        Try(getRegimeFlags(regimes)) match {
          case Success((noOFP, noSpirits, noWine, noCiderNorPerry, noBeer)) =>
            checkAppaId(appaId).invokeBlock[JsValue](
              request,
              { implicit request =>
                lockingService
                  .withLock(returnId, request.userId) {
                    val alcoholRegimes      = flagsToApprovalTypes(noOFP, noSpirits, noWine, noCiderNorPerry, noBeer)
                    val subscriptionSummary = SubscriptionSummary(ApprovalStatus.Approved, alcoholRegimes)
                    val obligationData      = getObligationData(returnId.periodKey, LocalDate.now())
                    val userAnswers         =
                      UserAnswers.createUserAnswers(returnAndUserDetails, subscriptionSummary, obligationData)
                    userAnswersRepository.add(userAnswers).map { userAnswers =>
                      Created(Json.toJson(userAnswers))
                    }
                  }
                  .map {
                    case Some(result) => result
                    case None         => Locked
                  }
              }
            )
          case Failure(e)                                                   => Future.successful(BadRequest(e.getMessage))
        }
      }
    }

  private def getObligationData(periodKey: String, now: LocalDate): ObligationData = ObligationData(
    status = Open,
    fromDate = now,
    toDate = now.plusDays(1),
    dueDate = now.plusDays(2),
    periodKey = periodKey
  )

  private def getRegimeFlags(flagDigits: String) =
    if (!flagDigits.matches("^\\d{2}$")) {
      throw new RuntimeException("Invalid flag digits to specify regimes")
    } else {
      val flags = flagDigits.toInt

      val noOFP           = (flags & 0x40) != 0
      val noSpirits       = (flags & 0x08) != 0
      val noWine          = (flags & 0x04) != 0
      val noCiderNorPerry = (flags & 0x02) != 0
      val noBeer          = (flags & 0x01) != 0

      (noOFP, noSpirits, noWine, noCiderNorPerry, noBeer)
    }

  private def flagsToApprovalTypes(
    noOFP: Boolean,
    noSpirits: Boolean,
    noWine: Boolean,
    noCiderNorPerry: Boolean,
    noBeer: Boolean
  ): Set[AlcoholRegime] =
    Set(
      Some(Beer).filterNot(_ => noBeer),
      Some(Cider).filterNot(_ => noCiderNorPerry),
      Some(Wine).filterNot(_ => noWine),
      Some(Spirits).filterNot(_ => noSpirits),
      Some(OtherFermentedProduct).filterNot(_ => noOFP)
    ).flatten
}
