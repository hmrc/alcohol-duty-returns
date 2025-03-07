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
import cats.implicits._
import com.google.inject.{Inject, Singleton}
import play.api.Logging
import uk.gov.hmrc.alcoholdutyreturns.connector.{AccountConnector, CalculatorConnector, ReturnsConnector}
import uk.gov.hmrc.alcoholdutyreturns.models.calculation.CalculateDutyDueByTaxTypeRequest
import uk.gov.hmrc.alcoholdutyreturns.models.returns.{AdrReturnSubmission, ReturnCreate, ReturnCreatedDetails, TotalDutyDuebyTaxType}
import uk.gov.hmrc.alcoholdutyreturns.models.{ErrorCodes, ReturnId}
import uk.gov.hmrc.alcoholdutyreturns.repositories.UserAnswersRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.backend.http.ErrorResponse

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ReturnsService @Inject() (
  returnsConnector: ReturnsConnector,
  accountConnector: AccountConnector,
  calculatorConnector: CalculatorConnector,
  userAnswersRepository: UserAnswersRepository,
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
          Left(ErrorCodes.badRequest)
        } else {
          Right(())
        }
      )
    )

  def submitReturn(returnSubmission: AdrReturnSubmission, returnId: ReturnId)(implicit
    hc: HeaderCarrier
  ): EitherT[Future, ErrorResponse, ReturnCreatedDetails] = {
    val returnConvertedToSubmissionFormat = ReturnCreate.fromAdrReturnSubmission(returnSubmission, returnId.periodKey)

    val returnCreatedDetailsEither = for {
      maybeTotalDutyDueByTaxType <- calculateTotalDutyDueByTaxType(returnSubmission)
      returnToSubmit              = returnConvertedToSubmissionFormat.copy(totalDutyDuebyTaxType = maybeTotalDutyDueByTaxType)
      _                          <- validateAgainstSchema(returnToSubmit)
      returnCreatedDetails       <- returnsConnector.submitReturn(returnToSubmit, returnId.appaId)
    } yield returnCreatedDetails

    EitherT(
      returnCreatedDetailsEither.value.flatMap {
        case Right(returnCreatedDetails)                                               =>
          for {
            _                        <-
              userAnswersRepository
                .get(returnId)
                .recover { case _ =>
                  logger.warn(
                    s"Failed retrieving user answers for returnId=$returnId. Continuing the process and auditing without user answers. "
                  )
                  None
                }
            _                        <- userAnswersRepository.clearUserAnswersById(returnId)
            returnCreatedDetailsRight = Right(returnCreatedDetails)
          } yield returnCreatedDetailsRight
        case Left(errorResponse) if errorResponse == ErrorCodes.returnAlreadySubmitted =>
          handleReturnAlreadySubmitted(returnId)
        case Left(errorResponse)                                                       =>
          Future.successful(Left(errorResponse))
      }
    )
  }

  private def handleReturnAlreadySubmitted(returnId: ReturnId)(implicit
    hc: HeaderCarrier
  ): Future[Either[ErrorResponse, ReturnCreatedDetails]] = {
    val submittedReturn = for {
      _          <- EitherT.right[ErrorResponse](userAnswersRepository.clearUserAnswersById(returnId))
      returnData <- returnsConnector.getReturn(returnId)
    } yield returnData

    submittedReturn.value.flatMap {
      case Left(_)              => Future.successful(Left(ErrorCodes.errorHandlingDuplicateSubmission))
      case Right(returnDetails) =>
        val amountDue            = returnDetails.totalDutyDue.totalDutyDue
        val chargeReference      = returnDetails.chargeDetails.chargeReference
        val returnCreatedDetails = ReturnCreatedDetails(
          processingDate = returnDetails.processingDate,
          adReference = returnDetails.idDetails.adReference,
          amount = amountDue,
          chargeReference = chargeReference,
          paymentDueDate = None,
          submissionID = returnDetails.idDetails.submissionID
        )
        if (amountDue > 0) {
          logger.info("Getting outstanding payments to search for matching charge reference")
          accountConnector.getOutstandingPayments(returnId.appaId).value.map {
            case Left(_)             => Left(ErrorCodes.errorHandlingDuplicateSubmission)
            case Right(openPayments) =>
              val matchingPayments = openPayments.outstandingPayments.filter(_.chargeReference == chargeReference)
              if (matchingPayments.length == 1) {
                logger.info("Charge reference found, proceeding to return submitted view")
                Right(returnCreatedDetails.copy(paymentDueDate = Some(matchingPayments.head.dueDate)))
              } else {
                logger.warn("Could not find a unique payment with the matching charge reference")
                Left(ErrorCodes.errorHandlingDuplicateSubmission)
              }
          }
        } else {
          Future.successful(Right(returnCreatedDetails))
        }
    }
  }
}
