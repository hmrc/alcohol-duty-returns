package uk.gov.hmrc.alcoholdutyreturns.models.audit

import enumeratum._

sealed trait EventKey extends EnumEntry

object EventKey extends Enum[EventKey] {
  val values = findValues

  case object ReturnStarted extends EventKey
  case object EventKeyProducerId extends EventKey
  case object EventKeyPeriodKey extends EventKey
  case object EventKeyGovernmentGatewayId extends EventKey
  case object EventKeyGovernmentGatewayGroupId extends EventKey
  case object EventKeyObligationDetails extends EventKey
  case object EventKeyFromDate extends EventKey
  case object EventKeyToDate extends EventKey
  case object EventKeyDueDate extends EventKey
  case object EventKeyAlcoholRegime extends EventKey
  case object EventKeyReturnStartedTime extends EventKey
  case object EventKeyReturnValidUntilDate extends EventKey
}