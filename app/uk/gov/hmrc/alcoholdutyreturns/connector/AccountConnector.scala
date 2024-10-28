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
import play.api.http.Status.NOT_FOUND
import play.api.libs.json.Reads
import uk.gov.hmrc.alcoholdutyreturns.config.AppConfig
import uk.gov.hmrc.alcoholdutyreturns.models.{ErrorCodes, ObligationData, ReturnId, SubscriptionSummary}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReadsInstances, HttpResponse, StringContextOps, UpstreamErrorResponse}
import uk.gov.hmrc.play.bootstrap.backend.http.ErrorResponse

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class AccountConnector @Inject() (
  config: AppConfig,
  implicit val httpClient: HttpClientV2
)(implicit ec: ExecutionContext)
    extends HttpReadsInstances
    with Logging {

  private[connector] def getData[T](
    url: String
  )(implicit hc: HeaderCarrier, reads: Reads[T]): EitherT[Future, ErrorResponse, T] =
    EitherT(
      httpClient
        .get(url"$url")
        .execute[Either[UpstreamErrorResponse, HttpResponse]]
        .map {
          case Right(response)                                              =>
            Try(response.json.as[T]).toOption
              .fold[Either[ErrorResponse, T]](Left(ErrorCodes.invalidJson))(Right(_))
          case Left(errorResponse) if errorResponse.statusCode == NOT_FOUND => Left(ErrorCodes.entityNotFound)
          case Left(errorResponse)                                          =>
            logger.warn(
              s"Received unexpected response from accounts API: ${errorResponse.statusCode} ${errorResponse.message}"
            )
            Left(ErrorCodes.unexpectedResponse)
        }
    )

  def getSubscriptionSummary(appaId: String)(implicit
    hc: HeaderCarrier
  ): EitherT[Future, ErrorResponse, SubscriptionSummary] =
    getData(config.getSubscriptionSummaryUrl(appaId))

  def getOpenObligationData(returnId: ReturnId)(implicit
    hc: HeaderCarrier
  ): EitherT[Future, ErrorResponse, ObligationData] =
    getData(config.getOpenObligationDataUrl(returnId))

  def getObligationData(appaId: String)(implicit
    hc: HeaderCarrier
  ): EitherT[Future, ErrorResponse, Seq[ObligationData]] =
    getData(config.getObligationDataUrl(appaId))
}
