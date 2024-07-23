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
import play.api.http.Status.{BAD_REQUEST, NOT_FOUND}
import uk.gov.hmrc.alcoholdutyreturns.config.AppConfig
import uk.gov.hmrc.alcoholdutyreturns.connector.helpers.HIPHeaders
import uk.gov.hmrc.alcoholdutyreturns.models.{ErrorResponse, ReturnId}
import uk.gov.hmrc.alcoholdutyreturns.models.returns.{GetReturnDetails, GetReturnDetailsSuccess, ReturnCreate, ReturnCreatedDetails, ReturnCreatedSuccess}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReadsInstances, HttpResponse, UpstreamErrorResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class ReturnsConnector @Inject() (
  config: AppConfig,
  headers: HIPHeaders,
  implicit val httpClient: HttpClient
)(implicit ec: ExecutionContext)
    extends HttpReadsInstances
    with Logging {

  def getReturn(returnId: ReturnId)(implicit
    hc: HeaderCarrier
  ): EitherT[Future, ErrorResponse, GetReturnDetails] = EitherT(
    httpClient
      .GET[Either[UpstreamErrorResponse, HttpResponse]](
        url = config.getReturnUrl(returnId),
        headers = headers.getReturnsHeaders
      )
      .map {
        case Right(response)                                                =>
          Try(response.json.as[GetReturnDetailsSuccess]).toOption
            .fold[Either[ErrorResponse, GetReturnDetails]](Left(ErrorResponse.InvalidJson))(returnDetailsSuccess =>
              Right(returnDetailsSuccess.success)
            )
        case Left(errorResponse) if errorResponse.statusCode == BAD_REQUEST =>
          Left(ErrorResponse.BadRequest)
        case Left(errorResponse) if errorResponse.statusCode == NOT_FOUND   =>
          Left(ErrorResponse.EntityNotFound)
        case Left(errorResponse)                                            =>
          logger.warn(
            s"Received unexpected response from returns API: ${errorResponse.statusCode} ${errorResponse.message}"
          )
          Left(ErrorResponse.UnexpectedResponse)
      }
  )

  def submitReturn(returnCreate: ReturnCreate)(implicit
    hc: HeaderCarrier
  ): EitherT[Future, ErrorResponse, ReturnCreatedDetails] = EitherT(
    httpClient
      .POST[ReturnCreate, Either[UpstreamErrorResponse, HttpResponse]](
        url = config.submitReturnUrl(),
        body = returnCreate,
        headers = headers.submitReturnHeaders
      )
      .map {
        case Right(response)                                                =>
          Try(response.json.as[ReturnCreatedSuccess]).toOption
            .fold[Either[ErrorResponse, ReturnCreatedDetails]](Left(ErrorResponse.InvalidJson))(returnCreatedSuccess =>
              Right(returnCreatedSuccess.success)
            )
        case Left(errorResponse) if errorResponse.statusCode == BAD_REQUEST =>
          Left(ErrorResponse.BadRequest)
        case Left(errorResponse) if errorResponse.statusCode == NOT_FOUND   =>
          Left(ErrorResponse.EntityNotFound)
        case Left(errorResponse)                                            =>
          logger.warn(
            s"Received unexpected response from submitReturn API: ${errorResponse.statusCode} ${errorResponse.message}"
          )
          Left(ErrorResponse.UnexpectedResponse)
      }
  )
}
