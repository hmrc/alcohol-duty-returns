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
import play.api.Logging
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.alcoholdutyreturns.config.AppConfig
import uk.gov.hmrc.alcoholdutyreturns.connector.helpers.HIPHeaders
import uk.gov.hmrc.alcoholdutyreturns.models.{DuplicateSubmissionError, ErrorCodes, ReturnId}
import uk.gov.hmrc.alcoholdutyreturns.models.returns.{GetReturnDetails, GetReturnDetailsSuccess, ReturnCreate, ReturnCreatedDetails, ReturnCreatedSuccess}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReadsInstances, HttpResponse, StringContextOps, UpstreamErrorResponse}
import uk.gov.hmrc.play.bootstrap.backend.http.ErrorResponse

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class ReturnsConnector @Inject() (
  config: AppConfig,
  headers: HIPHeaders,
  implicit val httpClient: HttpClientV2
)(implicit ec: ExecutionContext)
    extends HttpReadsInstances
    with Logging {

  def getReturn(returnId: ReturnId)(implicit
    hc: HeaderCarrier
  ): EitherT[Future, ErrorResponse, GetReturnDetails] = {
    logger.info(s"Getting return (appaId ${returnId.appaId}, periodKey ${returnId.periodKey})")
    EitherT(
      httpClient
        .get(url"${config.getReturnUrl(returnId)}")
        .setHeader(headers.getReturnsHeaders: _*)
        .execute[Either[UpstreamErrorResponse, HttpResponse]]
        .map {
          case Right(response)                                                =>
            Try(response.json.as[GetReturnDetailsSuccess]) match {
              case Success(returnDetailsSuccess) =>
                logger
                  .info(s"Return obtained successfully (appaId ${returnId.appaId}, periodKey ${returnId.periodKey})")
                Right(returnDetailsSuccess.success)
              case Failure(e)                    =>
                logger.warn(
                  s"Parsing failed for return (appaId ${returnId.appaId}, periodKey ${returnId.periodKey})",
                  e
                )
                Left(ErrorCodes.invalidJson)
            }
          case Left(errorResponse) if errorResponse.statusCode == BAD_REQUEST =>
            logger.warn(
              s"Bad request returned for get return (appaId ${returnId.appaId}, periodKey ${returnId.periodKey}): ${errorResponse.message}"
            )
            Left(ErrorCodes.badRequest)
          case Left(errorResponse) if errorResponse.statusCode == NOT_FOUND   =>
            logger.warn(s"Return not found (appaId ${returnId.appaId}, periodKey ${returnId.periodKey})")
            Left(ErrorCodes.entityNotFound)
          case Left(errorResponse)                                            =>
            logger.warn(
              s"Received unexpected response from returns API (appaId ${returnId.appaId}, " +
                s"periodKey ${returnId.periodKey}): ${errorResponse.statusCode} ${errorResponse.message}"
            )
            Left(ErrorCodes.unexpectedResponse)
        }
    )
  }

  def submitReturn(returnToSubmit: ReturnCreate, appaId: String)(implicit
    hc: HeaderCarrier
  ): EitherT[Future, ErrorResponse, ReturnCreatedDetails] = {
    val periodKey = returnToSubmit.periodKey
    logger.info(s"Submitting return (appaId $appaId, periodKey $periodKey)")
    EitherT(
      httpClient
        .post(url"${config.submitReturnUrl}")
        .setHeader(headers.submitReturnHeaders(appaId): _*)
        .withBody(Json.toJson(returnToSubmit))
        .execute[HttpResponse]
        .map {
          case response if response.status == CREATED              =>
            Try(response.json.as[ReturnCreatedSuccess]) match {
              case Success(returnCreatedSuccess) =>
                logger.info(s"Return submitted successfully (appaId $appaId, periodKey $periodKey)")
                Right(returnCreatedSuccess.success)
              case Failure(e)                    =>
                logger
                  .warn(s"Parsing failed for submit return response (appaId $appaId, periodKey $periodKey)", e)
                Left(ErrorCodes.unexpectedResponse)
            }
          case response if response.status == BAD_REQUEST          =>
            logger.warn(
              s"Bad request returned for submit return (appaId $appaId, periodKey $periodKey): ${response.body}"
            )
            Left(ErrorCodes.badRequest)
          case response if response.status == NOT_FOUND            =>
            logger.warn(s"Not found returned for submit return (appaId $appaId, periodKey $periodKey)")
            Left(ErrorCodes.entityNotFound)
          case response if response.status == UNPROCESSABLE_ENTITY =>
            Try(response.json.as[DuplicateSubmissionError]) match {
              case Success(dupError) if dupError.errors.code == "044" || dupError.errors.code == "999" =>
                logger.info(s"Return already submitted (appaId $appaId, periodKey $periodKey)")
                Left(ErrorCodes.duplicateSubmission)
              case _                                                                                   =>
                logger.warn(
                  s"Unprocessable entity returned for submit return response (appaId $appaId, periodKey $periodKey): ${response.body}"
                )
                Left(ErrorCodes.unexpectedResponse)
            }
          case response                                            =>
            logger.warn(
              s"Received unexpected response from submitReturn API (appaId $appaId, periodKey $periodKey). Status: ${response.status}, Body: ${response.body}"
            )
            Left(ErrorCodes.unexpectedResponse)
        }
    )
  }
}
