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
import uk.gov.hmrc.alcoholdutyreturns.models.returns.{AdrReturnAdjustments, AdrReturnAdjustmentsRow, AdrReturnAlcoholDeclared, AdrReturnAlcoholDeclaredRow, AdrReturnDetails, AdrReturnDetailsIdentification, AdrReturnTotalDutyDue, AlcoholProducts, ChargeDetails, Drawback, IdDetails, NetDutySuspension, NetDutySuspensionProducts, OverDeclaration, RegularReturnDetails, RepackagedDraught, RepackagedDraughtProduct, ReturnDetails, ReturnDetailsSuccess, SpiritsProduced, SpiritsProducedDetails, SpoiltProduct, TotalDutyDue, TotalDutyDuebyTaxType, UnderDeclaration}

import java.time.{Clock, Instant, LocalDate, YearMonth, ZoneId}

trait TestData extends ModelGenerators {
  val clock = Clock.fixed(Instant.ofEpochMilli(1718118467838L), ZoneId.of("UTC"))

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
    dueDate = now.plusDays(2),
    periodKey = "24AA"
  )

  def getFulfilledObligationData(now: LocalDate): ObligationData = getObligationData(now).copy(status = Fulfilled)

  private val booleanTrueString = "1"
  private val adrPeriodStartDay = 1

  private def periodKeyFromDate(periodFrom: LocalDate): String =
    s"${periodFrom.getYear.toString.takeRight(2)}A${(periodFrom.getMonthValue + 64).toChar}"

  private def periodFrom(monthsInThePast: Int, date: LocalDate): LocalDate = {
    val newDate = date.minusMonths(monthsInThePast)
    newDate.withDayOfMonth(adrPeriodStartDay)
  }

  def successfulReturnsExample(appaId: String, periodKey: String, now: Instant): ReturnDetailsSuccess = {
    val periodDate = LocalDate.of(periodKey.take(2).toInt, periodKey.charAt(3) - 'A', 1)

    ReturnDetailsSuccess(
      processingDate = now,
      idDetails = IdDetails(adReference = appaId, submissionID = "123456789123"),
      chargeDetails = ChargeDetails(
        periodKey = periodKey,
        chargeReference = Some(appaId),
        periodFrom = periodDate,
        periodTo = YearMonth.from(periodDate).atEndOfMonth(),
        receiptDate = now
      ),
      alcoholProducts = AlcoholProducts(
        alcoholProductsProducedFilled = booleanTrueString,
        regularReturn = Some(
          Seq(
            RegularReturnDetails(
              taxType = "301",
              dutyRate = BigDecimal("0.05"),
              litresProduced = BigDecimal("0.02"),
              litresOfPureAlcohol = BigDecimal("0.4083"),
              dutyDue = BigDecimal("0.36"),
              productName = Some("whisky")
            )
          )
        )
      ),
      overDeclaration = OverDeclaration(
        overDeclFilled = booleanTrueString,
        reasonForOverDecl = Option("Why over-declared"),
        overDeclaration = Some(
          Seq(
            ReturnDetails(
              returnPeriodAffected = periodKeyFromDate(periodFrom(1, periodDate)),
              taxType = "301",
              dutyRate = BigDecimal("0.3"),
              litresProduced = BigDecimal("0.79"),
              litresOfPureAlcohol = BigDecimal("0.5478"),
              dutyDue = BigDecimal("0.57"),
              productName = Some("gin")
            )
          )
        )
      ),
      underDeclaration = UnderDeclaration(
        underDeclFilled = booleanTrueString,
        reasonForUnderDecl = Option("Why under-declared"),
        underDeclaration = Some(
          Seq(
            ReturnDetails(
              returnPeriodAffected = periodKeyFromDate(periodFrom(2, periodDate)),
              taxType = "301",
              dutyRate = BigDecimal("1.0"),
              litresProduced = BigDecimal("0.78"),
              litresOfPureAlcohol = BigDecimal("0.8989"),
              dutyDue = BigDecimal("0.6"),
              productName = Some("cider")
            )
          )
        )
      ),
      spoiltProduct = SpoiltProduct(
        spoiltProdFilled = booleanTrueString,
        spoiltProduct = Some(
          Seq(
            ReturnDetails(
              returnPeriodAffected = periodKeyFromDate(periodFrom(3, periodDate)),
              taxType = "301",
              dutyRate = BigDecimal("0.75"),
              litresProduced = BigDecimal("0.69"),
              litresOfPureAlcohol = BigDecimal("0.9411"),
              dutyDue = BigDecimal("0.61"),
              productName = Some("red wine")
            )
          )
        )
      ),
      drawback = Drawback(
        drawbackFilled = booleanTrueString,
        drawbackProducts = Some(
          Seq(
            ReturnDetails(
              returnPeriodAffected = periodKeyFromDate(periodFrom(4, periodDate)),
              taxType = "301",
              dutyRate = BigDecimal("0.05"),
              litresProduced = BigDecimal("0.02"),
              litresOfPureAlcohol = BigDecimal("0.1571"),
              dutyDue = BigDecimal("0.12"),
              productName = Some("brandy")
            )
          )
        )
      ),
      repackagedDraught = RepackagedDraught(
        repDraughtFilled = booleanTrueString,
        repackagedDraughtProducts = Some(
          Seq(
            RepackagedDraughtProduct(
              returnPeriodAffected = periodKeyFromDate(periodFrom(5, periodDate)),
              originaltaxType = "300",
              originaldutyRate = BigDecimal("0.64"),
              newTaxType = "301",
              dutyRate = BigDecimal("0.01"),
              litresOfRepackaging = BigDecimal("0.97"),
              litresOfPureAlcohol = BigDecimal("0.0681"),
              dutyDue = BigDecimal("0.68"),
              productName = Some("wine")
            )
          )
        )
      ),
      totalDutyDuebyTaxType =
        Some(Seq(TotalDutyDuebyTaxType(taxType = "301", totalDutyDueTaxType = BigDecimal("1.0")))),
      totalDutyDue = TotalDutyDue(
        totalDutyDueAlcoholProducts = BigDecimal("0.35"),
        totalDutyOverDeclaration = BigDecimal("0.99"),
        totalDutyUnderDeclaration = BigDecimal("0.12"),
        totalDutySpoiltProduct = BigDecimal("0.68"),
        totalDutyDrawback = BigDecimal("0.54"),
        totalDutyRepDraughtProducts = BigDecimal("0.97"),
        totalDutyDue = BigDecimal("0.72")
      ),
      netDutySuspension = NetDutySuspension(
        netDutySuspensionFilled = booleanTrueString,
        netDutySuspensionProducts = Some(
          NetDutySuspensionProducts(
            totalLtsBeer = Some(BigDecimal("0.15")),
            totalLtsWine = Some(BigDecimal("0.44")),
            totalLtsCider = Some(BigDecimal("0.38")),
            totalLtsSpirit = Some(BigDecimal("0.02")),
            totalLtsOtherFermented = Some(BigDecimal("0.02")),
            totalLtsPureAlcoholBeer = Some(BigDecimal("0.4248")),
            totalLtsPureAlcoholWine = Some(BigDecimal("0.5965")),
            totalLtsPureAlcoholCider = Some(BigDecimal("0.0379")),
            totalLtsPureAlcoholSpirit = Some(BigDecimal("0.2492")),
            totalLtsPureAlcoholOtherFermented = Some(BigDecimal("0.1894"))
          )
        )
      ),
      spiritsProduced = Some(
        SpiritsProduced(
          spiritsProdFilled = booleanTrueString,
          spiritsProduced = Some(
            SpiritsProducedDetails(
              totalSpirits = BigDecimal("0.05"),
              scotchWhiskey = BigDecimal("0.26"),
              irishWhisky = BigDecimal("0.16"),
              typeOfSpirit = Seq("03"),
              typeOfSpiritOther = Some("Coco Pops Vodka"),
              code1MaltedBarley = Some(BigDecimal("0.17")),
              code2Other = Some(booleanTrueString),
              maltedGrainQuantity = Some(BigDecimal("0.55")),
              maltedGrainType = Some("wheat"),
              code3Wheat = Some(BigDecimal("0.8")),
              code4Maize = Some(BigDecimal("0.67")),
              code5Rye = Some(BigDecimal("0.13")),
              code6UnmaltedGrain = Some(BigDecimal("0.71")),
              code7EthyleneGas = Some(BigDecimal("0.45")),
              code8Molassess = Some(BigDecimal("0.31")),
              code9Beer = Some(BigDecimal("0.37")),
              code10Wine = Some(BigDecimal("0.76")),
              code11MadeWine = Some(BigDecimal("0.6")),
              code12CiderOrPerry = Some(BigDecimal("0.04")),
              code13Other = Some(booleanTrueString),
              otherMaterialsQuantity = Some(BigDecimal("0.26")),
              otherMaterialUom = Some("01"),
              otherMaterialsType = Some("Coco Pops")
            )
          )
        )
      )
    )
  }

  def processingError(processingDate: Instant): ReturnDetailsProcessingError =
    ReturnDetailsProcessingError(
      ReturnDetailsProcessingErrorInternal(processingDate, "003", "Request could not be processed.")
    )

  val internalServerError: ReturnDetailsInternalServerError =
    ReturnDetailsInternalServerError(
      ReturnDetailsInternalServerErrorInternal(
        "500",
        "Error&#x20;while&#x20;sending&#x20;message&#x20;to&#x20;module&#x20;processor&#x3a;&#x20;Sender&#x20;Channel&#x20;&#x27;CCOS_01_REST_Out_EXCISE_AD_ReturnDisplay_GET&#x27;&#x20;&#x28;ID&#x3a;&#x20;f14c255c0ced3f84852a066298b31ab4&#x29;&#x3a;&#x20;Catching&#x20;exception&#x20;calling&#x20;messaging&#x20;system&#x3a;&#x20;com.sap.aii.af.sdk.xi.srt.BubbleException&#x3a;&#x20;System&#x20;Error&#x20;Received.&#x20;HTTP&#x20;Status&#x20;Code&#x20;&#x3d;&#x20;200&#x3a;&#x20;However&#x20;System&#x20;Error&#x20;received&#x20;in&#x20;payload&#x20;ErrorCode&#x20;&#x3d;&#x20;INCORRECT_PAYLOAD_DATA&#x20;ErrorCategory&#x20;&#x3d;&#x20;XIServer&#x20;Parameter1&#x20;&#x3d;&#x20;&#x20;Parameter2&#x20;&#x3d;&#x20;&#x20;Parameter3&#x20;&#x3d;&#x20;&#x20;Parameter4&#x20;&#x3d;&#x20;&#x20;Additional&#x20;text&#x20;&#x3d;&#x20;&#x20;ErrorStack&#x20;&#x3d;&#x20;Error&#x20;while&#x20;processing&#x20;message&#x20;payload&#xa;&#xa;An&#x20;error&#x20;occurred&#x20;when&#x20;deserializing&#x20;in&#x20;the&#x20;simple&#x20;transformation&#x20;program&#x20;&#x2f;1SAI&#x2f;SAS95E681E8E61376C8825A&#xa;The&#x20;value&#x20;&#x27;9.999999999999E10&#x27;&#x20;is&#x20;not&#x20;in&#x20;the&#x20;value&#x20;range&#x20;of&#x20;the&#x20;XML&#x20;schema&#x20;type&#x20;&#x27;decimal&#x27;&#x20;or&#x20;it&#x20;does&#x20;not&#x20;meet&#x20;the&#x20;specified&#x20;limitations&#xa;&#x20;&#x5b;http&#x3a;&#x2f;&#x2f;sap.com&#x2f;xi&#x2f;XI&#x2f;Message&#x2f;30&#x5e;Error&#x20;&quot;INCORRECT_PAYLOAD_DATA&quot;&#x5d;",
        "C0000AB8190CB66000000003000007A6"
      )
    )

  def convertedReturnDetails(periodKey: String, now: Instant): AdrReturnDetails =
    AdrReturnDetails(
      identification = AdrReturnDetailsIdentification(periodKey = periodKey, submittedTime = now),
      alcoholDeclared = AdrReturnAlcoholDeclared(
        alcoholDeclaredDetails = Some(
          Seq(
            AdrReturnAlcoholDeclaredRow(
              taxType = "301",
              litresOfPureAlcohol = BigDecimal("0.4083"),
              dutyRate = BigDecimal("0.05"),
              dutyValue = BigDecimal("0.36")
            )
          )
        ),
        total = BigDecimal("0.35")
      ),
      adjustments = AdrReturnAdjustments(
        adjustmentDetails = Some(
          Seq(
            AdrReturnAdjustmentsRow(
              adjustmentTypeKey = "underdeclaration",
              taxType = "301",
              litresOfPureAlcohol = BigDecimal("0.8989"),
              dutyRate = BigDecimal("1.0"),
              dutyValue = BigDecimal("0.6")
            ),
            AdrReturnAdjustmentsRow(
              adjustmentTypeKey = "overdeclaration",
              taxType = "301",
              litresOfPureAlcohol = BigDecimal("0.5478"),
              dutyRate = BigDecimal("0.3"),
              dutyValue = BigDecimal("0.57")
            ),
            AdrReturnAdjustmentsRow(
              adjustmentTypeKey = "repackagedDraught",
              taxType = "301",
              litresOfPureAlcohol = BigDecimal("0.0681"),
              dutyRate = BigDecimal("0.01"),
              dutyValue = BigDecimal("0.68")
            ),
            AdrReturnAdjustmentsRow(
              adjustmentTypeKey = "spoilt",
              taxType = "301",
              litresOfPureAlcohol = BigDecimal("0.9411"),
              dutyRate = BigDecimal("0.75"),
              dutyValue = BigDecimal("0.61")
            ),
            AdrReturnAdjustmentsRow(
              adjustmentTypeKey = "drawback",
              taxType = "301",
              litresOfPureAlcohol = BigDecimal("0.1571"),
              dutyRate = BigDecimal("0.05"),
              dutyValue = BigDecimal("0.12")
            )
          )
        ),
        total = BigDecimal("3.30")
      ),
      AdrReturnTotalDutyDue(
        totalDue = BigDecimal("0.72")
      )
    )

  def exampleReturnDetails(periodKey: String, now: Instant): AdrReturnDetails =
    AdrReturnDetails(
      identification = AdrReturnDetailsIdentification(periodKey = periodKey, submittedTime = now),
      alcoholDeclared = AdrReturnAlcoholDeclared(
        alcoholDeclaredDetails = Some(
          Seq(
            AdrReturnAlcoholDeclaredRow(
              taxType = "311",
              litresOfPureAlcohol = BigDecimal(450),
              dutyRate = BigDecimal("9.27"),
              dutyValue = BigDecimal("4171.50")
            ),
            AdrReturnAlcoholDeclaredRow(
              taxType = "321",
              litresOfPureAlcohol = BigDecimal(450),
              dutyRate = BigDecimal("21.01"),
              dutyValue = BigDecimal("9454.50")
            ),
            AdrReturnAlcoholDeclaredRow(
              taxType = "331",
              litresOfPureAlcohol = BigDecimal(450),
              dutyRate = BigDecimal("28.50"),
              dutyValue = BigDecimal("12825.00")
            ),
            AdrReturnAlcoholDeclaredRow(
              taxType = "341",
              litresOfPureAlcohol = BigDecimal(450),
              dutyRate = BigDecimal("31.64"),
              dutyValue = BigDecimal("14238.00")
            ),
            AdrReturnAlcoholDeclaredRow(
              taxType = "351",
              litresOfPureAlcohol = BigDecimal(450),
              dutyRate = BigDecimal("8.42"),
              dutyValue = BigDecimal("3789.00")
            ),
            AdrReturnAlcoholDeclaredRow(
              taxType = "356",
              litresOfPureAlcohol = BigDecimal(450),
              dutyRate = BigDecimal("19.08"),
              dutyValue = BigDecimal("8586.00")
            ),
            AdrReturnAlcoholDeclaredRow(
              taxType = "361",
              litresOfPureAlcohol = BigDecimal(450),
              dutyRate = BigDecimal("8.40"),
              dutyValue = BigDecimal("3780.00")
            ),
            AdrReturnAlcoholDeclaredRow(
              taxType = "366",
              litresOfPureAlcohol = BigDecimal(450),
              dutyRate = BigDecimal("16.47"),
              dutyValue = BigDecimal("7411.50")
            ),
            AdrReturnAlcoholDeclaredRow(
              taxType = "371",
              litresOfPureAlcohol = BigDecimal(450),
              dutyRate = BigDecimal("8.20"),
              dutyValue = BigDecimal("3960.00")
            ),
            AdrReturnAlcoholDeclaredRow(
              taxType = "376",
              litresOfPureAlcohol = BigDecimal(450),
              dutyRate = BigDecimal("15.63"),
              dutyValue = BigDecimal("7033.50")
            )
          )
        ),
        total = BigDecimal("75249.00")
      ),
      adjustments = AdrReturnAdjustments(
        adjustmentDetails = Some(
          Seq(
            AdrReturnAdjustmentsRow(
              adjustmentTypeKey = AdrReturnAdjustments.underDeclaredKey,
              taxType = "321",
              litresOfPureAlcohol = BigDecimal(150),
              dutyRate = BigDecimal("21.01"),
              dutyValue = BigDecimal("3151.50")
            ),
            AdrReturnAdjustmentsRow(
              adjustmentTypeKey = AdrReturnAdjustments.spoiltKey,
              taxType = "321",
              litresOfPureAlcohol = BigDecimal(1150),
              dutyRate = BigDecimal("21.01"),
              dutyValue = BigDecimal("-24161.50")
            ),
            AdrReturnAdjustmentsRow(
              adjustmentTypeKey = AdrReturnAdjustments.spoiltKey,
              taxType = "321",
              litresOfPureAlcohol = BigDecimal(75),
              dutyRate = BigDecimal("21.01"),
              dutyValue = BigDecimal("-1575.50")
            ),
            AdrReturnAdjustmentsRow(
              adjustmentTypeKey = AdrReturnAdjustments.repackagedDraughtKey,
              taxType = "321",
              litresOfPureAlcohol = BigDecimal(150),
              dutyRate = BigDecimal("21.01"),
              dutyValue = BigDecimal("3151.50")
            )
          )
        ),
        total = BigDecimal("-19434")
      ),
      totalDutyDue = AdrReturnTotalDutyDue(totalDue = BigDecimal("55815"))
    )
}
