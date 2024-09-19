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
import uk.gov.hmrc.alcoholdutyreturns.models.ReturnId

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FakeLockingService extends LockingService {

  override def withLock[T](returnId: ReturnId, ownerId: String)(body: => Future[T]): Future[Option[T]] =
    body.map(Some(_))

  override def withLockExecuteAndRelease[T](returnId: ReturnId, userId: String)(body: => Future[T]): Future[Option[T]] =
    body.map(Some(_))

  override def keepAlive(returnId: ReturnId, ownerId: String): Future[Boolean] = Future.successful(true)

  override def releaseLock(returnId: ReturnId, ownerId: String): Future[Unit] = Future.successful(())

  override def releaseAllLocks(): Future[Unit] = Future.successful(())

  override def releaseLock(returnId: ReturnId): Future[Unit] = Future.successful(())
}
