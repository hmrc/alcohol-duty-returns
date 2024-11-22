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

import play.api.libs.json.{JsError, JsString, JsSuccess, Json, OFormat, Reads, Writes}
import ai.x.play.json.Jsonx
import ai.x.play.json.Encoders._
import enumeratum.{Enum, EnumEntry}
import uk.gov.hmrc.alcoholdutyreturns.models.JsonHelpers

import java.time.{Instant, LocalDate}

final case class GetReturnDetailsSuccess(success: GetReturnDetails)

object GetReturnDetailsSuccess {
  implicit val getReturnDetailsSuccessFormat: OFormat[GetReturnDetailsSuccess] =
    Json.format[GetReturnDetailsSuccess]
}

case class GetReturnDetails(
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

object GetReturnDetails {
  implicit val getReturnDetailsFormat: OFormat[GetReturnDetails] = Json.format[GetReturnDetails]
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

case class AlcoholProducts(alcoholProductsProducedFilled: Boolean, regularReturn: Option[Seq[RegularReturnDetails]])

object AlcoholProducts {
  import JsonHelpers.booleanReads
  import JsonHelpers.booleanWrites

  implicit val alcoholProductsFormat: OFormat[AlcoholProducts] = Json.format[AlcoholProducts]

  def fromAdrDutyDeclared(adrDutyDeclared: AdrDutyDeclared): AlcoholProducts =
    AlcoholProducts(
      alcoholProductsProducedFilled = adrDutyDeclared.declared,
      regularReturn = if (adrDutyDeclared.dutyDeclaredItems.nonEmpty) {
        Some(adrDutyDeclared.dutyDeclaredItems.map(RegularReturnDetails.fromAdrDutyDeclaredItem))
      } else {
        None
      }
    )
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

  def fromAdrDutyDeclaredItem(adrDutyDeclaredItem: AdrDutyDeclaredItem): RegularReturnDetails =
    RegularReturnDetails(
      taxType = adrDutyDeclaredItem.dutyDue.taxCode,
      dutyRate = adrDutyDeclaredItem.dutyDue.dutyRate,
      litresProduced = adrDutyDeclaredItem.quantityDeclared.litres,
      litresOfPureAlcohol = adrDutyDeclaredItem.quantityDeclared.lpa,
      dutyDue = adrDutyDeclaredItem.dutyDue.dutyDue,
      productName = None
    )
}

case class OverDeclaration(
  overDeclFilled: Boolean,
  reasonForOverDecl: Option[String],
  overDeclarationProducts: Option[Seq[ReturnDetails]]
)

object OverDeclaration {
  import JsonHelpers.booleanReads
  import JsonHelpers.booleanWrites

  implicit val overDeclarationFormat: OFormat[OverDeclaration] = Json.format[OverDeclaration]

  def fromAdrAdjustments(adrAdjustments: AdrAdjustments): OverDeclaration =
    OverDeclaration(
      overDeclFilled = adrAdjustments.overDeclarationDeclared,
      reasonForOverDecl = adrAdjustments.reasonForOverDeclaration,
      overDeclarationProducts = if (adrAdjustments.overDeclarationProducts.nonEmpty) {
        Some(adrAdjustments.overDeclarationProducts.map(ReturnDetails.fromAdrAdjustmentItem))
      } else {
        None
      }
    )
}

case class UnderDeclaration(
  underDeclFilled: Boolean,
  reasonForUnderDecl: Option[String],
  underDeclarationProducts: Option[Seq[ReturnDetails]]
)

object UnderDeclaration {
  import JsonHelpers.booleanReads
  import JsonHelpers.booleanWrites

  implicit val underDeclarationFormat: OFormat[UnderDeclaration] = Json.format[UnderDeclaration]

  def fromAdrAdjustments(adrAdjustments: AdrAdjustments): UnderDeclaration =
    UnderDeclaration(
      underDeclFilled = adrAdjustments.underDeclarationDeclared,
      reasonForUnderDecl = adrAdjustments.reasonForUnderDeclaration,
      underDeclarationProducts = if (adrAdjustments.underDeclarationProducts.nonEmpty) {
        Some(adrAdjustments.underDeclarationProducts.map(ReturnDetails.fromAdrAdjustmentItem))
      } else {
        None
      }
    )
}

case class SpoiltProduct(spoiltProdFilled: Boolean, spoiltProductProducts: Option[Seq[ReturnDetails]])

object SpoiltProduct {
  import JsonHelpers.booleanReads
  import JsonHelpers.booleanWrites

  implicit val spoiltProductFormat: OFormat[SpoiltProduct] = Json.format[SpoiltProduct]

  def fromAdrAdjustments(adrAdjustments: AdrAdjustments): SpoiltProduct =
    SpoiltProduct(
      spoiltProdFilled = adrAdjustments.spoiltProductDeclared,
      spoiltProductProducts = if (adrAdjustments.spoiltProducts.nonEmpty) {
        Some(adrAdjustments.spoiltProducts.map(ReturnDetails.fromAdrAdjustmentItem))
      } else {
        None
      }
    )
}

case class Drawback(drawbackFilled: Boolean, drawbackProducts: Option[Seq[ReturnDetails]])

object Drawback {
  import JsonHelpers.booleanReads
  import JsonHelpers.booleanWrites

  implicit val drawbackFormat: OFormat[Drawback] = Json.format[Drawback]

  def fromAdrAdjustments(adrAdjustments: AdrAdjustments): Drawback =
    Drawback(
      drawbackFilled = adrAdjustments.drawbackDeclared,
      drawbackProducts = if (adrAdjustments.drawbackProducts.nonEmpty) {
        Some(adrAdjustments.drawbackProducts.map(ReturnDetails.fromAdrAdjustmentItem))
      } else {
        None
      }
    )
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

  def fromAdrAdjustmentItem(adrAdjustmentItem: AdrAdjustmentItem): ReturnDetails =
    ReturnDetails(
      returnPeriodAffected = adrAdjustmentItem.returnPeriod,
      taxType = adrAdjustmentItem.dutyDue.taxCode,
      dutyRate = adrAdjustmentItem.dutyDue.dutyRate,
      litresProduced = adrAdjustmentItem.adjustmentQuantity.litres,
      litresOfPureAlcohol = adrAdjustmentItem.adjustmentQuantity.lpa,
      dutyDue = adrAdjustmentItem.dutyDue.dutyDue.abs,
      productName = None
    )
}

case class RepackagedDraught(
  repDraughtFilled: Boolean,
  repackagedDraughtProducts: Option[Seq[RepackagedDraughtProduct]]
)

object RepackagedDraught {
  import JsonHelpers.booleanReads
  import JsonHelpers.booleanWrites

  implicit val repackagedDraughtsFormat: OFormat[RepackagedDraught] = Json.format[RepackagedDraught]

  def fromAdrAdjustments(adrAdjustments: AdrAdjustments): RepackagedDraught =
    RepackagedDraught(
      repDraughtFilled = adrAdjustments.repackagedDraughtDeclared,
      repackagedDraughtProducts = if (adrAdjustments.repackagedDraughtProducts.nonEmpty) {
        Some(
          adrAdjustments.repackagedDraughtProducts.map(RepackagedDraughtProduct.fromAdrRepackagedDraughtAdjustmentItem)
        )
      } else {
        None
      }
    )
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
      dutyRate = dutyRate, // Duty is to be paid on the difference between original and new rate, but display the new
      litresProduced = BigDecimal(0), // Not used in returns display
      litresOfPureAlcohol = litresOfPureAlcohol,
      dutyDue = dutyDue,
      productName = productName
    )
}

object RepackagedDraughtProduct {
  implicit val repackagedDraughtProductFormat: OFormat[RepackagedDraughtProduct] = Json.format[RepackagedDraughtProduct]

  def fromAdrRepackagedDraughtAdjustmentItem(
    adrRepackagedDraughtAdjustmentItem: AdrRepackagedDraughtAdjustmentItem
  ): RepackagedDraughtProduct =
    RepackagedDraughtProduct(
      returnPeriodAffected = adrRepackagedDraughtAdjustmentItem.returnPeriod,
      originaltaxType = adrRepackagedDraughtAdjustmentItem.originalTaxCode,
      originaldutyRate = adrRepackagedDraughtAdjustmentItem.originalDutyRate,
      newTaxType = adrRepackagedDraughtAdjustmentItem.newTaxCode,
      dutyRate = adrRepackagedDraughtAdjustmentItem.newDutyRate,
      litresOfRepackaging = adrRepackagedDraughtAdjustmentItem.repackagedQuantity.litres,
      litresOfPureAlcohol = adrRepackagedDraughtAdjustmentItem.repackagedQuantity.lpa,
      dutyDue = adrRepackagedDraughtAdjustmentItem.dutyAdjustment,
      productName = None
    )
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

  def fromAdrTotals(adrTotals: AdrTotals): TotalDutyDue =
    TotalDutyDue(
      totalDutyDueAlcoholProducts = adrTotals.declaredDutyDue,
      totalDutyOverDeclaration = adrTotals.overDeclaration.abs,
      totalDutyUnderDeclaration = adrTotals.underDeclaration,
      totalDutySpoiltProduct = adrTotals.spoiltProduct.abs,
      totalDutyDrawback = adrTotals.drawback.abs,
      totalDutyRepDraughtProducts = adrTotals.repackagedDraught,
      totalDutyDue = adrTotals.totalDutyDue
    )
}

case class NetDutySuspension(
  netDutySuspensionFilled: Boolean,
  netDutySuspensionProducts: Option[NetDutySuspensionProducts]
)

object NetDutySuspension {
  import JsonHelpers.booleanReads
  import JsonHelpers.booleanWrites

  implicit val netDutySuspensionFormat: OFormat[NetDutySuspension] = Json.format[NetDutySuspension]

  def fromAdrDutySuspended(adrDutySuspended: AdrDutySuspended): NetDutySuspension =
    NetDutySuspension(
      netDutySuspensionFilled = adrDutySuspended.declared,
      netDutySuspensionProducts = if (adrDutySuspended.dutySuspendedProducts.nonEmpty) {
        Some(NetDutySuspensionProducts.fromAdrDutySuspendedProducts(adrDutySuspended.dutySuspendedProducts))
      } else {
        None
      }
    )
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

  private def apply(): NetDutySuspensionProducts =
    NetDutySuspensionProducts(
      totalLtsBeer = None,
      totalLtsWine = None,
      totalLtsCider = None,
      totalLtsSpirit = None,
      totalLtsOtherFermented = None,
      totalLtsPureAlcoholBeer = None,
      totalLtsPureAlcoholWine = None,
      totalLtsPureAlcoholCider = None,
      totalLtsPureAlcoholSpirit = None,
      totalLtsPureAlcoholOtherFermented = None
    )

  def fromAdrDutySuspendedProducts(dutySuspendedProducts: Seq[AdrDutySuspendedProduct]): NetDutySuspensionProducts =
    dutySuspendedProducts.foldLeft(NetDutySuspensionProducts()) { case (ndsp, suspendedProduct) =>
      suspendedProduct.regime match {
        case AdrDutySuspendedAlcoholRegime.Beer                  =>
          ndsp.copy(
            totalLtsBeer = Some(suspendedProduct.suspendedQuantity.litres),
            totalLtsPureAlcoholBeer = Some(suspendedProduct.suspendedQuantity.lpa)
          )
        case AdrDutySuspendedAlcoholRegime.Cider                 =>
          ndsp.copy(
            totalLtsCider = Some(suspendedProduct.suspendedQuantity.litres),
            totalLtsPureAlcoholCider = Some(suspendedProduct.suspendedQuantity.lpa)
          )
        case AdrDutySuspendedAlcoholRegime.Wine                  =>
          ndsp.copy(
            totalLtsWine = Some(suspendedProduct.suspendedQuantity.litres),
            totalLtsPureAlcoholWine = Some(suspendedProduct.suspendedQuantity.lpa)
          )
        case AdrDutySuspendedAlcoholRegime.Spirits               =>
          ndsp.copy(
            totalLtsSpirit = Some(suspendedProduct.suspendedQuantity.litres),
            totalLtsPureAlcoholSpirit = Some(suspendedProduct.suspendedQuantity.lpa)
          )
        case AdrDutySuspendedAlcoholRegime.OtherFermentedProduct =>
          ndsp.copy(
            totalLtsOtherFermented = Some(suspendedProduct.suspendedQuantity.litres),
            totalLtsPureAlcoholOtherFermented = Some(suspendedProduct.suspendedQuantity.lpa)
          )
      }
    }
}

sealed trait TypeOfSpiritType extends EnumEntry

object TypeOfSpiritType extends Enum[TypeOfSpiritType] {
  val values = findValues

  case object MaltSpirit extends TypeOfSpiritType
  case object GrainSpirit extends TypeOfSpiritType
  case object NeutralSpiritAgricultural extends TypeOfSpiritType
  case object NeutralSpiritIndustrial extends TypeOfSpiritType
  case object BeerBased extends TypeOfSpiritType
  case object WineMadeWineBased extends TypeOfSpiritType
  case object CiderPerryBased extends TypeOfSpiritType
  case object Other extends TypeOfSpiritType

  implicit val typeOfSpiritTypeReads: Reads[TypeOfSpiritType] = {
    case JsString("01") => JsSuccess(MaltSpirit)
    case JsString("02") => JsSuccess(GrainSpirit)
    case JsString("03") => JsSuccess(NeutralSpiritAgricultural)
    case JsString("04") => JsSuccess(NeutralSpiritIndustrial)
    case JsString("05") => JsSuccess(BeerBased)
    case JsString("06") => JsSuccess(WineMadeWineBased)
    case JsString("07") => JsSuccess(CiderPerryBased)
    case JsString("08") => JsSuccess(Other)
    case s: JsString    => JsError(s"$s is not a valid TypeOfSpiritType")
    case v              => JsError(s"got $v was expecting a string representing a TypeOfSpiritType")
  }

  implicit val typeOfSpiritTypeWrites: Writes[TypeOfSpiritType] = {
    case MaltSpirit                => JsString("01")
    case GrainSpirit               => JsString("02")
    case NeutralSpiritAgricultural => JsString("03")
    case NeutralSpiritIndustrial   => JsString("04")
    case BeerBased                 => JsString("05")
    case WineMadeWineBased         => JsString("06")
    case CiderPerryBased           => JsString("07")
    case Other                     => JsString("08")
  }

  def fromAdrTypeOfSpirit(typeOfSpirit: AdrTypeOfSpirit): TypeOfSpiritType =
    typeOfSpirit match {
      case AdrTypeOfSpirit.Malt                => MaltSpirit
      case AdrTypeOfSpirit.Grain               => GrainSpirit
      case AdrTypeOfSpirit.NeutralAgricultural => NeutralSpiritAgricultural
      case AdrTypeOfSpirit.NeutralIndustrial   => NeutralSpiritIndustrial
      case AdrTypeOfSpirit.Beer                => BeerBased
      case AdrTypeOfSpirit.WineOrMadeWine      => WineMadeWineBased
      case AdrTypeOfSpirit.CiderOrPerry        => CiderPerryBased
      case AdrTypeOfSpirit.Other               => Other
    }
}

sealed trait OtherMaterialsUomType extends EnumEntry

object OtherMaterialsUomType extends Enum[OtherMaterialsUomType] {
  val values = findValues

  case object Tonnes extends OtherMaterialsUomType
  case object Litres extends OtherMaterialsUomType

  implicit val otherMaterialsUomTypeReads: Reads[OtherMaterialsUomType] = {
    case JsString("01") => JsSuccess(Tonnes)
    case JsString("02") => JsSuccess(Litres)
    case s: JsString    => JsError(s"$s is not a valid OtherMaterialsUomType")
    case v              => JsError(s"got $v was expecting a string representing a OtherMaterialsUomType")
  }

  implicit val otherMaterialsUomTypeWrites: Writes[OtherMaterialsUomType] = {
    case Tonnes => JsString("01")
    case Litres => JsString("02")
  }

  def fromAdrUnitOfMeasure(unitOfMeasure: AdrUnitOfMeasure): OtherMaterialsUomType =
    unitOfMeasure match {
      case AdrUnitOfMeasure.Tonnes => Tonnes
      case AdrUnitOfMeasure.Litres => Litres
    }
}

case class SpiritsProduced(spiritsProdFilled: Boolean, spiritsProduced: Option[SpiritsProducedDetails])

object SpiritsProduced {
  import JsonHelpers.booleanReads
  import JsonHelpers.booleanWrites

  implicit val spiritsProducedFormat: OFormat[SpiritsProduced] = Json.format[SpiritsProduced]

  def fromAdrSpirits(spirits: AdrSpirits): SpiritsProduced =
    SpiritsProduced(
      spiritsProdFilled = spirits.spiritsDeclared,
      spiritsProduced = spirits.spiritsProduced.map(SpiritsProducedDetails.fromAdrSpiritsProduced)
    )
}

case class SpiritsProducedDetails(
  totalSpirits: BigDecimal,
  scotchWhiskey: BigDecimal,
  irishWhisky: BigDecimal,
  typeOfSpirit: Set[TypeOfSpiritType],
  typeOfSpiritOther: Option[String],
  code1MaltedBarley: Option[BigDecimal],
  code2Other: Option[Boolean],
  maltedGrainQuantity: Option[BigDecimal],
  maltedGrainType: Option[String],
  code3Wheat: Option[BigDecimal],
  code4Maize: Option[BigDecimal],
  code5Rye: Option[BigDecimal],
  code6UnmaltedGrain: Option[BigDecimal],
  code7EthyleneGas: Option[BigDecimal],
  code8Molasses: Option[BigDecimal],
  code9Beer: Option[BigDecimal],
  code10Wine: Option[BigDecimal],
  code11MadeWine: Option[BigDecimal],
  code12CiderOrPerry: Option[BigDecimal],
  code13Other: Option[Boolean],
  otherMaterialsQuantity: Option[BigDecimal],
  otherMaterialsUom: Option[OtherMaterialsUomType],
  otherMaterialsType: Option[String]
)

object SpiritsProducedDetails {
  import JsonHelpers.booleanReads
  import JsonHelpers.booleanWrites

  implicit val spiritsProducedDetailsFormat: OFormat[SpiritsProducedDetails] =
    Jsonx.formatCaseClass[SpiritsProducedDetails]

  def fromAdrSpiritsProduced(spiritsProduced: AdrSpiritsProduced): SpiritsProducedDetails =
    // Note the spelling of Whisk(e)y in the downstream API is the wrong way around
    SpiritsProducedDetails(
      totalSpirits = spiritsProduced.spiritsVolumes.totalSpirits,
      scotchWhiskey = spiritsProduced.spiritsVolumes.scotchWhisky,
      irishWhisky = spiritsProduced.spiritsVolumes.irishWhiskey,
      typeOfSpirit = spiritsProduced.typesOfSpirit.map(TypeOfSpiritType.fromAdrTypeOfSpirit),
      typeOfSpiritOther = spiritsProduced.otherSpiritTypeName,
      code1MaltedBarley = spiritsProduced.grainsQuantities.maltedBarley,
      code2Other = Some(spiritsProduced.hasOtherMaltedGrain),
      maltedGrainQuantity = spiritsProduced.grainsQuantities.otherMaltedGrain,
      maltedGrainType = spiritsProduced.otherMaltedGrainType,
      code3Wheat = spiritsProduced.grainsQuantities.wheat,
      code4Maize = spiritsProduced.grainsQuantities.maize,
      code5Rye = spiritsProduced.grainsQuantities.rye,
      code6UnmaltedGrain = spiritsProduced.grainsQuantities.unmaltedGrain,
      code7EthyleneGas = spiritsProduced.ingredientsVolumes.ethylene,
      code8Molasses = spiritsProduced.ingredientsVolumes.molasses,
      code9Beer = spiritsProduced.ingredientsVolumes.beer,
      code10Wine = spiritsProduced.ingredientsVolumes.wine,
      code11MadeWine = spiritsProduced.ingredientsVolumes.madeWine,
      code12CiderOrPerry = spiritsProduced.ingredientsVolumes.ciderOrPerry,
      code13Other = Some(spiritsProduced.otherIngredient.nonEmpty),
      otherMaterialsQuantity = spiritsProduced.otherIngredient.map(_.quantity),
      otherMaterialsUom = spiritsProduced.otherIngredient.map(otherIngredient =>
        OtherMaterialsUomType.fromAdrUnitOfMeasure(otherIngredient.unitOfMeasure)
      ),
      otherMaterialsType = spiritsProduced.otherIngredient.map(_.ingredientName)
    )
}

case class ReturnCreate(
  periodKey: String,
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

object ReturnCreate {
  implicit val returnCreateFormat: OFormat[ReturnCreate] = Json.format[ReturnCreate]

  def fromAdrReturnSubmission(adrReturnSubmission: AdrReturnSubmission, periodKey: String): ReturnCreate =
    ReturnCreate(
      periodKey = periodKey,
      alcoholProducts = AlcoholProducts.fromAdrDutyDeclared(adrReturnSubmission.dutyDeclared),
      overDeclaration = OverDeclaration.fromAdrAdjustments(adrReturnSubmission.adjustments),
      underDeclaration = UnderDeclaration.fromAdrAdjustments(adrReturnSubmission.adjustments),
      spoiltProduct = SpoiltProduct.fromAdrAdjustments(adrReturnSubmission.adjustments),
      drawback = Drawback.fromAdrAdjustments(adrReturnSubmission.adjustments),
      repackagedDraught = RepackagedDraught.fromAdrAdjustments(adrReturnSubmission.adjustments),
      totalDutyDuebyTaxType = None, // This is populated from data received from a calculator call
      totalDutyDue = TotalDutyDue.fromAdrTotals(adrReturnSubmission.totals),
      netDutySuspension = NetDutySuspension.fromAdrDutySuspended(adrReturnSubmission.dutySuspended),
      spiritsProduced = adrReturnSubmission.spirits.map(SpiritsProduced.fromAdrSpirits)
    )
}

final case class ReturnCreatedSuccess(success: ReturnCreatedDetails)

object ReturnCreatedSuccess {
  implicit val returnCreatedSuccessFormat: OFormat[ReturnCreatedSuccess] =
    Json.format[ReturnCreatedSuccess]
}

case class ReturnCreatedDetails(
  processingDate: Instant,
  adReference: String,
  amount: BigDecimal,
  chargeReference: Option[String],
  paymentDueDate: Option[LocalDate],
  submissionID: String
)

object ReturnCreatedDetails {
  implicit val returnCreatedDetailsFormat: OFormat[ReturnCreatedDetails] = Json.format[ReturnCreatedDetails]
}
