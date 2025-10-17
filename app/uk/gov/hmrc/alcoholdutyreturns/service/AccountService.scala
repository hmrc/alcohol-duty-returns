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
import com.google.inject.{Inject, Singleton}
import play.api.Logging
import uk.gov.hmrc.alcoholdutyreturns.connector.AccountConnector
import uk.gov.hmrc.alcoholdutyreturns.models.ApprovalStatus.{Approved, Insolvent}
import uk.gov.hmrc.alcoholdutyreturns.models.ObligationStatus.{Fulfilled, Open}
import uk.gov.hmrc.alcoholdutyreturns.models.{ErrorCodes, FulfilledObligations, ObligationData, ReturnId, SubscriptionSummary}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.ErrorResponse

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AccountService @Inject() (
  accountConnector: AccountConnector
) extends Logging {
  def getSubscriptionSummaryAndCheckStatus(
    appaId: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): EitherT[Future, ErrorResponse, SubscriptionSummary] = EitherT {
    accountConnector
      .getSubscriptionSummary(appaId)
      .fold(
        error => {
          logger.warn(s"Unable to get subscription summary for $appaId - ${error.statusCode} ${error.message}")
          Left(error)
        },
        subscriptionSummary =>
          subscriptionSummary.approvalStatus match {
            case Approved | Insolvent => Right(subscriptionSummary)
            case status               =>
              logger.warn(s"Invalid subscription status for $appaId ${status.entryName}")
              Left(ErrorCodes.invalidSubscriptionStatus(status))
          }
      )
  }

  def getOpenObligation(
    returnId: ReturnId
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): EitherT[Future, ErrorResponse, ObligationData] =
    EitherT {
      accountConnector
        .getOpenObligationData(returnId)
        .fold(
          error => {
            logger.warn(
              s"Unable to get an open obligation for ${returnId.appaId} ${returnId.periodKey} - ${error.statusCode} ${error.message}"
            )
            Left(error)
          },
          obligation =>
            obligation.status match {
              case Open      => Right(obligation)
              case Fulfilled =>
                logger.warn(
                  s"Unexpected fulfilled obligation returned (expected open) for ${returnId.appaId} ${returnId.periodKey}"
                )
                Left(ErrorCodes.obligationFulfilled)
            }
        )
    }

  def getOpenObligations(
    appaId: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): EitherT[Future, ErrorResponse, Seq[ObligationData]] =
    accountConnector.getOpenObligations(appaId).leftFlatMap { error =>
      logger.warn(s"Unable to get open obligations for $appaId - ${error.statusCode} ${error.message}")
      EitherT.leftT[Future, Seq[ObligationData]](ErrorCodes.unexpectedResponse)
    }

  def getFulfilledObligations(
    appaId: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): EitherT[Future, ErrorResponse, Seq[FulfilledObligations]] =
    accountConnector.getFulfilledObligations(appaId).leftFlatMap { error =>
      logger.warn(s"Unable to get fulfilled obligations for $appaId - ${error.statusCode} ${error.message}")
      EitherT.leftT[Future, Seq[FulfilledObligations]](ErrorCodes.unexpectedResponse)
    }
}
