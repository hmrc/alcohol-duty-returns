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

import play.api.libs.json.{JsResultException, Json}
import uk.gov.hmrc.alcoholdutyreturns.base.SpecBase

import java.time.Instant

class ReturnDetailsSpec extends SpecBase {
  "GetReturnDetailsSuccess must" - {
    "serialise to json" in new SetUp {
      Json
        .toJson(successfulReturnExample(appaId, periodKey, submissionId, chargeReference, Instant.now(clock)))
        .toString mustBe json
    }

    "deserialise from json" in new SetUp {
      Json.parse(json).as[GetReturnDetailsSuccess] mustBe successfulReturnExample(
        appaId,
        periodKey,
        submissionId,
        chargeReference,
        Instant.now(clock)
      )
    }
  }

  "ReturnCreate must" - {
    "be obtained from the submission data" in new SetUp {
      ReturnCreate.fromAdrReturnSubmission(returnSubmissionData, periodKey) mustBe returnCreateSubmissionData
        .copy(totalDutyDuebyTaxType = None)
    }

    "be obtained from the submission data when a nil return" in new SetUp {
      ReturnCreate.fromAdrReturnSubmission(nilReturnSubmissionData, periodKey) mustBe nilReturnCreateSubmissionData
        .copy(totalDutyDuebyTaxType = None)
    }
  }

