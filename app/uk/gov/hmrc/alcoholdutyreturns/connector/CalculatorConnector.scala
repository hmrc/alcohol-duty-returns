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
import play.api.libs.json.{Json, Reads, Writes}
import uk.gov.hmrc.alcoholdutyreturns.config.AppConfig
import uk.gov.hmrc.alcoholdutyreturns.models.calculation.{CalculateDutyDueByTaxTypeRequest, CalculatedDutyDueByTaxType}
import uk.gov.hmrc.alcoholdutyreturns.models.ErrorCodes
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReadsInstances, HttpResponse, StringContextOps, UpstreamErrorResponse}
import uk.gov.hmrc.play.bootstrap.backend.http.ErrorResponse

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class CalculatorConnector @Inject() (
  config: AppConfig,
  implicit val httpClient: HttpClientV2
)(implicit ec: ExecutionContext)
    extends HttpReadsInstances
    with Logging {

  private[connector] def performCalculation[I, O](
    url: String,
    requestBody: I
  )(implicit hc: HeaderCarrier, writes: Writes[I], reads: Reads[O]): EitherT[Future, ErrorResponse, O] =
    EitherT(
      httpClient
        .post(url"$url")
        .withBody(Json.toJson(requestBody))
        .execute[Either[UpstreamErrorResponse, HttpResponse]]
        .map {
          case Right(response)                                                =>
            Try(response.json.as[O]) match {
              case Success(result) =>
                Right(result)
              case Failure(e)      =>
                logger.warn(s"Parsing failed for calculation result", e)
                Left(ErrorCodes.invalidJson)
            }
          case Left(errorResponse) if errorResponse.statusCode == BAD_REQUEST =>
            logger.warn(
              s"Received bad request from calculator API: ${errorResponse.message}"
            )
            Left(ErrorCodes.badRequest)
          case Left(errorResponse)                                            =>
            logger.warn(
              s"Received unexpected response from calculator API: ${errorResponse.statusCode} ${errorResponse.message}"
            )
            Left(ErrorCodes.unexpectedResponse)
        }
    )

  def calculateDutyDueByTaxType(declarationsByTaxTypeRequest: CalculateDutyDueByTaxTypeRequest)(implicit
    hc: HeaderCarrier
  ): EitherT[Future, ErrorResponse, CalculatedDutyDueByTaxType] = {
    logger.info("Requesting calculation of duty due by tax type")
    performCalculation[CalculateDutyDueByTaxTypeRequest, CalculatedDutyDueByTaxType](
      config.getCalculateDutyDueByTaxTypeUrl,
      declarationsByTaxTypeRequest
    ).biSemiflatTap(
      _ => Future.successful(logger.warn("Calculation of duty due by tax type failed")),
      _ => Future.successful(logger.info("Calculation of duty due by tax type succeeded"))
    )
  }
}
