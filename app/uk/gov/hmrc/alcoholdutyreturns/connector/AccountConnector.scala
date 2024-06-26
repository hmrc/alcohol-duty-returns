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
import play.api.http.Status.{NOT_FOUND, OK}
import uk.gov.hmrc.alcoholdutyreturns.config.AppConfig
import uk.gov.hmrc.alcoholdutyreturns.models.ErrorResponse.{EntityNotFound, InvalidJson, UnexpectedResponse}
import uk.gov.hmrc.alcoholdutyreturns.models.{ErrorResponse, ObligationData, ReturnId, SubscriptionSummary}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReadsInstances, HttpResponse, UpstreamErrorResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class AccountConnector @Inject() (
  config: AppConfig,
  implicit val httpClient: HttpClient
)(implicit ec: ExecutionContext)
    extends HttpReadsInstances {

  def getSubscriptionSummary(appaId: String)(implicit
    hc: HeaderCarrier
  ): EitherT[Future, ErrorResponse, SubscriptionSummary] = EitherT(
    httpClient
      .GET[Either[UpstreamErrorResponse, HttpResponse]](
        url = config.getSubscriptionSummaryUrl(appaId)
      )
      .map {
        case Right(response) if response.status == OK                     =>
          Try(response.json.as[SubscriptionSummary]).toOption
            .fold[Either[ErrorResponse, SubscriptionSummary]](Left(InvalidJson))(Right(_))
        case Left(errorResponse) if errorResponse.statusCode == NOT_FOUND => Left(EntityNotFound)
        case _                                                            => Left(UnexpectedResponse)
      }
  )

  def getOpenObligationData(returnId: ReturnId)(implicit
    hc: HeaderCarrier
  ): EitherT[Future, ErrorResponse, ObligationData] = EitherT(
    httpClient
      .GET[Either[UpstreamErrorResponse, HttpResponse]](
        url = config.getOpenObligationDataUrl(returnId.appaId, returnId.periodKey)
      )
      .map {
        case Right(response) if response.status == OK                     =>
          Try(response.json.as[ObligationData]).toOption
            .fold[Either[ErrorResponse, ObligationData]](Left(InvalidJson))(Right(_))
        case Left(errorResponse) if errorResponse.statusCode == NOT_FOUND => Left(EntityNotFound)
        case _                                                            => Left(UnexpectedResponse)
      }
  )

  def getObligationData(appaId: String)(implicit
    hc: HeaderCarrier
  ): EitherT[Future, ErrorResponse, Seq[ObligationData]] = EitherT(
    httpClient
      .GET[Either[UpstreamErrorResponse, HttpResponse]](
        url = config.getObligationDataUrl(appaId)
      )
      .map {
        case Right(response) if response.status == OK                     =>
          Try(response.json.as[Seq[ObligationData]]).toOption
            .fold[Either[ErrorResponse, Seq[ObligationData]]](Left(InvalidJson))(Right(_))
        case Left(errorResponse) if errorResponse.statusCode == NOT_FOUND => Left(EntityNotFound)
        case _                                                            => Left(UnexpectedResponse)
      }
  )

}
