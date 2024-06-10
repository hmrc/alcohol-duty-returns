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
import uk.gov.hmrc.alcoholdutyreturns.controllers.actions.AuthorisedAction
import uk.gov.hmrc.alcoholdutyreturns.models.audit.AuditReturnStarted
import uk.gov.hmrc.alcoholdutyreturns.models.{ErrorResponse, ObligationData, ReturnAndUserDetails, ReturnId, UserAnswers}
import uk.gov.hmrc.alcoholdutyreturns.repositories.{CacheRepository, UpdateFailure, UpdateSuccess}
import uk.gov.hmrc.alcoholdutyreturns.service.{AccountService, AuditService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CacheController @Inject() (
  authorise: AuthorisedAction,
  cacheRepository: CacheRepository,
  accountService: AccountService,
  auditService: AuditService,
  override val controllerComponents: ControllerComponents
)(implicit executionContext: ExecutionContext)
    extends BackendController(controllerComponents)
    with Logging {

  def get(appaId: String, periodKey: String): Action[AnyContent] =
    authorise.async { _ =>
      cacheRepository.get(ReturnId(appaId, periodKey)).map {
        case Some(ua) => Ok(Json.toJson(ua))
        case None     => NotFound
      }
    }

  def set(): Action[JsValue] =
    authorise(parse.json).async { implicit request =>
      withJsonBody[UserAnswers] { userAnswers =>
        cacheRepository.set(userAnswers).map {
          case UpdateSuccess => Ok(Json.toJson(userAnswers))
          case UpdateFailure => NotFound
        }
      }
    }

  def createUserAnswers(): Action[JsValue] =
    authorise(parse.json).async { implicit request =>
      withJsonBody[ReturnAndUserDetails] { returnAndUserDetails =>
        val returnId = returnAndUserDetails.returnId

        val eitherAccountDetails = for {
          subscriptionSummary <- accountService.getSubscriptionSummaryAndCheckStatus(returnId.appaId)
          obligationData      <- accountService.getOpenObligation(returnId)
        } yield (subscriptionSummary, obligationData)

        eitherAccountDetails.foldF(
          err => Future.successful(error(err)),
          accountDetails => {
            val (subscriptionSummary, obligationData) = accountDetails
            val userAnswers                           = UserAnswers.createUserAnswers(returnAndUserDetails, subscriptionSummary, obligationData)
            cacheRepository.add(userAnswers).map { userAnswers =>
              auditReturnStarted(userAnswers, obligationData)
              Ok(Json.toJson(userAnswers))
            }
          }
        )
      }
    }

  private def auditReturnStarted(userAnswers: UserAnswers, obligationData: ObligationData)(implicit
    hc: HeaderCarrier
  ): Unit = {
    val eventDetail = AuditReturnStarted(
      appaId = userAnswers.returnId.appaId,
      periodKey = userAnswers.returnId.periodKey,
      governmentGatewayId = userAnswers.internalId,
      governmentGatewayGroupId = userAnswers.groupId,
      obligationData = obligationData,
      alcoholRegimes = userAnswers.regimes.regimes.toSortedSet,
      returnStartedTime = userAnswers.lastUpdated,
      returnValidUntilTime = userAnswers.validUntil
    )

    auditService.audit(eventDetail)
  }

  def error(errorResponse: ErrorResponse): Result = Result(
    header = ResponseHeader(errorResponse.status),
    body = HttpEntity.Strict(ByteString(Json.toBytes(Json.toJson(errorResponse))), Some("application/json"))
  )
}
