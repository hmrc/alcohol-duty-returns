package uk.gov.hmrc.alcoholdutyreturns.service

import com.google.inject.Inject
import uk.gov.hmrc.alcoholdutyreturns.config.AppConfig
import uk.gov.hmrc.alcoholdutyreturns.models.audit.AuditType.ReturnStarted
import uk.gov.hmrc.alcoholdutyreturns.models.audit.EventKey.{EventKeyAlcoholRegime, EventKeyDueDate, EventKeyFromDate, EventKeyGovernmentGatewayGroupId, EventKeyGovernmentGatewayId, EventKeyObligationDetails, EventKeyPeriodKey, EventKeyProducerId, EventKeyReturnStartedTime, EventKeyReturnValidUntilDate, EventKeyToDate}
import uk.gov.hmrc.alcoholdutyreturns.models.audit.{AuditType, EventKey}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.DataEvent

import scala.concurrent.{ExecutionContext, Future}

// will need to set up config for higher envs
/*
    if (configuration.get[Boolean]("auditing.enabled"))
      AuditingConfig(
        enabled           = true,
        consumer          = Some(
                              Consumer(
                                BaseUri(
                                  host     = configuration.get[String]("auditing.consumer.baseUri.host"),
                                  port     = configuration.get[Int]("auditing.consumer.baseUri.port"),
                                  protocol = configuration.getOptional[String]("auditing.consumer.baseUri.protocol").getOrElse("http")
                                )
                              )
                            ),
        auditSource       = configuration.get[String]("appName"),
        auditSentHeaders  = configuration.get[Boolean]("auditing.auditSentHeaders")
 */
class AuditService @Inject()(appConfig: AppConfig,
                             auditConnector: AuditConnector)(implicit ec: ExecutionContext) {
  private def audit[E <: EventKey](auditType: AuditType, eventDetail: Map[E, String])(implicit hc: HeaderCarrier): Future[AuditResult] = {
    val event = DataEvent(
      auditSource = appConfig.appName,
      auditType = auditType.entryName,
      detail = eventDetail.map { case (k, v) => k.entryName -> v}
    )

    auditConnector.sendEvent(event)
  }

  // Questions:
  // If this fails, should it log, or return an error or leave to the caller - is Unit okay as a return?
  // Dates/Times, send in as an instant and format here (consistency, saves caller doing it?)
  // periodKey - send in as a ReturnPeriod?
  // from/to/due are strings from the API and won't be as Instant etc?
  def auditStartReturn(producerId: String,
                       periodKey: String,
                       governmentGatewayId: String,
                       governmentGatewayGroupId: String,
                       obligationDetails: String,
                       fromDate: String,
                       toDate: String,
                       dueDate: String,
                       alcoholRegime: String,
                       returnStartedTime: String,
                       returnValidUntilDate: String
                      )(implicit hc: HeaderCarrier): Unit = {
    val eventDetail = Map(
      EventKeyProducerId -> producerId,
      EventKeyPeriodKey -> periodKey,
      EventKeyGovernmentGatewayId -> governmentGatewayId,
      EventKeyGovernmentGatewayGroupId -> governmentGatewayGroupId,
      EventKeyObligationDetails -> obligationDetails,
      EventKeyFromDate -> fromDate,
      EventKeyToDate -> toDate,
      EventKeyDueDate -> dueDate,
      EventKeyAlcoholRegime -> alcoholRegime,
      EventKeyReturnStartedTime -> returnStartedTime,
      EventKeyReturnValidUntilDate -> returnValidUntilDate
    )

    audit(ReturnStarted, eventDetail).flatMap(Future.successful)
  }

 /*
 When Can This Audit Event Be Created? Once the Returns Started link is developed
Audit Source: [micro service name]
Audit Type Name: ReturnStarted
Audit Event Trigger: When a user starts a new return
Audit Event Should Contain:
Alcohol Producer ID
Period Key
Government Gateway ID
Government Gateway Group ID
Obligation Details
From date
To date
Due date
Period Key(maybe)
Alcohol Regimes
Session ID
Date & Time Return Started
Return Valid Until Date
  */

}
