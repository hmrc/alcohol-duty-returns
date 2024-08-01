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

package uk.gov.hmrc.alcoholdutyreturns.models.calculation

import play.api.libs.json.{Json, OWrites}
import uk.gov.hmrc.alcoholdutyreturns.models.returns.{AdrAdjustmentItem, AdrDutyDeclaredItem, AdrRepackagedDraughtAdjustmentItem, AdrReturnSubmission}

case class CalculateDutyDueByTaxTypeRequestItem(
  taxType: String,
  dutyDue: BigDecimal
)

object CalculateDutyDueByTaxTypeRequestItem {
  implicit val calculateDutyDueByTaxTypeRequestItemWrites: OWrites[CalculateDutyDueByTaxTypeRequestItem] =
    Json.writes[CalculateDutyDueByTaxTypeRequestItem]

  def fromDutyDeclaredItem(dutyDeclaredItem: AdrDutyDeclaredItem): CalculateDutyDueByTaxTypeRequestItem =
    CalculateDutyDueByTaxTypeRequestItem(dutyDeclaredItem.dutyDue.taxCode, dutyDeclaredItem.dutyDue.dutyDue)

  def fromAdjustmentItem(adjustmentItem: AdrAdjustmentItem): CalculateDutyDueByTaxTypeRequestItem =
    CalculateDutyDueByTaxTypeRequestItem(adjustmentItem.dutyDue.taxCode, adjustmentItem.dutyDue.dutyDue)

  def fromRepackagedDraughtAdjustmentItem(
    repackagedDraughtAdjustmentItem: AdrRepackagedDraughtAdjustmentItem
  ): CalculateDutyDueByTaxTypeRequestItem =
    CalculateDutyDueByTaxTypeRequestItem(
      repackagedDraughtAdjustmentItem.newTaxCode,
      repackagedDraughtAdjustmentItem.dutyAdjustment
    )
}

case class CalculateDutyDueByTaxTypeRequest(
  declarationOrAdjustmentItems: Seq[CalculateDutyDueByTaxTypeRequestItem]
)

object CalculateDutyDueByTaxTypeRequest {
  implicit val calculateDutyDueByTaxTypeRequestWrites: OWrites[CalculateDutyDueByTaxTypeRequest] =
    Json.writes[CalculateDutyDueByTaxTypeRequest]

  private def fromDutyDeclaredItems(
    dutyDeclaredItems: Seq[AdrDutyDeclaredItem]
  ): Seq[CalculateDutyDueByTaxTypeRequestItem] =
    dutyDeclaredItems.map(CalculateDutyDueByTaxTypeRequestItem.fromDutyDeclaredItem)

  private def fromAdjustmentItems(adjustmentItems: Seq[AdrAdjustmentItem]): Seq[CalculateDutyDueByTaxTypeRequestItem] =
    adjustmentItems.map(CalculateDutyDueByTaxTypeRequestItem.fromAdjustmentItem)

  private def fromRepackagedDraughtAdjustmentItems(
    repackagedDraughtAdjustmentItems: Seq[AdrRepackagedDraughtAdjustmentItem]
  ): Seq[CalculateDutyDueByTaxTypeRequestItem] =
    repackagedDraughtAdjustmentItems.map(CalculateDutyDueByTaxTypeRequestItem.fromRepackagedDraughtAdjustmentItem)

  def fromReturnsSubmission(returnSubmission: AdrReturnSubmission): Option[CalculateDutyDueByTaxTypeRequest] =
    if (returnSubmission.dutyDeclared.dutyDeclaredItems.isEmpty && !returnSubmission.adjustments.hasAdjustments) {
      None
    } else {
      Some(
        CalculateDutyDueByTaxTypeRequest(
          fromDutyDeclaredItems(returnSubmission.dutyDeclared.dutyDeclaredItems) ++
            fromAdjustmentItems(returnSubmission.adjustments.overDeclarationProducts) ++
            fromAdjustmentItems(returnSubmission.adjustments.underDeclarationProducts) ++
            fromAdjustmentItems(returnSubmission.adjustments.spoiltProducts) ++
            fromAdjustmentItems(returnSubmission.adjustments.drawbackProducts) ++
            fromRepackagedDraughtAdjustmentItems(returnSubmission.adjustments.repackagedDraughtProducts)
        )
      )
    }
}