  "OtherMaterialsUomType must" - {
    Seq(
      (OtherMaterialsUomType.Tonnes: OtherMaterialsUomType, """"01"""", AdrUnitOfMeasure.Tonnes),
      (OtherMaterialsUomType.Litres: OtherMaterialsUomType, """"02"""", AdrUnitOfMeasure.Litres)
    ).foreach { case (v, code, submissionValue) =>
      s"deserialise the code $code to OtherMaterialsUomType $v" in {
        Json.parse(code).as[OtherMaterialsUomType] mustBe v
      }

      s"serialise OtherMaterialsUomType $v to the code $code" in {
        Json.toJson(v).toString mustBe code
      }

      s"be converted from the submission value $submissionValue to $v" in {
        OtherMaterialsUomType.fromAdrUnitOfMeasure(submissionValue) mustBe v
      }
    }

    "return an error if a read value is an invalid string" in {
      a[JsResultException] mustBe thrownBy(Json.parse(""""03"""").as[OtherMaterialsUomType])
    }

    "return an error if a read value is an invalid type" in {
      a[JsResultException] mustBe thrownBy(Json.parse("""1""").as[OtherMaterialsUomType])
    }
  }

  "TypeOfSpiritType must" - {
    Seq(
      (TypeOfSpiritType.MaltSpirit: TypeOfSpiritType, """"01"""", AdrTypeOfSpirit.Malt),
      (TypeOfSpiritType.GrainSpirit: TypeOfSpiritType, """"02"""", AdrTypeOfSpirit.Grain),
      (TypeOfSpiritType.NeutralSpiritAgricultural: TypeOfSpiritType, """"03"""", AdrTypeOfSpirit.NeutralAgricultural),
      (TypeOfSpiritType.NeutralSpiritIndustrial: TypeOfSpiritType, """"04"""", AdrTypeOfSpirit.NeutralIndustrial),
      (TypeOfSpiritType.BeerBased: TypeOfSpiritType, """"05"""", AdrTypeOfSpirit.Beer),
      (TypeOfSpiritType.WineMadeWineBased: TypeOfSpiritType, """"06"""", AdrTypeOfSpirit.WineOrMadeWine),
      (TypeOfSpiritType.CiderPerryBased: TypeOfSpiritType, """"07"""", AdrTypeOfSpirit.CiderOrPerry),
      (TypeOfSpiritType.Other: TypeOfSpiritType, """"08"""", AdrTypeOfSpirit.Other)
    ).foreach { case (v, code, submissionValue) =>
      s"deserialise the code $code to TypeOfSpiritType $v" in {
        Json.parse(code).as[TypeOfSpiritType] mustBe v
      }

      s"serialise TypeOfSpiritType $v to the code $code" in {
        Json.toJson(v).toString mustBe code
      }

      s"be converted from the submission value $submissionValue to $v" in {
        TypeOfSpiritType.fromAdrTypeOfSpirit(submissionValue) mustBe v
      }
    }

    "return an error if a read value is an invalid string" in {
      a[JsResultException] mustBe thrownBy(Json.parse(""""09"""").as[TypeOfSpiritType])
    }

    "return an error if a read value is an invalid type" in {
      a[JsResultException] mustBe thrownBy(Json.parse("""1""").as[TypeOfSpiritType])
    }
  }

  "AlcoholProducts must" - {
    "convert from AdrDutyDeclared" in new SetUp {
      AlcoholProducts.fromAdrDutyDeclared(
        returnSubmissionData.dutyDeclared
      ) mustBe returnCreateSubmissionData.alcoholProducts
    }

    "not convert when not declared" in new SetUp {
      AlcoholProducts.fromAdrDutyDeclared(
        exampleNilReturnSubmissionRequest.dutyDeclared
      ) mustBe nilReturnDetails.alcoholProducts
    }
  }

  "OverDeclaration must" - {
    "convert from AdrAdjustments" in new SetUp {
      OverDeclaration.fromAdrAdjustments(
        returnSubmissionData.adjustments
      ) mustBe returnCreateSubmissionData.overDeclaration
    }

    "not convert when not declared" in new SetUp {
      OverDeclaration.fromAdrAdjustments(
        exampleNilReturnSubmissionRequest.adjustments
      ) mustBe nilReturnDetails.overDeclaration
    }
  }

  "UnderDeclaration must" - {
    "convert from AdrAdjustments" in new SetUp {
      UnderDeclaration.fromAdrAdjustments(
        returnSubmissionData.adjustments
      ) mustBe returnCreateSubmissionData.underDeclaration
    }

    "not convert when not declared" in new SetUp {
      UnderDeclaration.fromAdrAdjustments(
        exampleNilReturnSubmissionRequest.adjustments
      ) mustBe nilReturnDetails.underDeclaration
    }
  }

  "SpoiltProduct must" - {
    "convert from AdrAdjustments" in new SetUp {
      SpoiltProduct.fromAdrAdjustments(
        returnSubmissionData.adjustments
      ) mustBe returnCreateSubmissionData.spoiltProduct
    }

    "not convert when not declared" in new SetUp {
      SpoiltProduct.fromAdrAdjustments(
        exampleNilReturnSubmissionRequest.adjustments
      ) mustBe nilReturnDetails.spoiltProduct
    }
  }

  "Drawback must" - {
    "convert from AdrAdjustments" in new SetUp {
      Drawback.fromAdrAdjustments(returnSubmissionData.adjustments) mustBe returnCreateSubmissionData.drawback
    }

    "not convert when not declared" in new SetUp {
      Drawback.fromAdrAdjustments(exampleNilReturnSubmissionRequest.adjustments) mustBe nilReturnDetails.drawback
    }
  }

  "RepackagedDraught must" - {
    "convert from AdrAdjustments" in new SetUp {
      RepackagedDraught.fromAdrAdjustments(
        returnSubmissionData.adjustments
      ) mustBe returnCreateSubmissionData.repackagedDraught
    }

    "not convert when not declared" in new SetUp {
      RepackagedDraught.fromAdrAdjustments(
        exampleNilReturnSubmissionRequest.adjustments
      ) mustBe nilReturnDetails.repackagedDraught
    }
  }

  "TotalDutyDue must" - {
    "convert from AdrTotals" in new SetUp {
      TotalDutyDue.fromAdrTotals(returnSubmissionData.totals) mustBe returnCreateSubmissionData.totalDutyDue
    }
  }

  "NetDutySuspension must" - {
    "convert from AdrDutySuspended" in new SetUp {
      NetDutySuspension.fromAdrDutySuspended(
        returnSubmissionData.dutySuspended
      ) mustBe returnCreateSubmissionData.netDutySuspension
    }

    "not convert when not declared" in new SetUp {
      NetDutySuspension.fromAdrDutySuspended(
        exampleNilReturnSubmissionRequest.dutySuspended
      ) mustBe nilReturnDetails.netDutySuspension
    }
  }

  "SpiritsProduced must" - {
    "convert from AdrSpirits" in new SetUp {
      SpiritsProduced.fromAdrSpirits(
        returnSubmissionData.spirits.get
      ) mustBe returnCreateSubmissionData.spiritsProduced.get
    }
  }

  class SetUp {
    val periodKey: String = "24AA"
    val json              =
      s"""{"success":{"processingDate":"2024-06-11T15:07:47.838Z","idDetails":{"adReference":"$appaId","submissionID":"$submissionId"},"chargeDetails":{"periodKey":"$periodKey","chargeReference":"$chargeReference","periodFrom":"2024-01-01","periodTo":"2024-01-31","receiptDate":"2024-06-11T15:07:47.838Z"},"alcoholProducts":{"alcoholProductsProducedFilled":"1","regularReturn":[{"taxType":"301","dutyRate":5.27,"litresProduced":240000.02,"litresOfPureAlcohol":12041,"dutyDue":63456.07}]},"overDeclaration":{"overDeclFilled":"1","reasonForOverDecl":"Why over-declared","overDeclarationProducts":[{"returnPeriodAffected":"23AL","taxType":"302","dutyRate":3.56,"litresProduced":5000.79,"litresOfPureAlcohol":100.58,"dutyDue":358.07}]},"underDeclaration":{"underDeclFilled":"1","reasonForUnderDecl":"Why under-declared","underDeclarationProducts":[{"returnPeriodAffected":"23AK","taxType":"301","dutyRate":5.27,"litresProduced":49000.78,"litresOfPureAlcohol":989,"dutyDue":5212.03}]},"spoiltProduct":{"spoiltProdFilled":"1","spoiltProductProducts":[{"returnPeriodAffected":"23AJ","taxType":"305","dutyRate":1.75,"litresProduced":50000.69,"litresOfPureAlcohol":1000.94,"dutyDue":1751.65}]},"drawback":{"drawbackFilled":"1","drawbackProducts":[{"returnPeriodAffected":"23AI","taxType":"309","dutyRate":5.12,"litresProduced":60000.02,"litresOfPureAlcohol":1301.11,"dutyDue":6661.69}]},"repackagedDraught":{"repDraughtFilled":"1","repackagedDraughtProducts":[{"returnPeriodAffected":"23AH","originaltaxType":"300","originaldutyRate":0.64,"newTaxType":"304","dutyRate":12.76,"litresOfRepackaging":5000.97,"litresOfPureAlcohol":100.81,"dutyDue":1221.82}]},"totalDutyDuebyTaxType":[{"taxType":"301","totalDutyDueTaxType":1}],"totalDutyDue":{"totalDutyDueAlcoholProducts":63456.07,"totalDutyOverDeclaration":358.07,"totalDutyUnderDeclaration":5212.03,"totalDutySpoiltProduct":1751.65,"totalDutyDrawback":6661.69,"totalDutyRepDraughtProducts":1221.82,"totalDutyDue":61118.51},"netDutySuspension":{"netDutySuspensionFilled":"1","netDutySuspensionProducts":{"totalLtsBeer":0.15,"totalLtsWine":0.44,"totalLtsCider":0.38,"totalLtsSpirit":0.02,"totalLtsOtherFermented":0.02,"totalLtsPureAlcoholBeer":0.4248,"totalLtsPureAlcoholWine":0.5965,"totalLtsPureAlcoholCider":0.0379,"totalLtsPureAlcoholSpirit":0.2492,"totalLtsPureAlcoholOtherFermented":0.1894}},"spiritsProduced":{"spiritsProdFilled":"1","spiritsProduced":{"totalSpirits":0.05,"scotchWhiskey":0.26,"irishWhisky":0.16,"typeOfSpirit":["03"],"typeOfSpiritOther":"Coco Pops Vodka","code1MaltedBarley":0.17,"code2Other":"1","maltedGrainQuantity":0.55,"maltedGrainType":"wheat","code3Wheat":0.8,"code4Maize":0.67,"code5Rye":0.13,"code6UnmaltedGrain":0.71,"code7EthyleneGas":0.45,"code8Molasses":0.31,"code9Beer":0.37,"code10Wine":0.76,"code11MadeWine":0.6,"code12CiderOrPerry":0.04,"code13Other":"1","otherMaterialsQuantity":0.26,"otherMaterialsUom":"01","otherMaterialsType":"Coco Pops"}}}}"""

    val returnSubmissionData          = exampleReturnSubmissionRequest
    val nilReturnSubmissionData       = exampleNilReturnSubmissionRequest
    val returnCreateSubmissionData    = returnCreateSubmission(periodKey)
    val nilReturnCreateSubmissionData = nilReturnCreateSubmission(periodKey)
    val nilReturnDetails              = nilReturnExample(appaId, periodKey, submissionId, Instant.now()).success
  }
}
