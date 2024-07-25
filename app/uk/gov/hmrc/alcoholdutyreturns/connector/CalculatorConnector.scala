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
import play.api.http.Status.BAD_REQUEST
import play.api.libs.json.{Reads, Writes}
import uk.gov.hmrc.alcoholdutyreturns.config.AppConfig
import uk.gov.hmrc.alcoholdutyreturns.models.calculation.{CalculateDutyDueByTaxTypeRequest, CalculatedDutyDueByTaxType}
import uk.gov.hmrc.alcoholdutyreturns.models.ErrorResponse
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReadsInstances, HttpResponse, UpstreamErrorResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class CalculatorConnector @Inject() (
  config: AppConfig,
  implicit val httpClient: HttpClient
)(implicit ec: ExecutionContext)
    extends HttpReadsInstances
    with Logging {

  private[connector] def performCalculation[I, O](
    url: String,
    requestBody: I
  )(implicit hc: HeaderCarrier, writes: Writes[I], reads: Reads[O]): EitherT[Future, ErrorResponse, O] =
    EitherT(
      httpClient
        .POST[I, Either[UpstreamErrorResponse, HttpResponse]](
          url = url,
          body = requestBody
        )
        .map {
          case Right(response)                                                =>
            Try(response.json.as[O]).toOption
              .fold[Either[ErrorResponse, O]](Left(ErrorResponse.InvalidJson))(Right(_))
          case Left(errorResponse) if errorResponse.statusCode == BAD_REQUEST => Left(ErrorResponse.BadRequest)
          case Left(errorResponse)                                            =>
            logger.warn(
              s"Received unexpected response from calculator API: ${errorResponse.statusCode} ${errorResponse.message}"
            )
            Left(ErrorResponse.UnexpectedResponse)
        }
    )

  def calculateDutyDueByTaxType(declarationsByTaxTypeRequest: CalculateDutyDueByTaxTypeRequest)(implicit
    hc: HeaderCarrier
  ): EitherT[Future, ErrorResponse, CalculatedDutyDueByTaxType] =
    performCalculation(config.getCalculateDutyDueByTaxTypeUrl, declarationsByTaxTypeRequest)
}
