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

import generators.ModelGenerators
import helpers.TestData
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.mockito.MockitoSugar
import org.scalatest.{OptionValues, TryValues}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.{Application, Mode}
import play.api.http.{HeaderNames, Status, Writeable}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{Result, Results}
import play.api.test.Helpers.route
import play.api.test.{DefaultAwaitTimeout, FakeRequest, ResultExtractors, Writeables}
import uk.gov.hmrc.alcoholdutyreturns.config.AppConfig
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.test.{HttpClientSupport, WireMockSupport}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.global

trait ISpecBase extends AnyWordSpec
    with Matchers
    with TryValues
    with OptionValues
    with ScalaFutures
    with Results
    with DefaultAwaitTimeout
    with Writeables
    with ResultExtractors
    with Status
    with HeaderNames
    with GuiceOneAppPerSuite
    with MockitoSugar
    with ScalaCheckPropertyChecks
    with WireMockSupport
    with HttpClientSupport
    with AuthStubs
    with TestData
    with ModelGenerators {
  implicit lazy val system: ActorSystem = ActorSystem()
  implicit lazy val materializer: Materializer = Materializer(system)

  implicit def ec: ExecutionContext = global

  val additionalAppConfig: Map[String, Any] = Map(
    "metrics.enabled" -> false,
    "auditing.enabled" -> false
  ) ++ getWireMockAppConfig(Seq("auth", "alcohol-duty-accounts", "alcohol-duty-calculator", "returns"))

  override def fakeApplication(): Application =
    GuiceApplicationBuilder()
      .disable[com.codahale.metrics.MetricRegistry]
      .configure(additionalAppConfig)
      .in(Mode.Test)
      .build()

  lazy val config = new AppConfig(app.configuration, new ServicesConfig(app.configuration))

  /*
  This is to initialise the app before running any tests, as it is lazy by default in org.scalatestplus.play.BaseOneAppPerSuite.
  It enables us to include behaviour tests that call routes within the `should` part of a test but before `in`.
   */
  locally {
    val _ = app
  }

  def callRoute[A](fakeRequest: FakeRequest[A], requiresAuth: Boolean = true)(implicit
                                                                              app: Application,
                                                                              w: Writeable[A]
  ): Future[Result] = {
    val errorHandler = app.errorHandler

    val req = if (requiresAuth) fakeRequest.withHeaders("Authorization" -> "test") else fakeRequest

    route(app, req) match {
      case None => fail("Route does not exist")
      case Some(result) =>
        result.recoverWith { case t: Throwable =>
          errorHandler.onServerError(req, t)
        }
    }
  }

  implicit val hc: HeaderCarrier = HeaderCarrier()
}