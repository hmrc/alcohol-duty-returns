/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.alcoholdutyreturns.models

import play.api.libs.functional.syntax.*
import play.api.libs.json.*

enum ApprovalStatus:
  case Approved, SmallCiderProducer, Insolvent, DeRegistered, Revoked

object ApprovalStatus {
  given format: Format[ApprovalStatus] = new Format[ApprovalStatus] {
    def reads(json: JsValue): JsResult[ApprovalStatus] = json match {
      case JsString("Approved")           => JsSuccess(Approved)
      case JsString("SmallCiderProducer") => JsSuccess(SmallCiderProducer)
      case JsString("Insolvent")          => JsSuccess(Insolvent)
      case JsString("DeRegistered")       => JsSuccess(DeRegistered)
      case JsString("Revoked")            => JsSuccess(Revoked)
      case _                              => JsError("Invalid Approval Status")
    }

    def writes(approvalStatus: ApprovalStatus): JsValue = JsString(approvalStatus.toString)
  }
}

case class SubscriptionSummary(
  approvalStatus: ApprovalStatus,
  regimes: Set[AlcoholRegime],
  paperlessReference: Boolean,
  bouncedEmail: Boolean = false
)

object SubscriptionSummary {

  private implicit val reads: Reads[SubscriptionSummary] =
    ((JsPath \ "approvalStatus").read[ApprovalStatus] and
      (JsPath \ "regimes").read[Set[AlcoholRegime]] and
      (JsPath \ "contactPreference").read[String].map(_.equalsIgnoreCase("digital")) and
      (JsPath \ "emailBounced").readNullable[Boolean].map(_.getOrElse(false)))(
      (approvalStatus, regimes, paperlessReference, bouncedEmail) =>
        SubscriptionSummary.apply(
          approvalStatus,
          if (regimes.nonEmpty) {
            regimes
          } else {
            throw new IllegalArgumentException("Expecting at least one regime to be approved")
          },
          paperlessReference,
          bouncedEmail
        )
    )

  private implicit val writes: OWrites[SubscriptionSummary] =
    ((JsPath \ "approvalStatus").write[ApprovalStatus] and
      (JsPath \ "regimes").write[Set[AlcoholRegime]] and
      (JsPath \ "contactPreference").write[String] and
      (JsPath \ "emailBounced").write[Boolean])(s =>
      (s.approvalStatus, s.regimes, if (s.paperlessReference) "digital" else "paper", s.bouncedEmail)
    )

  implicit val format: Format[SubscriptionSummary] = Format(reads, writes)
}
