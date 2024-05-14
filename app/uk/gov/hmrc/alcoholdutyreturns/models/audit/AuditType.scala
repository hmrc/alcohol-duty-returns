package uk.gov.hmrc.alcoholdutyreturns.models.audit

import enumeratum._

sealed trait AuditType extends EnumEntry

object AuditType extends Enum[AuditType] {
  val values = findValues

  case object ReturnStarted extends AuditType
}
