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

import org.mockito.ArgumentMatchers.any
import org.mongodb.scala.{MongoCollection, SingleObservable}
import uk.gov.hmrc.alcoholdutyreturns.base.SpecBase
import uk.gov.hmrc.alcoholdutyreturns.models.ReturnId
import uk.gov.hmrc.mongo.lock.{Lock, MongoLockRepository}

import scala.concurrent.Future

class LockingServiceSpec extends SpecBase {
  "LockingService when calling" - {
    "withLock must" - {
      "execute the block of code if the lock is available" in new SetUp {
        when(mongoLockRepository.refreshExpiry(any(), any(), any())).thenReturn(Future.successful(false))
        when(mongoLockRepository.takeLock(any(), any(), any())).thenReturn(Future.successful(Some(mock[Lock])))

        whenReady(lockingService.withLock(returnId, internalId)(testData)) { result =>
          result mustBe successResponse
        }
      }

      "execute the block of code if user owns the lock and is refreshed" in new SetUp {
        when(mongoLockRepository.refreshExpiry(any(), any(), any())).thenReturn(Future.successful(true))

        whenReady(lockingService.withLock(returnId, internalId)(testData)) { result =>
          result mustBe successResponse
        }
      }

      "return None if the lock is owned by another user" in new SetUp {
        when(mongoLockRepository.refreshExpiry(any(), any(), any())).thenReturn(Future.successful(false))
        when(mongoLockRepository.takeLock(any(), any(), any())).thenReturn(Future.successful(None))

        whenReady(lockingService.withLock(returnId, internalId)(testData)) { result =>
          result mustBe None
        }
      }

      "return a Future failure if the lock repository return an error" in new SetUp {
        val exceptionMessage = "Exception"

        when(mongoLockRepository.refreshExpiry(any(), any(), any())).thenReturn(Future.successful(false))
        when(mongoLockRepository.takeLock(any(), any(), any())).thenThrow(new Exception(exceptionMessage))

        val result: Throwable = lockingService.withLock(returnId, internalId)(testData).failed.futureValue
        result            mustBe an[Exception]
        result.getMessage mustBe exceptionMessage
      }
    }

    "withLockAndRelease must" - {
      "execute the block of code if the lock is available and then release the lock" in new SetUp {
        when(mongoLockRepository.refreshExpiry(any(), any(), any())).thenReturn(Future.successful(false))
        when(mongoLockRepository.takeLock(any(), any(), any())).thenReturn(Future.successful(Some(mock[Lock])))
        when(mongoLockRepository.releaseLock(any(), any())).thenReturn(Future.successful(()))

        whenReady(lockingService.withLockExecuteAndRelease(returnId, internalId)(testData)) { result =>
          result mustBe successResponse
        }

        verify(mongoLockRepository).releaseLock(any(), any())
      }

      "execute the block of code if user owns the lock and is refreshed and then release the lock" in new SetUp {
        when(mongoLockRepository.refreshExpiry(any(), any(), any())).thenReturn(Future.successful(true))
        when(mongoLockRepository.releaseLock(any(), any())).thenReturn(Future.successful(()))

        whenReady(lockingService.withLockExecuteAndRelease(returnId, internalId)(testData)) { result =>
          result mustBe successResponse
        }

        verify(mongoLockRepository).releaseLock(any(), any())
      }

      "return None if the lock is owned by another user and not release the lock" in new SetUp {
        when(mongoLockRepository.refreshExpiry(any(), any(), any())).thenReturn(Future.successful(false))
        when(mongoLockRepository.takeLock(any(), any(), any())).thenReturn(Future.successful(None))

        whenReady(lockingService.withLockExecuteAndRelease(returnId, internalId)(testData)) { result =>
          result mustBe None
        }

        verify(mongoLockRepository, times(0)).releaseLock(any(), any())
      }

      "return a Future failure if the lock repository return an error and not release the lock" in new SetUp {
        val exceptionMessage = "Exception"

        when(mongoLockRepository.refreshExpiry(any(), any(), any())).thenReturn(Future.successful(false))
        when(mongoLockRepository.takeLock(any(), any(), any()))
          .thenReturn(Future.failed(new Exception(exceptionMessage)))

        val result: Throwable =
          lockingService.withLockExecuteAndRelease(returnId, internalId)(testData).failed.futureValue
        result            mustBe an[Exception]
        result.getMessage mustBe exceptionMessage

        verify(mongoLockRepository, times(0)).releaseLock(any(), any())
      }
    }

    "keepAlive must" - {
      "return true if the lock has been refreshed" in new SetUp {
        when(mongoLockRepository.refreshExpiry(any(), any(), any())).thenReturn(Future.successful(true))
        whenReady(lockingService.keepAlive(returnId = returnId, ownerId = internalId)) { result =>
          result mustBe true
        }
      }

      "return true if the clock hasn't been refreshed but it was possible to take a new Lock" in new SetUp {
        when(mongoLockRepository.refreshExpiry(any(), any(), any())).thenReturn(Future.successful(false))
        when(mongoLockRepository.takeLock(any(), any(), any())).thenReturn(Future.successful(Some(mock[Lock])))

        whenReady(lockingService.keepAlive(returnId = returnId, ownerId = internalId)) { result =>
          result mustBe true
        }
      }

      "return false if the clock hasn't been refreshed" in new SetUp {
        when(mongoLockRepository.refreshExpiry(any(), any(), any())).thenReturn(Future.successful(false))
        when(mongoLockRepository.takeLock(any(), any(), any())).thenReturn(Future.successful(None))

        whenReady(lockingService.keepAlive(returnId = returnId, ownerId = internalId)) { result =>
          result mustBe false
        }
      }

      "return a failed future if the lock repository returns an exception" in new SetUp {
        val exceptionMessage = "Exception!"
        when(mongoLockRepository.refreshExpiry(any(), any(), any()))
          .thenReturn(Future.failed(new Exception(exceptionMessage)))

        val result: Throwable = lockingService.keepAlive(returnId, internalId).failed.futureValue
        result            mustBe an[Exception]
        result.getMessage mustBe exceptionMessage
      }
    }

    "releaseLock must" - {
      "return a successful future, if the lock repository returns a successful future" in new SetUp {
        when(mongoLockRepository.releaseLock(any(), any())).thenReturn(Future.successful(()))
        whenReady(lockingService.releaseLock(returnId = returnId, ownerId = internalId)) { result =>
          result mustBe ()
        }
      }

      "return a failed future if the lock repository returns an exception" in new SetUp {
        val exceptionMessage = "Exception!"
        when(mongoLockRepository.releaseLock(any(), any())).thenReturn(Future.failed(new Exception(exceptionMessage)))

        val result: Throwable = lockingService.releaseLock(returnId, internalId).failed.futureValue
        result            mustBe an[Exception]
        result.getMessage mustBe exceptionMessage
      }
    }

    "releaseAllLocks must" - {
      "drop the lock collection in the mongo lock repository" in new SetUp {
        val mongoCollection = mock[MongoCollection[Lock]]
        when(mongoLockRepository.collection).thenReturn(mongoCollection)

        val singleObservableUnit = mock[SingleObservable[Unit]]
        when(singleObservableUnit.toFuture()).thenReturn(Future.successful(()))

        when(mongoCollection.drop()).thenReturn(singleObservableUnit)

        whenReady(lockingService.releaseAllLocks()) { result =>
          result mustBe ()
        }
      }
    }
  }

  class SetUp {
    val returnId: ReturnId = ReturnId(appaId, periodKey)

    val mongoLockRepository: MongoLockRepository = mock[MongoLockRepository]
    val lockingService                           = new LockingServiceImpl(appConfig, mongoLockRepository)

    val testData: Future[Boolean]        = Future.successful(true)
    val successResponse: Option[Boolean] = Some(true)

  }
}
