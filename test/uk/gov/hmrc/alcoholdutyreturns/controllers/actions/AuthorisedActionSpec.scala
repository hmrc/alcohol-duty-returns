/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.alcoholdutyreturns.controllers.actions

import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import play.api.mvc.{BodyParsers, Request, Result}
import play.api.test.Helpers._
import uk.gov.hmrc.alcoholdutyreturns.base.SpecBase
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core.CredentialStrength.strong
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{authorisedEnrolments, internalId => retriveInternalId}

import scala.concurrent.Future

class AuthorisedActionSpec extends SpecBase {
  val enrolment               = "HMRC-AD-ORG"
  val appaIdKey               = "APPAID"
  val state                   = "Activated"
  val enrolments              = Enrolments(Set(Enrolment(enrolment, Seq(EnrolmentIdentifier(appaIdKey, appaId)), state)))
  val emptyEnrolments         = Enrolments(Set.empty)
  val enrolmentsWithoutAppaId = Enrolments(Set(Enrolment(enrolment, Seq.empty, state)))
  val testContent             = "Test"

  val defaultBodyParser: BodyParsers.Default = app.injector.instanceOf[BodyParsers.Default]
  val mockAuthConnector: AuthConnector       = mock[AuthConnector]

  val authorisedAction =
    new BaseAuthorisedAction(mockAuthConnector, appConfig, defaultBodyParser)

  val testAction: Request[_] => Future[Result] = { _ =>
    Future(Ok(testContent))
  }

  "invokeBlock must" - {
    "execute the block and return OK if authorised" in {
      when(
        mockAuthConnector.authorise(
          eqTo(
            AuthProviders(GovernmentGateway)
              and Enrolment(enrolment)
              and CredentialStrength(strong)
              and Organisation
              and ConfidenceLevel.L50
          ),
          eqTo(
            retriveInternalId and authorisedEnrolments
          )
        )(any(), any())
      )
        .thenReturn(Future(new ~(Some(internalId), enrolments)))

      val result: Future[Result] = authorisedAction.invokeBlock(fakeRequest, testAction)

      status(result)          mustBe OK
      contentAsString(result) mustBe testContent
    }

    "execute the block and throw IllegalStateException if cannot get the enrolment" in {
      when(
        mockAuthConnector.authorise(
          eqTo(
            AuthProviders(GovernmentGateway)
              and Enrolment(enrolment)
              and CredentialStrength(strong)
              and Organisation
              and ConfidenceLevel.L50
          ),
          eqTo(
            retriveInternalId and authorisedEnrolments
          )
        )(any(), any())
      )
        .thenReturn(Future(new ~(Some(internalId), emptyEnrolments)))

      intercept[IllegalStateException] {
        await(authorisedAction.invokeBlock(fakeRequest, testAction))
      }
    }

    "execute the block and throw IllegalStateException if cannot get the APPAID enrolment" in {
      when(
        mockAuthConnector.authorise(
          eqTo(
            AuthProviders(GovernmentGateway)
              and Enrolment(enrolment)
              and CredentialStrength(strong)
              and Organisation
              and ConfidenceLevel.L50
          ),
          eqTo(
            retriveInternalId and authorisedEnrolments
          )
        )(any(), any())
      )
        .thenReturn(Future(new ~(Some(internalId), enrolmentsWithoutAppaId)))

      intercept[IllegalStateException] {
        await(authorisedAction.invokeBlock(fakeRequest, testAction))
      }
    }

    "thrown IllegalStateException if the authConnector returns None as Internal Id" in {
      when(
        mockAuthConnector.authorise(
          eqTo(
            AuthProviders(GovernmentGateway)
              and Enrolment(enrolment)
              and CredentialStrength(strong)
              and Organisation
              and ConfidenceLevel.L50
          ),
          eqTo(
            retriveInternalId and authorisedEnrolments
          )
        )(any(), any())
      )
        .thenReturn(Future(new ~(None, enrolments)))

      intercept[IllegalStateException] {
        await(authorisedAction.invokeBlock(fakeRequest, testAction))
      }
    }
  }

  "return 401 unauthorized if there is an authorisation exception" in {
    List(
      InsufficientConfidenceLevel(),
      InsufficientEnrolments(),
      UnsupportedAffinityGroup(),
      UnsupportedCredentialRole(),
      UnsupportedAuthProvider(),
      IncorrectCredentialStrength(),
      InternalError(),
      BearerTokenExpired(),
      MissingBearerToken(),
      InvalidBearerToken(),
      SessionRecordNotFound()
    ).foreach { exception =>
      when(mockAuthConnector.authorise[Unit](any(), any())(any(), any())).thenReturn(Future.failed(exception))

      val result: Future[Result] = authorisedAction.invokeBlock(fakeRequest, testAction)

      status(result) mustBe UNAUTHORIZED
    }
  }

  "return the exception if there is any other exception" in {
    val msg = "Test Exception"

    when(mockAuthConnector.authorise[Unit](any(), any())(any(), any()))
      .thenReturn(Future.failed(new RuntimeException(msg)))

    val result = intercept[RuntimeException] {
      await(authorisedAction.invokeBlock(fakeRequest, testAction))
    }

    result.getMessage mustBe msg
  }
}
