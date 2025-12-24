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

package uk.gov.hmrc.alcoholdutyreturns.connector

import cats.data.EitherT
import org.apache.pekko.actor.{ActorSystem, Scheduler}
import org.apache.pekko.pattern.retry
import play.api.Logging
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.alcoholdutyreturns.config.{AppConfig, CircuitBreakerProvider}
import uk.gov.hmrc.alcoholdutyreturns.connector.helpers.HIPHeaders
import uk.gov.hmrc.alcoholdutyreturns.models.returns._
import uk.gov.hmrc.alcoholdutyreturns.models.{DuplicateSubmissionError, ErrorCodes, ReturnId}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReadsInstances, HttpResponse, InternalServerException, StringContextOps}
import uk.gov.hmrc.play.bootstrap.http.ErrorResponse

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class ReturnsConnector @Inject() (
  config: AppConfig,
  headers: HIPHeaders,
  circuitBreakerProvider: CircuitBreakerProvider,
  implicit val system: ActorSystem,
  implicit val httpClient: HttpClientV2
)(implicit ec: ExecutionContext)
    extends HttpReadsInstances
    with Logging {

  implicit val scheduler: Scheduler = system.scheduler

  def getReturn(returnId: ReturnId)(implicit
    hc: HeaderCarrier
  ): Future[Either[ErrorResponse, GetReturnDetails]] =
    retry(
      () => fetchCall(returnId),
      attempts = config.retryAttempts,
      delay = config.retryAttemptsDelay
    ).recoverWith { error =>
      logger.error(
        s"[ReturnsConnector] [getReturn] An exception was returned while trying to get return for (appaId ${returnId.appaId}, periodKey ${returnId.periodKey}): $error"
      )
      Future.successful(Left(ErrorResponse(INTERNAL_SERVER_ERROR, ErrorCodes.unexpectedResponse.message)))
    }

  def submitReturn(returnToSubmit: ReturnCreate, appaId: String)(implicit
    hc: HeaderCarrier
  ): EitherT[Future, ErrorResponse, ReturnCreatedDetails] =
    EitherT(
      retry(
        () => submitCall(returnToSubmit, appaId),
        attempts = config.retryAttemptsPost,
        delay = config.retryAttemptsDelay
      ).recoverWith { error =>
        logger.error(
          s"[ReturnsConnector] [submitReturn] Received unexpected response from submitReturn API (appaId $appaId, periodKey $returnToSubmit.periodKey). Error: ${error.getMessage}"
        )
        Future.successful(Left(ErrorResponse(INTERNAL_SERVER_ERROR, ErrorCodes.unexpectedResponse.message)))
      }
    )

  private def fetchCall(returnId: ReturnId)(implicit
    hc: HeaderCarrier
  ): Future[Either[ErrorResponse, GetReturnDetails]] =
    circuitBreakerProvider.get().withCircuitBreaker {
      logger.info(
        s"[ReturnsConnector] [getReturn] Getting return (appaId ${returnId.appaId}, periodKey ${returnId.periodKey})"
      )
      httpClient
        .get(url"${config.getReturnUrl(returnId)}")
        .setHeader(headers.getReturnsHeaders: _*)
        .execute[HttpResponse]
        .flatMap { response =>
          response.status match {
            case OK                   =>
              Try {
                response.json
                  .as[GetReturnDetailsSuccess]
              } match {
                case Success(returnDetailsSuccess) =>
                  logger
                    .info(
                      s"[ReturnsConnector] [getReturn] Return obtained successfully (appaId ${returnId.appaId}, periodKey ${returnId.periodKey})"
                    )
                  Future.successful(Right(returnDetailsSuccess.success))
                case Failure(e)                    =>
                  logger.error(
                    s"[ReturnsConnector] [getReturn] Parsing failed for return (appaId ${returnId.appaId}, periodKey ${returnId.periodKey})",
                    e
                  )
                  Future.successful(Left(ErrorCodes.invalidJson))
              }
            case BAD_REQUEST          =>
              logger.warn(
                s"[ReturnsConnector] [getReturn] Bad request returned for get return (appaId ${returnId.appaId}, periodKey ${returnId.periodKey})"
              )
              Future.successful(Left(ErrorCodes.badRequest))
            case NOT_FOUND            =>
              logger.warn(
                s"[ReturnsConnector] [getReturn] Return not found (appaId ${returnId.appaId}, periodKey ${returnId.periodKey})"
              )
              Future.successful(Left(ErrorCodes.entityNotFound))
            case UNPROCESSABLE_ENTITY =>
              logger.warn(
                s"[ReturnsConnector] [getReturn] Get return unprocessable for (appaId ${returnId.appaId}, periodKey ${returnId.periodKey}): ${response.body}"
              )
              Future.successful(Left(ErrorCodes.unexpectedResponse))
            // Retry and log on final fail for the following transient errors
            case BAD_GATEWAY          =>
              Future.failed(new InternalServerException("Bad gateway"))
            case SERVICE_UNAVAILABLE  =>
              Future.failed(new InternalServerException("Service unavailable"))
            case GATEWAY_TIMEOUT      =>
              Future.failed(new InternalServerException("Gateway timeout"))
            case _                    =>
              Future.failed(new InternalServerException(response.body))
          }
        }
    }

  private def submitCall(returnToSubmit: ReturnCreate, appaId: String)(implicit
    hc: HeaderCarrier
  ): Future[Either[ErrorResponse, ReturnCreatedDetails]] =
    circuitBreakerProvider.get().withCircuitBreaker {
      val periodKey = returnToSubmit.periodKey
      logger.info(s"[ReturnsConnector] [submitReturn] Submitting return (appaId $appaId, periodKey $periodKey)")
      httpClient
        .post(url"${config.submitReturnUrl}")
        .setHeader(headers.submitReturnHeaders(appaId): _*)
        .withBody(Json.toJson(returnToSubmit))
        .execute[HttpResponse]
        .flatMap {
          case response if response.status == CREATED              =>
            Try(response.json.as[ReturnCreatedSuccess]) match {
              case Success(returnCreatedSuccess) =>
                logger.info(
                  s"[ReturnsConnector] [submitReturn] Return submitted successfully (appaId $appaId, periodKey $periodKey)"
                )
                Future.successful(Right(returnCreatedSuccess.success))
              case Failure(e)                    =>
                logger
                  .error(
                    s"[ReturnsConnector] [submitReturn] Parsing failed for submit return response (appaId $appaId, periodKey $periodKey)",
                    e
                  )
                Future.successful(Left(ErrorCodes.unexpectedResponse))
            }
          case response if response.status == BAD_REQUEST          =>
            logger.warn(
              s"[ReturnsConnector] [submitReturn] Bad request returned for submit return (appaId $appaId, periodKey $periodKey): ${response.body}"
            )
            Future.successful(Left(ErrorCodes.badRequest))
          case response if response.status == NOT_FOUND            =>
            logger.warn(
              s"[ReturnsConnector] [submitReturn] Not found returned for submit return (appaId $appaId, periodKey $periodKey)"
            )
            Future.successful(Left(ErrorCodes.entityNotFound))
          case response if response.status == UNPROCESSABLE_ENTITY =>
            checkForDuplicateSubmission(response, appaId, periodKey)
          // Retry and log on final fail for the following transient errors
          case response if response.status == BAD_GATEWAY          =>
            Future.failed(new InternalServerException("Bad gateway"))
          case response if response.status == SERVICE_UNAVAILABLE  =>
            Future.failed(new InternalServerException("Service unavailable"))
          case response if response.status == GATEWAY_TIMEOUT      =>
            Future.failed(new InternalServerException("Gateway timeout"))
          case response                                            =>
            Future.failed(new InternalServerException(response.body))
        }
    }

  private def checkForDuplicateSubmission(
    response: HttpResponse,
    appaId: String,
    periodKey: String
  ): Future[Left[ErrorResponse, Nothing]] =
    Try(response.json.as[DuplicateSubmissionError]) match {
      case Success(dupError) if dupError.errors.code == "044" || dupError.errors.code == "999" =>
        logger.warn(
          s"[ReturnsConnector] [submitReturn] Return already submitted (appaId $appaId, periodKey $periodKey) - Error code: ${dupError.errors.code}"
        )
        Future.successful(Left(ErrorCodes.duplicateSubmission))
      case _                                                                                   =>
        logger.warn(
          s"[ReturnsConnector] [submitReturn] Unprocessable entity returned for submit return response (appaId $appaId, periodKey $periodKey): ${response.body}"
        )
        Future.successful(Left(ErrorCodes.unexpectedResponse))
    }
}
