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

import cats.implicits._
import cats.data.EitherT
import com.google.inject.{Inject, Singleton}
import play.api.Logging
import uk.gov.hmrc.alcoholdutyreturns.connector.{CalculatorConnector, ReturnsConnector}
import uk.gov.hmrc.alcoholdutyreturns.models.ErrorResponse.BadRequest
import uk.gov.hmrc.alcoholdutyreturns.models.audit.AuditReturnSubmitted
import uk.gov.hmrc.alcoholdutyreturns.models.calculation.CalculateDutyDueByTaxTypeRequest
import uk.gov.hmrc.alcoholdutyreturns.models.{ErrorResponse, ReturnId, UserAnswers}
import uk.gov.hmrc.alcoholdutyreturns.models.returns.{AdrReturnCreatedDetails, AdrReturnSubmission, ReturnCreate, ReturnCreatedDetails, TotalDutyDuebyTaxType}
import uk.gov.hmrc.alcoholdutyreturns.repositories.CacheRepository
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ReturnsService @Inject() (
  returnsConnector: ReturnsConnector,
  calculatorConnector: CalculatorConnector,
  cacheRepository: CacheRepository,
  auditService: AuditService,
  schemaValidationService: SchemaValidationService
)(implicit
  ec: ExecutionContext
) extends Logging {
  private def calculateTotalDutyDueByTaxType(
    returnSubmission: AdrReturnSubmission
  )(implicit hc: HeaderCarrier): EitherT[Future, ErrorResponse, Option[Seq[TotalDutyDuebyTaxType]]] =
    CalculateDutyDueByTaxTypeRequest
      .fromReturnsSubmission(returnSubmission)
      .map(calculatorConnector.calculateDutyDueByTaxType)
      .traverse(_.map(_.convertToTotalDutyDuebyTaxType()))

  private def validateAgainstSchema(returnToSubmit: ReturnCreate): EitherT[Future, ErrorResponse, Unit] =
    EitherT(
      Future(
        if (!schemaValidationService.validateAgainstSchema(returnToSubmit)) {
          Left(BadRequest)
        } else {
          Right(())
        }
      )
    )

  def submitReturn(returnSubmission: AdrReturnSubmission, returnId: ReturnId)(implicit
    hc: HeaderCarrier
  ): EitherT[Future, ErrorResponse, ReturnCreatedDetails] = {
    val returnConvertedToSubmissionFormat = ReturnCreate.fromAdrReturnSubmission(returnSubmission, returnId.periodKey)

    for {
      maybeTotalDutyDueByTaxType <- calculateTotalDutyDueByTaxType(returnSubmission)
      returnToSubmit              = returnConvertedToSubmissionFormat.copy(totalDutyDuebyTaxType = maybeTotalDutyDueByTaxType)
      _                          <- validateAgainstSchema(returnToSubmit)
      returnCreatedDetails       <- returnsConnector.submitReturn(
                                      returnToSubmit,
                                      returnId.appaId
                                    )
      _                           = cacheRepository.get(returnId).map {
                                      case Some(ua) =>
                                        auditReturnSubmitted(
                                          Some(ua),
                                          returnToSubmit,
                                          AdrReturnCreatedDetails.fromReturnCreatedDetails(returnCreatedDetails),
                                          returnId
                                        )
                                      case None     =>
                                        logger.warn("User answers couldn't be retrieved while auditing return submission")
                                        auditReturnSubmitted(
                                          None,
                                          returnToSubmit,
                                          AdrReturnCreatedDetails.fromReturnCreatedDetails(returnCreatedDetails),
                                          returnId
                                        )
                                    }
      _                          <- EitherT.right[ErrorResponse](cacheRepository.clearUserAnswersById(returnId))
    } yield returnCreatedDetails
  }

  private def auditReturnSubmitted(
    userAnswers: Option[UserAnswers],
    returnToSubmit: ReturnCreate,
    returnCreatedDetails: AdrReturnCreatedDetails,
    returnId: ReturnId
  )(implicit
    hc: HeaderCarrier
  ): Unit =
    userAnswers
      .map { userAnswers =>
        val eventDetail = AuditReturnSubmitted(
          appaId = returnId.appaId,
          periodKey = returnId.periodKey,
          governmentGatewayId = userAnswers.internalId,
          governmentGatewayGroupId = userAnswers.groupId,
          returnSubmittedTime = returnCreatedDetails.processingDate,
          alcoholRegimes = userAnswers.regimes.regimes,
          requestPayload = returnToSubmit,
          responsePayload = returnCreatedDetails
        )
        auditService.audit(eventDetail)
      }
      .getOrElse(logger.warn("No user answers found"))

}
