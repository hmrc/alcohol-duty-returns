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

package uk.gov.hmrc.alcoholdutyreturns.service

import com.google.inject.{ImplementedBy, Singleton}
import org.mongodb.scala.SingleObservableFuture
import play.api.Logging
import uk.gov.hmrc.alcoholdutyreturns.config.AppConfig
import uk.gov.hmrc.alcoholdutyreturns.models.ReturnId
import uk.gov.hmrc.alcoholdutyreturns.utils.ADRMongoLockRepository

import javax.inject.Inject
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.language.{implicitConversions, postfixOps}

@ImplementedBy(classOf[LockingServiceImpl])
trait LockingService {
  def withLock[T](returnId: ReturnId, ownerId: String)(body: () => Future[T]): Future[Option[T]]
  def withLockExecuteAndRelease[T](returnId: ReturnId, userId: String)(body: () => Future[T]): Future[Option[T]]
  def keepAlive(returnId: ReturnId, ownerId: String): Future[Boolean]
  def releaseLock(returnId: ReturnId, ownerId: String): Future[Unit]
  def releaseAllLocks(): Future[Unit]
}

@Singleton
class LockingServiceImpl @Inject() (
  config: AppConfig,
  mongoLockRepository: ADRMongoLockRepository
)(implicit ec: ExecutionContext)
    extends LockingService
    with Logging {

  private val ttl = config.lockingDurationInSeconds seconds

  def withLock[T](returnId: ReturnId, ownerId: String)(body: () => Future[T]): Future[Option[T]] =
    (for {
      refreshed <- mongoLockRepository.refreshExpiry(returnId, ownerId, ttl)
      acquired  <- if (!refreshed) {
                     mongoLockRepository.takeLock(returnId, ownerId, ttl).map(_.isDefined)
                   } else {
                     Future.successful(false)
                   }
      result    <- if (refreshed || acquired) {
                     body().map(Some(_))
                   } else {
                     logger.info(s"[LockingService] [withLock] The return $returnId is locked")
                     Future.successful(None)
                   }
    } yield result).recoverWith { case ex =>
      logger.warn("[LockingService] [withLock] Exception thrown in locking service", ex)
      Future.failed(ex)
    }

  def withLockExecuteAndRelease[T](returnId: ReturnId, userId: String)(body: () => Future[T]): Future[Option[T]] =
    withLock(returnId, userId)(body).flatMap {
      case Some(value) =>
        mongoLockRepository
          .releaseLock(returnId, userId)
          .map(_ => Some(value))
      case None        => Future.successful(None)
    }

  def keepAlive(returnId: ReturnId, ownerId: String): Future[Boolean] =
    withLock(returnId, ownerId)(() => Future.successful(())).map(_.isDefined)

  def releaseLock(returnId: ReturnId, ownerId: String): Future[Unit] =
    mongoLockRepository.releaseLock(returnId, ownerId)

  def releaseAllLocks(): Future[Unit] =
    mongoLockRepository.collection.drop().toFuture()

  implicit def returnIdToLockId(returnId: ReturnId): String = s"${returnId.appaId}/${returnId.periodKey}"

}
