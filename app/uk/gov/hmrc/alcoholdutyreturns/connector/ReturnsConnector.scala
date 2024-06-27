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
import play.api.http.Status.{OK, UNPROCESSABLE_ENTITY}
import uk.gov.hmrc.alcoholdutyreturns.config.AppConfig
import uk.gov.hmrc.alcoholdutyreturns.models.{ErrorResponse, ReturnId}
import uk.gov.hmrc.alcoholdutyreturns.models.returns.ReturnDetailsSuccess
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReadsInstances, HttpResponse, UpstreamErrorResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class ReturnsConnector @Inject() (
  config: AppConfig,
  implicit val httpClient: HttpClient
)(implicit ec: ExecutionContext)
    extends HttpReadsInstances {

  def getReturn(returnId: ReturnId)(implicit
    hc: HeaderCarrier
  ): EitherT[Future, ErrorResponse, ReturnDetailsSuccess] = EitherT(
    httpClient
      .GET[Either[UpstreamErrorResponse, HttpResponse]](
        url = config.getReturnsUrl(returnId)
      )
      .map {
        case Right(response) if response.status == OK                                =>
          Try(response.json.as[ReturnDetailsSuccess]).toOption
            .fold[Either[ErrorResponse, ReturnDetailsSuccess]](Left(ErrorResponse.InvalidJson))(Right(_))
        case Left(errorResponse) if errorResponse.statusCode == UNPROCESSABLE_ENTITY =>
          Left(ErrorResponse.EntityNotFound)
        case _                                                                       => Left(ErrorResponse.UnexpectedResponse)
      }
  )
}
