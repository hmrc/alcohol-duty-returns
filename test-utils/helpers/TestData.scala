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

package helpers

import generators.ModelGenerators
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.alcoholdutyreturns.models.AlcoholRegime.{Beer, Cider, OtherFermentedProduct, Spirits, Wine}
import uk.gov.hmrc.alcoholdutyreturns.models.ApprovalStatus.Approved
import uk.gov.hmrc.alcoholdutyreturns.models.{AlcoholRegimes, ObligationData, ReturnAndUserDetails, ReturnId, SubscriptionSummary, UserAnswers}
import uk.gov.hmrc.alcoholdutyreturns.models.ObligationStatus.{Fulfilled, Open}

import java.time.{Clock, Instant, LocalDate}

trait TestData extends ModelGenerators {
  def clock: Clock       = Clock.systemDefaultZone()
  val appaId: String     = appaIdGen.sample.get
  val periodKey: String  = periodKeyGen.sample.get
  val groupId: String    = "groupId"
  val internalId: String = "internalId"
  val returnId: ReturnId = ReturnId(appaId, periodKey)

  val returnAndUserDetails: ReturnAndUserDetails = ReturnAndUserDetails(returnId, groupId, internalId)

  val alcoholRegimes: AlcoholRegimes    = AlcoholRegimes(Set(Beer, Wine))
  val allAlcoholRegimes: AlcoholRegimes = AlcoholRegimes(Set(Beer, Cider, Spirits, Wine, OtherFermentedProduct))

  val subscriptionSummary: SubscriptionSummary = SubscriptionSummary(Approved, allAlcoholRegimes.regimes)

  val emptyUserAnswers: UserAnswers = UserAnswers(
    returnId,
    groupId,
    internalId,
    allAlcoholRegimes,
    lastUpdated = Instant.now(clock)
  )

  val userAnswers: UserAnswers = UserAnswers(
    returnId,
    groupId,
    internalId,
    allAlcoholRegimes,
    JsObject(Seq(ObligationData.toString -> Json.toJson(getObligationData(LocalDate.now(clock))))),
    lastUpdated = Instant.now(clock)
  )

  def getObligationData(now: LocalDate): ObligationData = ObligationData(
    status = Open,
    fromDate = now,
    toDate = now.plusDays(1),
    dueDate = now.plusDays(2)
  )

  def getFulfilledObligationData(now: LocalDate): ObligationData = getObligationData(now).copy(status = Fulfilled)
}
