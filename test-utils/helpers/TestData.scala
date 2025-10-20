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
import uk.gov.hmrc.alcoholdutyreturns.models.ObligationStatus.{Fulfilled, Open}
import uk.gov.hmrc.alcoholdutyreturns.models._
import uk.gov.hmrc.alcoholdutyreturns.models.calculation._
import uk.gov.hmrc.alcoholdutyreturns.models.returns._

import java.time._

trait TestData extends ModelGenerators {
  val clockMillis: Long = 1718118467838L
  val clock: Clock      = Clock.fixed(Instant.ofEpochMilli(clockMillis), ZoneId.of("UTC"))

  val regime: String          = "AD"
  val appaId: String          = appaIdGen.sample.get
  val periodKey: String       = periodKeyGen.sample.get
  val groupId: String         = "groupId"
  val internalId: String      = "internalId"
  val returnId: ReturnId      = ReturnId(appaId, periodKey)
  val submissionId: String    = submissionIdGen.sample.get
  val chargeReference: String = chargeReferenceGen.sample.get
  val paymentDueDate: String  = "2024-02-25"

  val returnAndUserDetails: ReturnAndUserDetails = ReturnAndUserDetails(returnId, groupId, internalId)

  val alcoholRegimes: AlcoholRegimes    = AlcoholRegimes(Set(Beer, Wine))
  val allAlcoholRegimes: AlcoholRegimes = AlcoholRegimes(Set(Beer, Cider, Spirits, Wine, OtherFermentedProduct))

  val subscriptionSummary: SubscriptionSummary = SubscriptionSummary(Approved, allAlcoholRegimes.regimes)

  val emptyUserAnswers: UserAnswers = UserAnswers(
    returnId,
    groupId,
    internalId,
    allAlcoholRegimes,
    startedTime = Instant.now(clock),
    lastUpdated = Instant.now(clock)
  )

  val userAnswers: UserAnswers = UserAnswers(
    returnId,
    groupId,
    internalId,
    allAlcoholRegimes,
    JsObject(Seq(ObligationData.toString -> Json.toJson(getObligationData(LocalDate.now(clock))))),
    startedTime = Instant.now(clock),
    lastUpdated = Instant.now(clock)
  )

  def getObligationData(now: LocalDate): ObligationData = ObligationData(
    status = Open,
    fromDate = now,
    toDate = now.plusDays(1),
    dueDate = now.plusDays(2),
    periodKey = "24AA"
  )

  val fulfilledObligations2024: FulfilledObligations =
    FulfilledObligations(
      year = 2024,
      obligations = Seq(
        getObligationData(LocalDate.of(2024, 12, 31)).copy(status = Fulfilled),
        getObligationData(LocalDate.of(2024, 11, 30)).copy(status = Fulfilled)
      )
    )

  val emptyFulfilledObligations: FulfilledObligations = FulfilledObligations(2023, Seq.empty)

  val fulfilledObligationData = Seq(fulfilledObligations2024, emptyFulfilledObligations)

  private val adrPeriodStartDay = 1

  private def periodKeyFromDate(periodFrom: LocalDate): String =
    s"${periodFrom.getYear.toString.takeRight(2)}A${(periodFrom.getMonthValue + 64).toChar}"

  private def periodFrom(monthsInThePast: Int, date: LocalDate): LocalDate = {
    val newDate = date.minusMonths(monthsInThePast)
    newDate.withDayOfMonth(adrPeriodStartDay)
  }

