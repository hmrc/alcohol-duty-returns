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

import play.api.libs.json.Json
import uk.gov.hmrc.alcoholdutyreturns.base.SpecBase

import java.time.Instant

class ReturnDetailsSpec extends SpecBase {
  "ReturnDetails" when {
    "should serialise to json" in new SetUp {
      Json.toJson(successfulReturnsExample(appaId, periodKey, Instant.now(clock))).toString shouldBe json
    }

    "should deserialise from json" in new SetUp {
      Json.parse(json).as[ReturnDetailsSuccess] shouldBe successfulReturnsExample(
        appaId,
        periodKey,
        Instant.now(clock)
      )
    }
  }

  class SetUp {
    val periodKey: String = "24AC"
    val json              =
      s"""{"processingDate":"2024-06-11T15:07:47.838Z","idDetails":{"adReference":"$appaId","submissionID":"123456789123"},"chargeDetails":{"periodKey":"24AC","chargeReference":"$appaId","periodFrom":"2024-03-01","periodTo":"2024-03-31","receiptDate":"2024-06-11T15:07:47.838Z"},"alcoholProducts":{"alcoholProductsProducedFilled":"1","regularReturn":[{"taxType":"301","dutyRate":0.05,"litresProduced":0.02,"litresOfPureAlcohol":0.4083,"dutyDue":0.36,"productName":"whisky"}]},"overDeclaration":{"overDeclFilled":"1","reasonForOverDecl":"Why over-declared","overDeclaration":[{"returnPeriodAffected":"24AB","taxType":"301","dutyRate":0.3,"litresProduced":0.79,"litresOfPureAlcohol":0.5478,"dutyDue":0.57,"productName":"gin"}]},"underDeclaration":{"underDeclFilled":"1","reasonForUnderDecl":"Why under-declared","underDeclaration":[{"returnPeriodAffected":"24AA","taxType":"301","dutyRate":1,"litresProduced":0.78,"litresOfPureAlcohol":0.8989,"dutyDue":0.6,"productName":"cider"}]},"spoiltProduct":{"spoiltProdFilled":"1","spoiltProduct":[{"returnPeriodAffected":"23AL","taxType":"301","dutyRate":0.75,"litresProduced":0.69,"litresOfPureAlcohol":0.9411,"dutyDue":0.61,"productName":"red wine"}]},"drawback":{"drawbackFilled":"1","drawbackProducts":[{"returnPeriodAffected":"23AK","taxType":"301","dutyRate":0.05,"litresProduced":0.02,"litresOfPureAlcohol":0.1571,"dutyDue":0.12,"productName":"brandy"}]},"repackagedDraught":{"repDraughtFilled":"1","repackagedDraughtProducts":[{"returnPeriodAffected":"23AJ","originaltaxType":"300","originaldutyRate":0.64,"newTaxType":"301","dutyRate":0.01,"litresOfRepackaging":0.97,"litresOfPureAlcohol":0.0681,"dutyDue":0.68,"productName":"wine"}]},"totalDutyDuebyTaxType":[{"taxType":"301","totalDutyDueTaxType":1}],"totalDutyDue":{"totalDutyDueAlcoholProducts":0.35,"totalDutyOverDeclaration":0.99,"totalDutyUnderDeclaration":0.12,"totalDutySpoiltProduct":0.68,"totalDutyDrawback":0.54,"totalDutyRepDraughtProducts":0.97,"totalDutyDue":0.72},"netDutySuspension":{"netDutySuspensionFilled":"1","netDutySuspensionProducts":{"totalLtsBeer":0.15,"totalLtsWine":0.44,"totalLtsCider":0.38,"totalLtsSpirit":0.02,"totalLtsOtherFermented":0.02,"totalLtsPureAlcoholBeer":0.4248,"totalLtsPureAlcoholWine":0.5965,"totalLtsPureAlcoholCider":0.0379,"totalLtsPureAlcoholSpirit":0.2492,"totalLtsPureAlcoholOtherFermented":0.1894}},"spiritsProduced":{"spiritsProdFilled":"1","spiritsProduced":{"totalSpirits":0.05,"scotchWhiskey":0.26,"irishWhisky":0.16,"typeOfSpirit":["03"],"typeOfSpiritOther":"Coco Pops Vodka","code1MaltedBarley":0.17,"code2Other":"1","maltedGrainQuantity":0.55,"maltedGrainType":"wheat","code3Wheat":0.8,"code4Maize":0.67,"code5Rye":0.13,"code6UnmaltedGrain":0.71,"code7EthyleneGas":0.45,"code8Molassess":0.31,"code9Beer":0.37,"code10Wine":0.76,"code11MadeWine":0.6,"code12CiderOrPerry":0.04,"code13Other":"1","otherMaterialsQuantity":0.26,"otherMaterialUom":"01","otherMaterialsType":"Coco Pops"}}}"""
  }
}
