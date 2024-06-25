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

package uk.gov.hmrc.alcoholdutyreturns.models.returns

import play.api.libs.json.{Json, OFormat}
import ai.x.play.json.Jsonx
import ai.x.play.json.Encoders._

import java.time.{Instant, LocalDate}

case class ReturnDetailsSuccess(
  processingDate: Instant,
  idDetails: IdDetails,
  chargeDetails: ChargeDetails,
  alcoholProducts: AlcoholProducts,
  overDeclaration: OverDeclaration,
  underDeclaration: UnderDeclaration,
  spoiltProduct: SpoiltProduct,
  drawback: Drawback,
  repackagedDraught: RepackagedDraught,
  totalDutyDuebyTaxType: Option[Seq[TotalDutyDuebyTaxType]],
  totalDutyDue: TotalDutyDue,
  netDutySuspension: NetDutySuspension,
  spiritsProduced: Option[SpiritsProduced]
)

object ReturnDetailsSuccess {
  implicit val returnDetailsSuccessFormat: OFormat[ReturnDetailsSuccess] = Json.format[ReturnDetailsSuccess]
}

case class IdDetails(adReference: String, submissionID: String)

object IdDetails {
  implicit val idDetailsFormat: OFormat[IdDetails] = Json.format[IdDetails]
}

case class ChargeDetails(
  periodKey: String,
  chargeReference: Option[String],
  periodFrom: LocalDate,
  periodTo: LocalDate,
  receiptDate: Instant
)

object ChargeDetails {
  implicit val chargeDetailsFormat: OFormat[ChargeDetails] = Json.format[ChargeDetails]
}

case class AlcoholProducts(alcoholProductsProducedFilled: String, regularReturn: Option[Seq[RegularReturnDetails]])

object AlcoholProducts {
  implicit val alcoholProductsFormat: OFormat[AlcoholProducts] = Json.format[AlcoholProducts]
}

case class RegularReturnDetails(
  taxType: String,
  dutyRate: BigDecimal,
  litresProduced: BigDecimal,
  litresOfPureAlcohol: BigDecimal,
  dutyDue: BigDecimal,
  productName: Option[String]
)

object RegularReturnDetails {
  implicit val regularReturnDetailsFormat: OFormat[RegularReturnDetails] = Json.format[RegularReturnDetails]
}

case class OverDeclaration(
  overDeclFilled: String,
  reasonForOverDecl: Option[String],
  overDeclaration: Option[Seq[ReturnDetails]]
)

object OverDeclaration {
  implicit val overDeclarationFormat: OFormat[OverDeclaration] = Json.format[OverDeclaration]
}

case class UnderDeclaration(
  underDeclFilled: String,
  reasonForUnderDecl: Option[String],
  underDeclaration: Option[Seq[ReturnDetails]]
)

object UnderDeclaration {
  implicit val underDeclarationFormat: OFormat[UnderDeclaration] = Json.format[UnderDeclaration]
}

case class SpoiltProduct(spoiltProdFilled: String, spoiltProduct: Option[Seq[ReturnDetails]])

object SpoiltProduct {
  implicit val spoiltProductFormat: OFormat[SpoiltProduct] = Json.format[SpoiltProduct]
}

case class Drawback(drawbackFilled: String, drawbackProducts: Option[Seq[ReturnDetails]])

object Drawback {
  implicit val drawbackFormat: OFormat[Drawback] = Json.format[Drawback]
}

case class ReturnDetails(
  returnPeriodAffected: String,
  taxType: String,
  dutyRate: BigDecimal,
  litresProduced: BigDecimal,
  litresOfPureAlcohol: BigDecimal,
  dutyDue: BigDecimal,
  productName: Option[String]
)

object ReturnDetails {
  implicit val returnDetailsFormat: OFormat[ReturnDetails] = Json.format[ReturnDetails]
}

case class RepackagedDraught(repDraughtFilled: String, repackagedDraughtProducts: Option[Seq[RepackagedDraughtProduct]])

object RepackagedDraught {
  implicit val repackagedDraughtsFormat: OFormat[RepackagedDraught] = Json.format[RepackagedDraught]
}

