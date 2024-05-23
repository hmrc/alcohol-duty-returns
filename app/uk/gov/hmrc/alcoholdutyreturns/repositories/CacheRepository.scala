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

package uk.gov.hmrc.alcoholdutyreturns.repositories

import org.mongodb.scala.model._
import play.api.libs.json.Format
import uk.gov.hmrc.alcoholdutyreturns.config.AppConfig
import uk.gov.hmrc.alcoholdutyreturns.models.{ReturnId, UserAnswers}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.{Clock, Instant}
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

sealed trait UpdateResult
case object UpdateSuccess extends UpdateResult
case object UpdateFailure extends UpdateResult

@Singleton
class CacheRepository @Inject() (
  mongoComponent: MongoComponent,
  appConfig: AppConfig,
  clock: Clock
)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[UserAnswers](
      collectionName = "user-answers",
      mongoComponent = mongoComponent,
      domainFormat = UserAnswers.format,
      indexes = Seq(
        IndexModel(
          Indexes.ascending("lastUpdated"),
          IndexOptions()
            .name("lastUpdatedIdx")
            .expireAfter(appConfig.dbTimeToLiveInSeconds, TimeUnit.SECONDS)
        )
      ),
      replaceIndexes = true,
      extraCodecs = Seq(Codecs.playFormatCodec(ReturnId.format))
    ) {

  implicit val instantFormat: Format[Instant] = MongoJavatimeFormats.instantFormat

  private def byId(id: ReturnId) = Filters.equal("_id", id)

  def keepAlive(id: ReturnId): Future[Boolean] =
    collection
      .updateOne(
        filter = byId(id),
        update = Updates.combine(
          Updates.set("lastUpdated", Instant.now(clock)),
          Updates.set("validUntil", Instant.now(clock).plusSeconds(appConfig.dbTimeToLiveInSeconds))
        )
      )
      .toFuture()
      .map(_ => true)

  def get(id: ReturnId): Future[Option[UserAnswers]] =
    keepAlive(id).flatMap { _ =>
      collection
        .find(byId(id))
        .headOption()
    }

  def set(answers: UserAnswers): Future[UpdateResult] = {

    val updatedAnswers = answers.copy(
      lastUpdated = Instant.now(clock),
      validUntil = Some(Instant.now(clock).plusSeconds(appConfig.dbTimeToLiveInSeconds))
    )

    collection
      .replaceOne(
        filter = byId(updatedAnswers.id),
        replacement = updatedAnswers,
        options = ReplaceOptions().upsert(false)
      )
      .toFuture()
      .map(res => if (res.getModifiedCount == 1) UpdateSuccess else UpdateFailure)
  }

  def add(answers: UserAnswers): Future[UserAnswers] = {

    val updatedAnswers = answers.copy(
      lastUpdated = Instant.now(clock),
      validUntil = Some(Instant.now(clock).plusSeconds(appConfig.dbTimeToLiveInSeconds))
    )

    collection
      .insertOne(updatedAnswers)
      .toFuture()
      .map(_ => updatedAnswers)
  }
}
