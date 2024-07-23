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

package uk.gov.hmrc.alcoholdutyreturns.base

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, getRequestedFor, postRequestedFor, urlEqualTo}
import com.github.tomakehurst.wiremock.matching.EqualToPattern

trait WireMockHelper {
  val wireMockServer: WireMockServer
  val wireMockHost: String
  val wireMockPort: Int

  protected val endpointConfigurationPath = "microservice.services"

  protected def getWireMockAppConfig(endpointNames: Seq[String]): Map[String, Any] =
    endpointNames
      .flatMap(endpointName =>
        Seq(
          s"$endpointConfigurationPath.$endpointName.host" -> wireMockHost,
          s"$endpointConfigurationPath.$endpointName.port" -> wireMockPort
        )
      )
      .toMap

  private def stripToPath(url: String) =
    if (url.startsWith("http://") || url.startsWith("https://"))
      url.dropWhile(_ != '/').dropWhile(_ == '/').dropWhile(_ != '/')
    else
      url

  private def urlWithParameters(url: String, parameters: Map[String, String]) = {
    val queryParams = parameters.map { case (k, v) => s"$k=$v" }.mkString("&")

    s"${stripToPath(url)}?$queryParams"
  }

  def stubGet(url: String, status: Int, body: String): Unit =
    wireMockServer.stubFor(
      WireMock.get(urlEqualTo(stripToPath(url))).willReturn(aResponse().withStatus(status).withBody(body))
    )

  def stubGetWithParameters(url: String, parameters: Map[String, String], status: Int, body: String): Unit =
    wireMockServer.stubFor(
      WireMock
        .get(urlEqualTo(urlWithParameters(url, parameters)))
        .willReturn(aResponse().withStatus(status).withBody(body))
    )

  def stubPost(url: String, status: Int, requestBody: String, returnBody: String): Unit =
    wireMockServer.stubFor(
      WireMock.post(urlEqualTo(stripToPath(url))).withRequestBody(new EqualToPattern(requestBody)).willReturn(aResponse().withStatus(status).withBody(returnBody))
    )

  def verifyGet(url: String): Unit =
    wireMockServer.verify(getRequestedFor(urlEqualTo(stripToPath(url))))

  def verifyGetWithParameters(url: String, parameters: Map[String, String]): Unit =
    wireMockServer.verify(getRequestedFor(urlEqualTo(urlWithParameters(url, parameters))))

  def verifyPost(url: String): Unit =
    wireMockServer.verify(postRequestedFor(urlEqualTo(stripToPath(url))))
}