case class RepackagedDraughtProduct(
  returnPeriodAffected: String,
  originaltaxType: String,
  originaldutyRate: BigDecimal,
  newTaxType: String,
  dutyRate: BigDecimal,
  litresOfRepackaging: BigDecimal,
  litresOfPureAlcohol: BigDecimal,
  dutyDue: BigDecimal,
  productName: Option[String]
) {
  def toReturnDetailsForAdjustment(): ReturnDetails =
    ReturnDetails(
      returnPeriodAffected = returnPeriodAffected,
      taxType = newTaxType,
      dutyRate = dutyRate,
      litresProduced = BigDecimal(0),
      litresOfPureAlcohol = litresOfPureAlcohol,
      dutyDue = dutyDue,
      productName = productName
    )
}

object RepackagedDraughtProduct {
  implicit val repackagedDraughtProductFormat: OFormat[RepackagedDraughtProduct] = Json.format[RepackagedDraughtProduct]
}

case class TotalDutyDuebyTaxType(taxType: String, totalDutyDueTaxType: BigDecimal)

object TotalDutyDuebyTaxType {
  implicit val totalDutyDuebyTaxTypeFormat: OFormat[TotalDutyDuebyTaxType] = Json.format[TotalDutyDuebyTaxType]
}

case class TotalDutyDue(
  totalDutyDueAlcoholProducts: BigDecimal,
  totalDutyOverDeclaration: BigDecimal,
  totalDutyUnderDeclaration: BigDecimal,
  totalDutySpoiltProduct: BigDecimal,
  totalDutyDrawback: BigDecimal,
  totalDutyRepDraughtProducts: BigDecimal,
  totalDutyDue: BigDecimal
)

object TotalDutyDue {
  implicit val totalDutyDueFormat: OFormat[TotalDutyDue] = Json.format[TotalDutyDue]
}

case class NetDutySuspension(
  netDutySuspensionFilled: String,
  netDutySuspensionProducts: Option[NetDutySuspensionProducts]
)

object NetDutySuspension {
  implicit val netDutySuspensionFormat: OFormat[NetDutySuspension] = Json.format[NetDutySuspension]
}

case class NetDutySuspensionProducts(
  totalLtsBeer: Option[BigDecimal],
  totalLtsWine: Option[BigDecimal],
  totalLtsCider: Option[BigDecimal],
  totalLtsSpirit: Option[BigDecimal],
  totalLtsOtherFermented: Option[BigDecimal],
  totalLtsPureAlcoholBeer: Option[BigDecimal],
  totalLtsPureAlcoholWine: Option[BigDecimal],
  totalLtsPureAlcoholCider: Option[BigDecimal],
  totalLtsPureAlcoholSpirit: Option[BigDecimal],
  totalLtsPureAlcoholOtherFermented: Option[BigDecimal]
)

object NetDutySuspensionProducts {
  implicit val netDutySuspensionProductsFormat: OFormat[NetDutySuspensionProducts] =
    Json.format[NetDutySuspensionProducts]
}

case class SpiritsProduced(spiritsProdFilled: String, spiritsProduced: Option[SpiritsProducedDetails])

object SpiritsProduced {
  implicit val spiritsProducedFormat: OFormat[SpiritsProduced] = Json.format[SpiritsProduced]
}

case class SpiritsProducedDetails(
  totalSpirits: BigDecimal,
  scotchWhiskey: BigDecimal,
  irishWhisky: BigDecimal,
  typeOfSpirit: Seq[String],
  typeOfSpiritOther: Option[String],
  code1MaltedBarley: Option[BigDecimal],
  code2Other: Option[String],
  maltedGrainQuantity: Option[BigDecimal],
  maltedGrainType: Option[String],
  code3Wheat: Option[BigDecimal],
  code4Maize: Option[BigDecimal],
  code5Rye: Option[BigDecimal],
  code6UnmaltedGrain: Option[BigDecimal],
  code7EthyleneGas: Option[BigDecimal],
  code8Molassess: Option[BigDecimal],
  code9Beer: Option[BigDecimal],
  code10Wine: Option[BigDecimal],
  code11MadeWine: Option[BigDecimal],
  code12CiderOrPerry: Option[BigDecimal],
  code13Other: Option[String],
  otherMaterialsQuantity: Option[BigDecimal],
  otherMaterialUom: Option[String],
  otherMaterialsType: Option[String]
)

object SpiritsProducedDetails {
  implicit val spiritsProducedDetailsFormat: OFormat[SpiritsProducedDetails] =
    Jsonx.formatCaseClass[SpiritsProducedDetails]
}
