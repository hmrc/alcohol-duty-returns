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
  "ReturnDetails" when {
    "should serialise to json" in new SetUp {
      Json
        .toJson(successfulReturnsExample(appaId, periodKey, submissionId, chargeReference, Instant.now(clock)))
        .toString shouldBe json
    }

    "should deserialise from json" in new SetUp {
      Json.parse(json).as[ReturnDetailsSuccess] shouldBe successfulReturnsExample(
        appaId,
        periodKey,
        submissionId,
        chargeReference,
        Instant.now(clock)
      )
    }
  }

  "OtherMaterialsUomType" should {
    Seq(
      (OtherMaterialsUomType.Tonnes: OtherMaterialsUomType, """"01""""),
      (OtherMaterialsUomType.Litres: OtherMaterialsUomType, """"02"""")
    ).foreach { case (v, code) =>
      s"deserialise the code $code to OtherMaterialsUomType $v" in {
        Json.parse(code).as[OtherMaterialsUomType] shouldBe v
      }

      s"serialise OtherMaterialsUomType $v to the code $code" in {
        Json.toJson(v).toString shouldBe code
      }
    }

    "return an error if a read value is an invalid string" in {
      a[JsResultException] shouldBe thrownBy(Json.parse(""""03"""").as[OtherMaterialsUomType])
    }

    "return an error if a read value is an invalid type" in {
      a[JsResultException] shouldBe thrownBy(Json.parse("""1""").as[OtherMaterialsUomType])
    }
  }

  "TypeOfSpiritType" should {
    Seq(
      (TypeOfSpiritType.MaltSpirit: TypeOfSpiritType, """"01""""),
      (TypeOfSpiritType.GrainSpirit: TypeOfSpiritType, """"02""""),
      (TypeOfSpiritType.NeutralSpiritAgricultural: TypeOfSpiritType, """"03""""),
      (TypeOfSpiritType.NeutralSpiritIndustrial: TypeOfSpiritType, """"04""""),
      (TypeOfSpiritType.BeerBased: TypeOfSpiritType, """"05""""),
      (TypeOfSpiritType.WineMadeWineBased: TypeOfSpiritType, """"06""""),
      (TypeOfSpiritType.CiderPerryBased: TypeOfSpiritType, """"07""""),
      (TypeOfSpiritType.Other: TypeOfSpiritType, """"08"""")
    ).foreach { case (v, code) =>
      s"deserialise the code $code to TypeOfSpiritType $v" in {
        Json.parse(code).as[TypeOfSpiritType] shouldBe v
      }

      s"serialise TypeOfSpiritType $v to the code $code" in {
        Json.toJson(v).toString shouldBe code
      }
    }

    "return an error if a read value is an invalid string" in {
      a[JsResultException] shouldBe thrownBy(Json.parse(""""09"""").as[TypeOfSpiritType])
    }

    "return an error if a read value is an invalid type" in {
      a[JsResultException] shouldBe thrownBy(Json.parse("""1""").as[TypeOfSpiritType])
    }
  }

  class SetUp {
    val periodKey: String = "24AC"
    val json              =
      s"""{"processingDate":"2024-06-11T15:07:47.838Z","idDetails":{"adReference":"$appaId","submissionID":"$submissionId"},"chargeDetails":{"periodKey":"24AC","chargeReference":"$chargeReference","periodFrom":"2024-03-01","periodTo":"2024-03-31","receiptDate":"2024-06-11T15:07:47.838Z"},"alcoholProducts":{"alcoholProductsProducedFilled":"1","regularReturn":[{"taxType":"301","dutyRate":5.27,"litresProduced":240000.02,"litresOfPureAlcohol":12041,"dutyDue":63456.07}]},"overDeclaration":{"overDeclFilled":"1","reasonForOverDecl":"Why over-declared","overDeclarationProducts":[{"returnPeriodAffected":"24AB","taxType":"302","dutyRate":3.56,"litresProduced":5000.79,"litresOfPureAlcohol":100.58,"dutyDue":358.07}]},"underDeclaration":{"underDeclFilled":"1","reasonForUnderDecl":"Why under-declared","underDeclarationProducts":[{"returnPeriodAffected":"24AA","taxType":"301","dutyRate":5.27,"litresProduced":49000.78,"litresOfPureAlcohol":989,"dutyDue":5212.03}]},"spoiltProduct":{"spoiltProdFilled":"1","spoiltProductProducts":[{"returnPeriodAffected":"23AL","taxType":"305","dutyRate":1.75,"litresProduced":50000.69,"litresOfPureAlcohol":1000.94,"dutyDue":1751.65}]},"drawback":{"drawbackFilled":"1","drawbackProducts":[{"returnPeriodAffected":"23AK","taxType":"309","dutyRate":5.12,"litresProduced":60000.02,"litresOfPureAlcohol":1301.11,"dutyDue":6661.69}]},"repackagedDraught":{"repDraughtFilled":"1","repackagedDraughtProducts":[{"returnPeriodAffected":"23AJ","originaltaxType":"300","originaldutyRate":0.64,"newTaxType":"304","dutyRate":12.76,"litresOfRepackaging":5000.97,"litresOfPureAlcohol":100.81,"dutyDue":1221.82}]},"totalDutyDuebyTaxType":[{"taxType":"301","totalDutyDueTaxType":1}],"totalDutyDue":{"totalDutyDueAlcoholProducts":63456.07,"totalDutyOverDeclaration":358.07,"totalDutyUnderDeclaration":5212.03,"totalDutySpoiltProduct":1751.65,"totalDutyDrawback":6661.69,"totalDutyRepDraughtProducts":1221.82,"totalDutyDue":61118.51},"netDutySuspension":{"netDutySuspensionFilled":"1","netDutySuspensionProducts":{"totalLtsBeer":0.15,"totalLtsWine":0.44,"totalLtsCider":0.38,"totalLtsSpirit":0.02,"totalLtsOtherFermented":0.02,"totalLtsPureAlcoholBeer":0.4248,"totalLtsPureAlcoholWine":0.5965,"totalLtsPureAlcoholCider":0.0379,"totalLtsPureAlcoholSpirit":0.2492,"totalLtsPureAlcoholOtherFermented":0.1894}},"spiritsProduced":{"spiritsProdFilled":"1","spiritsProduced":{"totalSpirits":0.05,"scotchWhiskey":0.26,"irishWhisky":0.16,"typeOfSpirit":["03"],"typeOfSpiritOther":"Coco Pops Vodka","code1MaltedBarley":0.17,"code2Other":"1","maltedGrainQuantity":0.55,"maltedGrainType":"wheat","code3Wheat":0.8,"code4Maize":0.67,"code5Rye":0.13,"code6UnmaltedGrain":0.71,"code7EthyleneGas":0.45,"code8Molassess":0.31,"code9Beer":0.37,"code10Wine":0.76,"code11MadeWine":0.6,"code12CiderOrPerry":0.04,"code13Other":"1","otherMaterialsQuantity":0.26,"otherMaterialUom":"01","otherMaterialsType":"Coco Pops"}}}"""
  }
}
