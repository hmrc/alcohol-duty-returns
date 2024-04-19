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

package uk.gov.hmrc.alcoholdutyreturns.service

import cats.data.EitherT
import com.google.inject.{ImplementedBy, Inject, Singleton}
import play.api.libs.json.Json
import uk.gov.hmrc.alcoholdutyreturns.connector.AccountConnector
import uk.gov.hmrc.alcoholdutyreturns.models.ApprovalStatus.{Approved, Insolvent}
import uk.gov.hmrc.alcoholdutyreturns.models.ErrorResponse.{InvalidSubscriptionStatus, ObligationFulfilled}
import uk.gov.hmrc.alcoholdutyreturns.models.ObligationStatus.{Fulfilled, Open}
import uk.gov.hmrc.alcoholdutyreturns.models.{AlcoholRegime, ErrorResponse, ObligationData, ReturnId, UserAnswers}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AccountServiceImpl @Inject() (
  accountConnector: AccountConnector
) extends AccountService {

  def createUserAnswers(
    userAnswers: UserAnswers
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): EitherT[Future, ErrorResponse, UserAnswers]        =
    for {
      regimes <- checkSubscriptionAndGetRegimes(userAnswers.id.appaId)
      _       <- getObligation(userAnswers.id)
    } yield addAlcoholRegimeToUserAnswers(userAnswers, regimes)

  private def addAlcoholRegimeToUserAnswers(answers: UserAnswers, regimes: Seq[AlcoholRegime]): UserAnswers = {
    val data = Json.obj((AlcoholRegime.toString, Json.toJson(regimes)))
    answers.copy(data = data)
  }
  private def checkSubscriptionAndGetRegimes(
    appaId: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): EitherT[Future, ErrorResponse, Seq[AlcoholRegime]] = EitherT {
    accountConnector
      .getSubscriptionSummary(appaId)
      .fold(
        error => Left(error),
        subscription =>
          subscription.approvalStatus match {
            case Approved | Insolvent => Right(subscription.regimes)
            case status               => Left(InvalidSubscriptionStatus(status))
          }
      )
  }

  private def getObligation(
    returnId: ReturnId
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): EitherT[Future, ErrorResponse, ObligationData] =
    EitherT {
      accountConnector
        .getObligationData(returnId)
        .fold(
          error => Left(error),
          obligation =>
            obligation.status match {
              case Open      => Right(obligation)
              case Fulfilled => Left(ObligationFulfilled)
            }
        )
    }

}

@ImplementedBy(classOf[AccountServiceImpl])
trait AccountService {
  def createUserAnswers(
    userAnswers: UserAnswers
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): EitherT[Future, ErrorResponse, UserAnswers]
}