  def successfulReturnExample(
    appaId: String,
    periodKey: String,
    submissionId: String,
    chargeReference: String,
    now: Instant
  ): GetReturnDetailsSuccess = {
    val periodDate = LocalDate.of(periodKey.take(2).toInt + 2000, periodKey.charAt(3) - 'A' + 1, 1)

    GetReturnDetailsSuccess(
      GetReturnDetails(
        processingDate = now,
        idDetails = IdDetails(adReference = appaId, submissionID = submissionId),
        chargeDetails = ChargeDetails(
          periodKey = periodKey,
          chargeReference = Some(chargeReference),
          periodFrom = periodDate,
          periodTo = YearMonth.from(periodDate).atEndOfMonth(),
          receiptDate = now
        ),
        alcoholProducts = AlcoholProducts(
          alcoholProductsProducedFilled = true,
          regularReturn = Some(
            Seq(
              RegularReturnDetails(
                taxType = "301",
                dutyRate = BigDecimal("5.27"),
                litresProduced = BigDecimal("240000.02"),
                litresOfPureAlcohol = BigDecimal("12041.00"),
                dutyDue = BigDecimal("63456.07"),
                productName = None
              )
            )
          )
        ),
        overDeclaration = OverDeclaration(
          overDeclFilled = true,
          reasonForOverDecl = Some("Why over-declared"),
          overDeclarationProducts = Some(
            Seq(
              ReturnDetails(
                returnPeriodAffected = Some(periodKeyFromDate(periodFrom(1, periodDate))),
                taxType = "302",
                dutyRate = Some(BigDecimal("3.56")),
                litresProduced = BigDecimal("5000.79"),
                litresOfPureAlcohol = BigDecimal("100.58"),
                dutyDue = BigDecimal("358.07"),
                productName = None
              )
            )
          )
        ),
        underDeclaration = UnderDeclaration(
          underDeclFilled = true,
          reasonForUnderDecl = Some("Why under-declared"),
          underDeclarationProducts = Some(
            Seq(
              ReturnDetails(
                returnPeriodAffected = Some(periodKeyFromDate(periodFrom(2, periodDate))),
                taxType = "301",
                dutyRate = Some(BigDecimal("5.27")),
                litresProduced = BigDecimal("49000.78"),
                litresOfPureAlcohol = BigDecimal("989"),
                dutyDue = BigDecimal("5212.03"),
                productName = None
              )
            )
          )
        ),
        spoiltProduct = SpoiltProduct(
          spoiltProdFilled = true,
          spoiltProductProducts = Some(
            Seq(
              ReturnDetails(
                returnPeriodAffected = None,
                taxType = "305",
                dutyRate = None,
                litresProduced = BigDecimal("50000.69"),
                litresOfPureAlcohol = BigDecimal("1000.94"),
                dutyDue = BigDecimal("1751.65"),
                productName = None
              )
            )
          )
        ),
        drawback = Drawback(
          drawbackFilled = true,
          drawbackProducts = Some(
            Seq(
              ReturnDetails(
                returnPeriodAffected = Some(periodKeyFromDate(periodFrom(4, periodDate))),
                taxType = "309",
                dutyRate = Some(BigDecimal("5.12")),
                litresProduced = BigDecimal("60000.02"),
                litresOfPureAlcohol = BigDecimal("1301.11"),
                dutyDue = BigDecimal("6661.69"),
                productName = None
              )
            )
          )
        ),
        repackagedDraught = RepackagedDraught(
          repDraughtFilled = true,
          repackagedDraughtProducts = Some(
            Seq(
              RepackagedDraughtProduct(
                returnPeriodAffected = periodKeyFromDate(periodFrom(5, periodDate)),
                originaltaxType = "300",
                originaldutyRate = BigDecimal("0.64"),
                newTaxType = "304",
                dutyRate = BigDecimal("12.76"),
                litresOfRepackaging = BigDecimal("5000.97"),
                litresOfPureAlcohol = BigDecimal("100.81"),
                dutyDue = BigDecimal("1221.82"),
                productName = None
              )
            )
          )
        ),
        totalDutyDuebyTaxType =
          Some(Seq(TotalDutyDuebyTaxType(taxType = "301", totalDutyDueTaxType = BigDecimal("1.0")))),
        totalDutyDue = TotalDutyDue(
          totalDutyDueAlcoholProducts = BigDecimal("63456.07"),
          totalDutyOverDeclaration = BigDecimal("358.07"),
          totalDutyUnderDeclaration = BigDecimal("5212.03"),
          totalDutySpoiltProduct = BigDecimal("1751.65"),
          totalDutyDrawback = BigDecimal("6661.69"),
          totalDutyRepDraughtProducts = BigDecimal("1221.82"),
          totalDutyDue = BigDecimal("61118.51")
        ),
        netDutySuspension = NetDutySuspension(
          netDutySuspensionFilled = true,
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
            spiritsProdFilled = true,
            spiritsProduced = Some(
              SpiritsProducedDetails(
                totalSpirits = BigDecimal("0.05"),
                scotchWhiskey = BigDecimal("0.26"),
                irishWhisky = BigDecimal("0.16"),
                typeOfSpirit = Set(TypeOfSpiritType.NeutralSpiritAgricultural),
                typeOfSpiritOther = Some("Coco Pops Vodka")
              )
            )
          )
        )
      )
    )
  }

  /* This shouldn't occur, but is supported by the schema */
  def nilReturnWithPresentItemSequencesExample(
    appaId: String,
    periodKey: String,
    submissionId: String,
    now: Instant
  ): GetReturnDetailsSuccess = {
    val periodDate = LocalDate.of(periodKey.take(2).toInt + 2000, periodKey.charAt(3) - 'A' + 1, 1)

    GetReturnDetailsSuccess(
      GetReturnDetails(
        processingDate = now,
        idDetails = IdDetails(adReference = appaId, submissionID = submissionId),
        chargeDetails = ChargeDetails(
          periodKey = periodKey,
          chargeReference = None,
          periodFrom = periodDate,
          periodTo = YearMonth.from(periodDate).atEndOfMonth(),
          receiptDate = now
        ),
        alcoholProducts = AlcoholProducts(
          alcoholProductsProducedFilled = false,
          regularReturn = Some(Seq.empty)
        ),
        overDeclaration = OverDeclaration(
          overDeclFilled = false,
          reasonForOverDecl = None,
          overDeclarationProducts = Some(Seq.empty)
        ),
        underDeclaration = UnderDeclaration(
          underDeclFilled = false,
          reasonForUnderDecl = None,
          underDeclarationProducts = Some(Seq.empty)
        ),
        spoiltProduct = SpoiltProduct(
          spoiltProdFilled = false,
          spoiltProductProducts = Some(Seq.empty)
        ),
        drawback = Drawback(
          drawbackFilled = false,
          drawbackProducts = Some(Seq.empty)
        ),
        repackagedDraught = RepackagedDraught(
          repDraughtFilled = false,
          repackagedDraughtProducts = Some(Seq.empty)
        ),
        totalDutyDuebyTaxType = None,
        totalDutyDue = TotalDutyDue(
          totalDutyDueAlcoholProducts = BigDecimal(0),
          totalDutyOverDeclaration = BigDecimal(0),
          totalDutyUnderDeclaration = BigDecimal(0),
          totalDutySpoiltProduct = BigDecimal(0),
          totalDutyDrawback = BigDecimal(0),
          totalDutyRepDraughtProducts = BigDecimal(0),
          totalDutyDue = BigDecimal(0)
        ),
        netDutySuspension = NetDutySuspension(
          netDutySuspensionFilled = false,
          netDutySuspensionProducts = None
        ),
        spiritsProduced = None
      )
    )
  }

  def nilReturnExample(
    appaId: String,
    periodKey: String,
    submissionId: String,
    now: Instant
  ): GetReturnDetailsSuccess = {
    val periodDate = LocalDate.of(periodKey.take(2).toInt + 2000, periodKey.charAt(3) - 'A' + 1, 1)

    GetReturnDetailsSuccess(
      GetReturnDetails(
        processingDate = now,
        idDetails = IdDetails(adReference = appaId, submissionID = submissionId),
        chargeDetails = ChargeDetails(
          periodKey = periodKey,
          chargeReference = None,
          periodFrom = periodDate,
          periodTo = YearMonth.from(periodDate).atEndOfMonth(),
          receiptDate = now
        ),
        alcoholProducts = AlcoholProducts(
          alcoholProductsProducedFilled = false,
          regularReturn = None
        ),
        overDeclaration = OverDeclaration(
          overDeclFilled = false,
          reasonForOverDecl = None,
          overDeclarationProducts = None
        ),
        underDeclaration = UnderDeclaration(
          underDeclFilled = false,
          reasonForUnderDecl = None,
          underDeclarationProducts = None
        ),
        spoiltProduct = SpoiltProduct(
          spoiltProdFilled = false,
          spoiltProductProducts = None
        ),
        drawback = Drawback(
          drawbackFilled = false,
          drawbackProducts = None
        ),
        repackagedDraught = RepackagedDraught(
          repDraughtFilled = false,
          repackagedDraughtProducts = None
        ),
        totalDutyDuebyTaxType = None,
        totalDutyDue = TotalDutyDue(
          totalDutyDueAlcoholProducts = BigDecimal(0),
          totalDutyOverDeclaration = BigDecimal(0),
          totalDutyUnderDeclaration = BigDecimal(0),
          totalDutySpoiltProduct = BigDecimal(0),
          totalDutyDrawback = BigDecimal(0),
          totalDutyRepDraughtProducts = BigDecimal(0),
          totalDutyDue = BigDecimal(0)
        ),
        netDutySuspension = NetDutySuspension(
          netDutySuspensionFilled = false,
          netDutySuspensionProducts = None
        ),
        spiritsProduced = None
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
        "Something went wrong",
        "C0000AB8190CB66000000003000007A6"
      )
    )

  val badGateway: ReturnDetailsInternalServerError =
    ReturnDetailsInternalServerError(
      ReturnDetailsInternalServerErrorInternal(
        "502",
        "Bad gateway",
        "C0000AB8190CB66000000003000007A6"
      )
    )

  val serviceUnavailable: ReturnDetailsInternalServerError =
    ReturnDetailsInternalServerError(
      ReturnDetailsInternalServerErrorInternal(
        "503",
        "Service unavailable",
        "C0000AB8190CB66000000003000007A6"
      )
    )

  val gatewayTimeout: ReturnDetailsInternalServerError =
    ReturnDetailsInternalServerError(
      ReturnDetailsInternalServerErrorInternal(
        "504",
        "Gateway timeout",
        "C0000AB8190CB66000000003000007A6"
      )
    )

  def convertedReturnDetails(periodKey: String, now: Instant): AdrReturnDetails = {
    val periodDate = LocalDate.of(periodKey.take(2).toInt + 2000, periodKey.charAt(3) - 'A' + 1, 1)

    AdrReturnDetails(
      identification = AdrReturnDetailsIdentification(
        periodKey = periodKey,
        chargeReference = Some(chargeReference),
        submittedTime = now
      ),
      alcoholDeclared = AdrReturnAlcoholDeclared(
        alcoholDeclaredDetails = Some(
          Seq(
            AdrReturnAlcoholDeclaredRow(
              taxType = "301",
              litresOfPureAlcohol = BigDecimal("12041"),
              dutyRate = BigDecimal("5.27"),
              dutyValue = BigDecimal("63456.07")
            )
          )
        ),
        total = BigDecimal("63456.07")
      ),
      adjustments = AdrReturnAdjustments(
        adjustmentDetails = Some(
          Seq(
            AdrReturnAdjustmentsRow(
              adjustmentTypeKey = "underdeclaration",
              returnPeriodAffected = Some(periodKeyFromDate(periodFrom(2, periodDate))),
              taxType = "301",
              litresOfPureAlcohol = BigDecimal("989"),
              dutyRate = Some(BigDecimal("5.27")),
              dutyValue = BigDecimal("5212.03")
            ),
            AdrReturnAdjustmentsRow(
              adjustmentTypeKey = "overdeclaration",
              returnPeriodAffected = Some(periodKeyFromDate(periodFrom(1, periodDate))),
              taxType = "302",
              litresOfPureAlcohol = BigDecimal("100.58"),
              dutyRate = Some(BigDecimal("3.56")),
              dutyValue = BigDecimal("-358.07")
            ),
            AdrReturnAdjustmentsRow(
              adjustmentTypeKey = "repackagedDraught",
              returnPeriodAffected = Some(periodKeyFromDate(periodFrom(5, periodDate))),
              taxType = "304",
              litresOfPureAlcohol = BigDecimal("100.81"),
              dutyRate = Some(BigDecimal("12.76")),
              dutyValue = BigDecimal("1221.82")
            ),
            AdrReturnAdjustmentsRow(
              adjustmentTypeKey = "spoilt",
              returnPeriodAffected = None,
              taxType = "305",
              litresOfPureAlcohol = BigDecimal("1000.94"),
              dutyRate = None,
              dutyValue = BigDecimal("-1751.65")
            ),
            AdrReturnAdjustmentsRow(
              adjustmentTypeKey = "drawback",
              returnPeriodAffected = Some(periodKeyFromDate(periodFrom(4, periodDate))),
              taxType = "309",
              litresOfPureAlcohol = BigDecimal("1301.11"),
              dutyRate = Some(BigDecimal("5.12")),
              dutyValue = BigDecimal("-6661.69")
            )
          )
        ),
        total = BigDecimal("-2337.56")
      ),
      AdrReturnTotalDutyDue(
        totalDue = BigDecimal("61118.51")
      ),
      Some(
        AdrReturnNetDutySuspension(
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
      ),
      spirits = Some(
        AdrReturnSpirits(
          AdrReturnSpiritsVolumes(
            totalSpirits = BigDecimal("0.05"),
            scotchWhisky = BigDecimal("0.26"),
            irishWhiskey = BigDecimal("0.16")
          ),
          typesOfSpirit = Set(AdrTypeOfSpirit.NeutralAgricultural),
          otherSpiritTypeName = Some("Coco Pops Vodka")
        )
      )
    )
  }

  def exampleReturnDetails(periodKey: String, now: Instant): AdrReturnDetails = {
    val periodDate = LocalDate.of(periodKey.take(2).toInt + 2000, periodKey.charAt(3) - 'A' + 1, 1)

    AdrReturnDetails(
      identification = AdrReturnDetailsIdentification(
        periodKey = periodKey,
        chargeReference = Some(chargeReference),
        submittedTime = now
      ),
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
              returnPeriodAffected = Some(periodKeyFromDate(periodFrom(1, periodDate))),
              taxType = "321",
              litresOfPureAlcohol = BigDecimal(150),
              dutyRate = Some(BigDecimal("21.01")),
              dutyValue = BigDecimal("3151.50")
            ),
            AdrReturnAdjustmentsRow(
              adjustmentTypeKey = AdrReturnAdjustments.spoiltKey,
              returnPeriodAffected = None,
              taxType = "321",
              litresOfPureAlcohol = BigDecimal(1150),
              dutyRate = None,
              dutyValue = BigDecimal("-24161.50")
            ),
            AdrReturnAdjustmentsRow(
              adjustmentTypeKey = AdrReturnAdjustments.spoiltKey,
              returnPeriodAffected = Some(periodKeyFromDate(periodFrom(3, periodDate))),
              taxType = "321",
              litresOfPureAlcohol = BigDecimal(75),
              dutyRate = Some(BigDecimal("21.01")),
              dutyValue = BigDecimal("-1575.50")
            ),
            AdrReturnAdjustmentsRow(
              adjustmentTypeKey = AdrReturnAdjustments.repackagedDraughtKey,
              returnPeriodAffected = Some(periodKeyFromDate(periodFrom(4, periodDate))),
              taxType = "321",
              litresOfPureAlcohol = BigDecimal(150),
              dutyRate = Some(BigDecimal("21.01")),
              dutyValue = BigDecimal("3151.50")
            )
          )
        ),
        total = BigDecimal("-19434")
      ),
      totalDutyDue = AdrReturnTotalDutyDue(totalDue = BigDecimal("55815")),
      netDutySuspension = Some(
        AdrReturnNetDutySuspension(
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
      ),
      spirits = Some(
        AdrReturnSpirits(
          AdrReturnSpiritsVolumes(
            totalSpirits = BigDecimal("0.05"),
            scotchWhisky = BigDecimal("0.26"),
            irishWhiskey = BigDecimal("0.16")
          ),
          typesOfSpirit = Set(AdrTypeOfSpirit.NeutralAgricultural),
          otherSpiritTypeName = Some("Coco Pops Vodka")
        )
      )
    )
  }

  def nilReturnDetails(periodKey: String, now: Instant): AdrReturnDetails =
    AdrReturnDetails(
      identification =
        AdrReturnDetailsIdentification(periodKey = periodKey, chargeReference = None, submittedTime = now),
      alcoholDeclared = AdrReturnAlcoholDeclared(
        alcoholDeclaredDetails = None,
        total = BigDecimal(0)
      ),
      adjustments = AdrReturnAdjustments(
        adjustmentDetails = None,
        total = BigDecimal(0)
      ),
      totalDutyDue = AdrReturnTotalDutyDue(totalDue = BigDecimal(0)),
      netDutySuspension = None,
      spirits = None
    )

  val exampleReturnSubmissionRequest: AdrReturnSubmission = AdrReturnSubmission(
    dutyDeclared = AdrDutyDeclared(
      declared = true,
      dutyDeclaredItems = Seq(
        AdrDutyDeclaredItem(
          quantityDeclared = AdrAlcoholQuantity(
            litres = BigDecimal("1000.10"),
            lpa = BigDecimal("100.1010")
          ),
          dutyDue = AdrDuty(
            taxCode = "331",
            dutyRate = BigDecimal("1.27"),
            dutyDue = BigDecimal("127.12")
          )
        ),
        AdrDutyDeclaredItem(
          quantityDeclared = AdrAlcoholQuantity(
            litres = BigDecimal("2000.21"),
            lpa = BigDecimal("200.2022")
          ),
          dutyDue = AdrDuty(
            taxCode = "332",
            dutyRate = BigDecimal("1.57"),
            dutyDue = BigDecimal("314.31")
          )
        )
      )
    ),
    adjustments = AdrAdjustments(
      overDeclarationDeclared = true,
      reasonForOverDeclaration = Some("Submitted too much"),
      overDeclarationProducts = Seq(
        AdrAdjustmentItem(
          returnPeriod = "24AD",
          adjustmentQuantity = AdrAlcoholQuantity(
            litres = BigDecimal("400.04"),
            lpa = BigDecimal("40.0404")
          ),
          dutyDue = AdrDuty(
            taxCode = "352",
            dutyRate = BigDecimal("1.32"),
            dutyDue = BigDecimal("-52.85")
          )
        )
      ),
      underDeclarationDeclared = true,
      reasonForUnderDeclaration = Some("Submitted too little"),
      underDeclarationProducts = Seq(
        AdrAdjustmentItem(
          returnPeriod = "24AC",
          adjustmentQuantity = AdrAlcoholQuantity(
            litres = BigDecimal("300.03"),
            lpa = BigDecimal("30.0303")
          ),
          dutyDue = AdrDuty(
            taxCode = "351",
            dutyRate = BigDecimal("2.32"),
            dutyDue = BigDecimal("69.67")
          )
        )
      ),
      spoiltProductDeclared = true,
      spoiltProducts = Seq(
        AdrAdjustmentItem(
          returnPeriod = "24AE",
          adjustmentQuantity = AdrAlcoholQuantity(
            litres = BigDecimal("500.05"),
            lpa = BigDecimal("50.0505")
          ),
          dutyDue = AdrDuty(
            taxCode = "353",
            dutyRate = BigDecimal("1.82"),
            dutyDue = BigDecimal("-91.09")
          )
        )
      ),
      drawbackDeclared = true,
      drawbackProducts = Seq(
        AdrAdjustmentItem(
          returnPeriod = "24AF",
          adjustmentQuantity = AdrAlcoholQuantity(
            litres = BigDecimal("600.06"),
            lpa = BigDecimal("60.0606")
          ),
          dutyDue = AdrDuty(
            taxCode = "361",
            dutyRate = BigDecimal("2.21"),
            dutyDue = BigDecimal("-132.73")
          )
        )
      ),
      repackagedDraughtDeclared = true,
      repackagedDraughtProducts = Seq(
        AdrRepackagedDraughtAdjustmentItem(
          returnPeriod = "24AG",
          originalTaxCode = "371",
          originalDutyRate = BigDecimal("0.27"),
          newTaxCode = "331",
          newDutyRate = BigDecimal("1.27"),
          repackagedQuantity = AdrAlcoholQuantity(
            litres = BigDecimal("700.07"),
            lpa = BigDecimal("70.0707")
          ),
          dutyAdjustment = BigDecimal("70.07")
        )
      )
    ),
    dutySuspended = AdrDutySuspended(
      declared = true,
      dutySuspendedProducts = Seq(
        AdrDutySuspendedProduct(
          regime = AdrDutySuspendedAlcoholRegime.Beer,
          suspendedQuantity = AdrAlcoholQuantity(
            litres = BigDecimal("101.10"),
            lpa = BigDecimal("1010.1011")
          )
        ),
        AdrDutySuspendedProduct(
          regime = AdrDutySuspendedAlcoholRegime.Wine,
          suspendedQuantity = AdrAlcoholQuantity(
            litres = BigDecimal("202.20"),
            lpa = BigDecimal("2020.2022")
          )
        ),
        AdrDutySuspendedProduct(
          regime = AdrDutySuspendedAlcoholRegime.Cider,
          suspendedQuantity = AdrAlcoholQuantity(
            litres = BigDecimal("303.30"),
            lpa = BigDecimal("3030.3033")
          )
        ),
        AdrDutySuspendedProduct(
          regime = AdrDutySuspendedAlcoholRegime.Spirits,
          suspendedQuantity = AdrAlcoholQuantity(
            litres = BigDecimal("404.40"),
            lpa = BigDecimal("4040.4044")
          )
        ),
        AdrDutySuspendedProduct(
          regime = AdrDutySuspendedAlcoholRegime.OtherFermentedProduct,
          suspendedQuantity = AdrAlcoholQuantity(
            litres = BigDecimal("505.50"),
            lpa = BigDecimal("5050.5055")
          )
        )
      )
    ),
    spirits = Some(
      AdrSpirits(
        spiritsDeclared = true,
        spiritsProduced = Some(
          AdrSpiritsProduced(
            spiritsVolumes = AdrSpiritsVolumes(
              totalSpirits = BigDecimal("123.45"),
              scotchWhisky = BigDecimal("234.56"),
              irishWhiskey = BigDecimal("345.67")
            ),
            typesOfSpirit = Set(AdrTypeOfSpirit.Malt, AdrTypeOfSpirit.Beer, AdrTypeOfSpirit.Other),
            otherSpiritTypeName = Some("MaltyBeer")
          )
        )
      )
    ),
    totals = AdrTotals(
      declaredDutyDue = BigDecimal("441.53"),
      overDeclaration = BigDecimal("-52.85"),
      underDeclaration = BigDecimal("69.67"),
      spoiltProduct = BigDecimal("-91.09"),
      drawback = BigDecimal("-132.73"),
      repackagedDraught = BigDecimal("70.07"),
      totalDutyDue = BigDecimal("304.60")
    )
  )

  val exampleNilReturnSubmissionRequest: AdrReturnSubmission = AdrReturnSubmission(
    dutyDeclared = AdrDutyDeclared(
      declared = false,
      dutyDeclaredItems = Seq.empty
    ),
    adjustments = AdrAdjustments(
      overDeclarationDeclared = false,
      reasonForOverDeclaration = None,
      overDeclarationProducts = Seq.empty,
      underDeclarationDeclared = false,
      reasonForUnderDeclaration = None,
      underDeclarationProducts = Seq.empty,
      spoiltProductDeclared = false,
      spoiltProducts = Seq.empty,
      drawbackDeclared = false,
      drawbackProducts = Seq.empty,
      repackagedDraughtDeclared = false,
      repackagedDraughtProducts = Seq.empty
    ),
    dutySuspended = AdrDutySuspended(
      declared = false,
      dutySuspendedProducts = Seq.empty
    ),
    spirits = None,
    totals = AdrTotals(
      declaredDutyDue = BigDecimal(0),
      overDeclaration = BigDecimal(0),
      underDeclaration = BigDecimal(0),
      spoiltProduct = BigDecimal(0),
      drawback = BigDecimal(0),
      repackagedDraught = BigDecimal(0),
      totalDutyDue = BigDecimal(0)
    )
  )

  def returnCreateSubmission(periodKey: String): ReturnCreate =
    ReturnCreate(
      periodKey = periodKey,
      alcoholProducts = AlcoholProducts(
        alcoholProductsProducedFilled = true,
        regularReturn = Some(
          Seq(
            RegularReturnDetails(
              taxType = "331",
              dutyRate = BigDecimal("1.27"),
              litresProduced = BigDecimal("1000.10"),
              litresOfPureAlcohol = BigDecimal("100.1010"),
              dutyDue = BigDecimal("127.12"),
              productName = None
            ),
            RegularReturnDetails(
              taxType = "332",
              dutyRate = BigDecimal("1.57"),
              litresProduced = BigDecimal("2000.21"),
              litresOfPureAlcohol = BigDecimal("200.2022"),
              dutyDue = BigDecimal("314.31"),
              productName = None
            )
          )
        )
      ),
      overDeclaration = OverDeclaration(
        overDeclFilled = true,
        reasonForOverDecl = Some("Submitted too much"),
        overDeclarationProducts = Some(
          Seq(
            ReturnDetails(
              returnPeriodAffected = Some("24AD"),
              taxType = "352",
              dutyRate = Some(BigDecimal("1.32")),
              litresProduced = BigDecimal("400.04"),
              litresOfPureAlcohol = BigDecimal("40.0404"),
              dutyDue = BigDecimal("52.85"),
              productName = None
            )
          )
        )
      ),
      underDeclaration = UnderDeclaration(
        underDeclFilled = true,
        reasonForUnderDecl = Some("Submitted too little"),
        underDeclarationProducts = Some(
          Seq(
            ReturnDetails(
              returnPeriodAffected = Some("24AC"),
              taxType = "351",
              dutyRate = Some(BigDecimal("2.32")),
              litresProduced = BigDecimal("300.03"),
              litresOfPureAlcohol = BigDecimal("30.0303"),
              dutyDue = BigDecimal("69.67"),
              productName = None
            )
          )
        )
      ),
      spoiltProduct = SpoiltProduct(
        spoiltProdFilled = true,
        spoiltProductProducts = Some(
          Seq(
            ReturnDetails(
              returnPeriodAffected = Some("24AE"),
              taxType = "353",
              dutyRate = Some(BigDecimal("1.82")),
              litresProduced = BigDecimal("500.05"),
              litresOfPureAlcohol = BigDecimal("50.0505"),
              dutyDue = BigDecimal("91.09"),
              productName = None
            )
          )
        )
      ),
      drawback = Drawback(
        drawbackFilled = true,
        drawbackProducts = Some(
          Seq(
            ReturnDetails(
              returnPeriodAffected = Some("24AF"),
              taxType = "361",
              dutyRate = Some(BigDecimal("2.21")),
              litresProduced = BigDecimal("600.06"),
              litresOfPureAlcohol = BigDecimal("60.0606"),
              dutyDue = BigDecimal("132.73"),
              productName = None
            )
          )
        )
      ),
      repackagedDraught = RepackagedDraught(
        repDraughtFilled = true,
        repackagedDraughtProducts = Some(
          Seq(
            RepackagedDraughtProduct(
              returnPeriodAffected = "24AG",
              originaltaxType = "371",
              originaldutyRate = BigDecimal("0.27"),
              newTaxType = "331",
              dutyRate = BigDecimal("1.27"),
              litresOfRepackaging = BigDecimal("700.07"),
              litresOfPureAlcohol = BigDecimal("70.0707"),
              dutyDue = BigDecimal("70.07"),
              productName = None
            )
          )
        )
      ),
      totalDutyDuebyTaxType = Some(
        Seq(
          TotalDutyDuebyTaxType(
            taxType = "332",
            totalDutyDueTaxType = BigDecimal("314.31")
          ),
          TotalDutyDuebyTaxType(
            taxType = "351",
            totalDutyDueTaxType = BigDecimal("69.67")
          ),
          TotalDutyDuebyTaxType(
            taxType = "361",
            totalDutyDueTaxType = BigDecimal("-132.73")
          ),
          TotalDutyDuebyTaxType(
            taxType = "353",
            totalDutyDueTaxType = BigDecimal("-91.09")
          ),
          TotalDutyDuebyTaxType(
            taxType = "352",
            totalDutyDueTaxType = BigDecimal("-52.85")
          ),
          TotalDutyDuebyTaxType(
            taxType = "331",
            totalDutyDueTaxType = BigDecimal("197.19")
          )
        )
      ),
      totalDutyDue = TotalDutyDue(
        totalDutyDueAlcoholProducts = BigDecimal("441.53"),
        totalDutyOverDeclaration = BigDecimal("52.85"),
        totalDutyUnderDeclaration = BigDecimal("69.67"),
        totalDutySpoiltProduct = BigDecimal("91.09"),
        totalDutyDrawback = BigDecimal("132.73"),
        totalDutyRepDraughtProducts = BigDecimal("70.07"),
        totalDutyDue = BigDecimal("304.60")
      ),
      netDutySuspension = NetDutySuspension(
        netDutySuspensionFilled = true,
        netDutySuspensionProducts = Some(
          NetDutySuspensionProducts(
            totalLtsBeer = Some(BigDecimal("101.10")),
            totalLtsWine = Some(BigDecimal("202.20")),
            totalLtsCider = Some(BigDecimal("303.30")),
            totalLtsSpirit = Some(BigDecimal("404.40")),
            totalLtsOtherFermented = Some(BigDecimal("505.50")),
            totalLtsPureAlcoholBeer = Some(BigDecimal("1010.1011")),
            totalLtsPureAlcoholWine = Some(BigDecimal("2020.2022")),
            totalLtsPureAlcoholCider = Some(BigDecimal("3030.3033")),
            totalLtsPureAlcoholSpirit = Some(BigDecimal("4040.4044")),
            totalLtsPureAlcoholOtherFermented = Some(BigDecimal("5050.5055"))
          )
        )
      ),
      spiritsProduced = Some(
        SpiritsProduced(
          spiritsProdFilled = true,
          spiritsProduced = Some(
            SpiritsProducedDetails(
              totalSpirits = BigDecimal("123.45"),
              scotchWhiskey = BigDecimal("234.56"),
              irishWhisky = BigDecimal("345.67"),
              typeOfSpirit = Set(TypeOfSpiritType.MaltSpirit, TypeOfSpiritType.BeerBased, TypeOfSpiritType.Other),
              typeOfSpiritOther = Some("MaltyBeer")
            )
          )
        )
      )
    )

  def nilReturnCreateSubmission(periodKey: String): ReturnCreate =
    ReturnCreate(
      periodKey = periodKey,
      alcoholProducts = AlcoholProducts(
        alcoholProductsProducedFilled = false,
        regularReturn = None
      ),
      overDeclaration = OverDeclaration(
        overDeclFilled = false,
        reasonForOverDecl = None,
        overDeclarationProducts = None
      ),
      underDeclaration = UnderDeclaration(
        underDeclFilled = false,
        reasonForUnderDecl = None,
        underDeclarationProducts = None
      ),
      spoiltProduct = SpoiltProduct(
        spoiltProdFilled = false,
        spoiltProductProducts = None
      ),
      drawback = Drawback(
        drawbackFilled = false,
        drawbackProducts = None
      ),
      repackagedDraught = RepackagedDraught(
        repDraughtFilled = false,
        repackagedDraughtProducts = None
      ),
      totalDutyDuebyTaxType = None,
      totalDutyDue = TotalDutyDue(
        totalDutyDueAlcoholProducts = BigDecimal(0),
        totalDutyOverDeclaration = BigDecimal(0),
        totalDutyUnderDeclaration = BigDecimal(0),
        totalDutySpoiltProduct = BigDecimal(0),
        totalDutyDrawback = BigDecimal(0),
        totalDutyRepDraughtProducts = BigDecimal(0),
        totalDutyDue = BigDecimal(0)
      ),
      netDutySuspension = NetDutySuspension(
        netDutySuspensionFilled = false,
        netDutySuspensionProducts = None
      ),
      spiritsProduced = None
    )

  private val dueDate = 25

  def exampleReturnCreatedSuccessfulResponse(
    periodKey: String,
    total: BigDecimal,
    now: Instant,
    chargeReference: String,
    submissionId: String
  ): ReturnCreatedSuccess =
    ReturnCreatedSuccess(
      ReturnCreatedDetails(
        processingDate = now,
        adReference = appaId,
        amount = total,
        chargeReference = if (total != 0) Some(chargeReference) else None,
        paymentDueDate = if (total != 0) Some(PeriodKey.toYearMonth(periodKey).plusMonths(1).atDay(dueDate)) else None,
        submissionID = submissionId
      )
    )

  def exampleReturnCreatedDetails(
    periodKey: String,
    total: BigDecimal,
    now: Instant,
    chargeReference: String
  ): AdrReturnCreatedDetails =
    AdrReturnCreatedDetails(
      processingDate = now,
      amount = total,
      chargeReference = if (total != 0) Some(chargeReference) else None,
      paymentDueDate = if (total != 0) Some(PeriodKey.toYearMonth(periodKey).plusMonths(1).atDay(dueDate)) else None
    )

  val calculateDutyDueByTaxTypeRequest: CalculateDutyDueByTaxTypeRequest =
    CalculateDutyDueByTaxTypeRequest(
      declarationOrAdjustmentItems = Seq(
        CalculateDutyDueByTaxTypeRequestItem(
          taxType = "331",
          dutyDue = BigDecimal("115.11")
        ),
        CalculateDutyDueByTaxTypeRequestItem(
          taxType = "332",
          dutyDue = BigDecimal("321.88")
        ),
        CalculateDutyDueByTaxTypeRequestItem(
          taxType = "332",
          dutyDue = BigDecimal("245.79")
        ),
        CalculateDutyDueByTaxTypeRequestItem(
          taxType = "331",
          dutyDue = BigDecimal("8.12")
        )
      )
    )

  val calculatedDutyDueByTaxType: CalculatedDutyDueByTaxType =
    CalculatedDutyDueByTaxType(
      totalDutyDueByTaxType = Seq(
        CalculatedDutyDueByTaxTypeItem(
          taxType = "332",
          totalDutyDue = BigDecimal("567.67")
        ),
        CalculatedDutyDueByTaxTypeItem(
          taxType = "331",
          totalDutyDue = BigDecimal("123.23")
        )
      )
    )

  val calculateDutyDueByTaxTypeRequestForExampleSubmission =
    CalculateDutyDueByTaxTypeRequest(
      declarationOrAdjustmentItems = Seq(
        CalculateDutyDueByTaxTypeRequestItem(
          taxType = "331",
          dutyDue = BigDecimal("127.12")
        ),
        CalculateDutyDueByTaxTypeRequestItem(
          taxType = "332",
          dutyDue = BigDecimal("314.31")
        ),
        CalculateDutyDueByTaxTypeRequestItem(
          taxType = "352",
          dutyDue = BigDecimal("-52.85")
        ),
        CalculateDutyDueByTaxTypeRequestItem(
          taxType = "351",
          dutyDue = BigDecimal("69.67")
        ),
        CalculateDutyDueByTaxTypeRequestItem(
          taxType = "353",
          dutyDue = BigDecimal("-91.09")
        ),
        CalculateDutyDueByTaxTypeRequestItem(
          taxType = "361",
          dutyDue = BigDecimal("-132.73")
        ),
        CalculateDutyDueByTaxTypeRequestItem(
          taxType = "331",
          dutyDue = BigDecimal("70.07")
        )
      )
    )

  val calculatedDutyDueByTaxTypeForExampleSubmission =
    CalculatedDutyDueByTaxType(
      totalDutyDueByTaxType = Seq(
        CalculatedDutyDueByTaxTypeItem(
          taxType = "332",
          totalDutyDue = BigDecimal("314.31")
        ),
        CalculatedDutyDueByTaxTypeItem(
          taxType = "351",
          totalDutyDue = BigDecimal("69.67")
        ),
        CalculatedDutyDueByTaxTypeItem(
          taxType = "361",
          totalDutyDue = BigDecimal("-132.73")
        ),
        CalculatedDutyDueByTaxTypeItem(
          taxType = "353",
          totalDutyDue = BigDecimal("-91.09")
        ),
        CalculatedDutyDueByTaxTypeItem(
          taxType = "352",
          totalDutyDue = BigDecimal("-52.85")
        ),
        CalculatedDutyDueByTaxTypeItem(
          taxType = "331",
          totalDutyDue = BigDecimal("197.19")
        )
      )
    )
}
