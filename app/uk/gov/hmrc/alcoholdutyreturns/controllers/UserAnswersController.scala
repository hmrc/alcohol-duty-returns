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

import org.apache.pekko.util.ByteString
import play.api.Logging
import play.api.http.HttpEntity
import play.api.libs.json._
import play.api.mvc._
import uk.gov.hmrc.alcoholdutyreturns.controllers.actions.{AuthorisedAction, CheckAppaIdAction}
import uk.gov.hmrc.alcoholdutyreturns.models.{ReturnAndUserDetails, ReturnId, UserAnswers}
import uk.gov.hmrc.alcoholdutyreturns.repositories.{UpdateFailure, UpdateSuccess, UserAnswersRepository}
import uk.gov.hmrc.alcoholdutyreturns.service.{AccountService, LockingService}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.play.bootstrap.http.ErrorResponse

import java.time.Clock
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UserAnswersController @Inject() (
  authorise: AuthorisedAction,
  checkAppaId: CheckAppaIdAction,
  userAnswersRepository: UserAnswersRepository,
  lockingService: LockingService,
  accountService: AccountService,
  clock: Clock,
  override val controllerComponents: ControllerComponents
)(implicit executionContext: ExecutionContext)
    extends BackendController(controllerComponents)
    with Logging {

  def get(appaId: String, periodKey: String): Action[AnyContent] =
    (authorise andThen checkAppaId(appaId)).async { implicit request =>
      val returnId = ReturnId(appaId, periodKey)
      lockingService
        .withLock(returnId, request.userId) {
          userAnswersRepository.get(returnId).map {
            case Some(ua) => Ok(Json.toJson(ua))
            case None     => NotFound
          }
        }
        .map {
          case Some(result) => result
          case None         => Locked
        }
    }

  def set(): Action[JsValue] =
    authorise(parse.json).async { implicit request =>
      withJsonBody[UserAnswers] { userAnswers =>
        val appaId = userAnswers.returnId.appaId

        checkAppaId(appaId).invokeBlock[JsValue](
          request,
          { implicit request =>
            lockingService
              .withLock(userAnswers.returnId, request.userId) {
                userAnswersRepository.set(userAnswers).map {
                  case UpdateSuccess => Ok(Json.toJson(userAnswers))
                  case UpdateFailure => NotFound
                }
              }
              .map {
                case Some(result) => result
                case None         => Locked
              }
          }
        )
      }
    }

  def createUserAnswers(): Action[JsValue] =
    authorise(parse.json).async { implicit request =>
      withJsonBody[ReturnAndUserDetails] { returnAndUserDetails =>
        val returnId = returnAndUserDetails.returnId
        val appaId   = returnId.appaId

        checkAppaId(appaId).invokeBlock[JsValue](
          request,
          { implicit request =>
            lockingService
              .withLock(returnId, request.userId) {
                val eitherAccountDetails = for {
                  subscriptionSummary <- accountService.getSubscriptionSummaryAndCheckStatus(appaId)
                  obligationData      <- accountService.getOpenObligation(returnId)
                } yield (subscriptionSummary, obligationData)

                eitherAccountDetails.foldF(
                  err => {
                    logger.warn(
                      s"Unable to create userAnswers for $appaId ${returnId.periodKey} - ${err.statusCode} ${err.message}"
                    )
                    Future.successful(error(err))
                  },
                  accountDetails => {
                    val (subscriptionSummary, obligationData) = accountDetails
                    val userAnswers                           =
                      UserAnswers.createUserAnswers(returnAndUserDetails, subscriptionSummary, obligationData, clock)
                    userAnswersRepository.add(userAnswers).map { userAnswers =>
                      Created(Json.toJson(userAnswers))
                    }
                  }
                )
              }
              .map {
                case Some(result) => result
                case None         => Locked
              }
          }
        )
      }
    }

  def delete(appaId: String, periodKey: String): Action[AnyContent] =
    (authorise andThen checkAppaId(appaId)).async { implicit request =>
      val returnId = ReturnId(appaId, periodKey)
      lockingService
        .withLock(returnId, request.userId) {
          userAnswersRepository.clearUserAnswersById(returnId).map { _ =>
            Ok(s"User answers deleted for user ${request.userId} on return $appaId/$periodKey")
          }
        }
        .map {
          case Some(result) => result
          case None         => Locked
        }
    }

  def releaseReturnLock(appaId: String, periodKey: String): Action[AnyContent] =
    (authorise andThen checkAppaId(appaId)).async { implicit request =>
      lockingService
        .releaseLock(ReturnId(appaId, periodKey), request.userId)
        .map(_ => Ok(s"Locked release for user ${request.userId} on return $appaId/$periodKey"))
    }

  def keepAlive(appaId: String, periodKey: String): Action[AnyContent] =
    (authorise andThen checkAppaId(appaId)).async { implicit request =>
      lockingService
        .keepAlive(ReturnId(appaId, periodKey), request.userId)
        .map { result =>
          if (result) Ok("Lock refreshed") else Locked
        }
    }

  def error(errorResponse: ErrorResponse): Result = Result(
    header = ResponseHeader(errorResponse.statusCode),
    body = HttpEntity.Strict(ByteString(Json.toBytes(Json.toJson(errorResponse))), Some("application/json"))
  )
}
