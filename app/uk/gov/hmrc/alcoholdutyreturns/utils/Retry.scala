/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.alcoholdutyreturns.utils

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.pattern.after
import play.api.Logging
import uk.gov.hmrc.alcoholdutyreturns.config.AppConfig
import uk.gov.hmrc.http.{HttpException, UpstreamErrorResponse}

import javax.inject.Inject
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

class Retry @Inject() (val times: Int, val initialWaitInMS: Int, val system: ActorSystem, appConfig: AppConfig)
    extends Logging {

  lazy val waitFactor: Float = appConfig.waitFactor

  private def apply[A](triesRemaining: Int, currentWait: Int)(
    f: Int => Future[A]
  )(implicit ec: ExecutionContext): Future[A] =
    f(triesRemaining).recoverWith {
      case ShouldRetryAfter(e) if triesRemaining > 0 =>
        logger.info(s"[Retry][apply] Retrying after failure ${e.getMessage}")
        val wait = Math.ceil(currentWait * waitFactor).toInt
        after(wait.milliseconds, system.scheduler)(apply(triesRemaining - 1, wait)(f))
      case ShouldRetryAfter(e)                       =>
        logger.warn(s"No retries remaining, error: ${e.getMessage}")
        Future.failed(e)
    }

  def apply[A](f: Int => Future[A])(implicit ec: ExecutionContext): Future[A] =
    apply(times, initialWaitInMS)(f)

  private object ShouldRetryAfter {
    def unapply(e: Exception): Option[Exception] = e match {
      case ex: HttpException if ex.responseCode >= 500 && ex.responseCode < 600     => Some(ex)
      case ex: UpstreamErrorResponse if ex.statusCode >= 500 && ex.statusCode < 600 => Some(ex)
      case _                                                                        => None
    }
  }
}
