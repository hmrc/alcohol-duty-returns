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

package uk.gov.hmrc.alcoholdutyreturns.service

import com.networknt.schema.{InputFormat, JsonSchema, JsonSchemaFactory, SpecVersion}
import com.networknt.schema.oas.OpenApi30
import enumeratum.{Enum, EnumEntry}
import play.api.libs.json.{Json, Writes}
import play.api.{Environment, Logging}
import uk.gov.hmrc.alcoholdutyreturns.config.AppConfig
import uk.gov.hmrc.alcoholdutyreturns.models.returns.ReturnCreate

import javax.inject.{Inject, Singleton}
import scala.jdk.CollectionConverters._
import scala.jdk.FunctionConverters.enrichAsJavaConsumer
import scala.util.{Failure, Success, Try}

@Singleton
class SchemaValidationService @Inject() (
  env: Environment,
  appConfig: AppConfig
) extends Logging {

  /** Schemas are loaded when this object is created
    *  This should be bound as an eagerSingleton() in Module to initialise on startup
    */

  private sealed trait SchemaType extends EnumEntry

  private object SchemaType extends Enum[SchemaType] {
    val values = findValues

    case object SubmitReturn extends SchemaType
  }

  import SchemaType._

  private val schemaFileNames: Map[SchemaType, String] = Map(SubmitReturn -> "submitReturnSchema.json")

  private val jsonSchemaFactory = JsonSchemaFactory
    .getInstance(
      SpecVersion.VersionFlag.V202012,
      enrichAsJavaConsumer((builder: JsonSchemaFactory.Builder) =>
        builder.metaSchema(OpenApi30.getInstance()).defaultMetaSchemaIri(OpenApi30.getInstance().getIri)
      ).asJava
    )

  private def loadSchema(schemaFileName: String): Option[JsonSchema] = {
    val is = env.classLoader.getResourceAsStream(s"${appConfig.schemaDir}/$schemaFileName")

    if (is != null) {
      val maybeSchema = Try {
        jsonSchemaFactory.getSchema(is)
      } match {
        case Success(schema) =>
          logger.info(s"Successfully loaded json schema $schemaFileName")
          Some(schema)
        case Failure(_)      =>
          logger.error(s"Unable to load json schema $schemaFileName")
          None
      }

      is.close()
      maybeSchema
    } else {
      None
    }
  }

  private val schemas: Map[SchemaType, JsonSchema] = {
    val loadAttemptedSchemas = SchemaType.values.map { schema =>
      val fileName = schemaFileNames.getOrElse(
        schema, {
          logger.error(s"Cannot find filename for schema ${schema.entryName}")
          throw new RuntimeException(s"Cannot find filename for schema ${schema.entryName}")
        }
      )
      loadSchema(fileName).map(schema -> _)
    }
    val numSchemas           = loadAttemptedSchemas.length
    val schemasLoaded        = loadAttemptedSchemas.flatten
    val numSchemasLoaded     = schemasLoaded.length

    logger.info(s"Validation schemas $numSchemas of $numSchemasLoaded loaded")

    if (numSchemas != numSchemasLoaded) {
      logger.error(s"Only $numSchemas of $numSchemasLoaded schemas loaded - unable to start server")
      throw new RuntimeException("Schema loading failed")
    } else {
      logger.info(s"All validation schemas($numSchemas) were loaded")
      schemasLoaded.toMap
    }
  }

  private def selectSchema[T](obj: T): Either[Throwable, JsonSchema] = obj match {
    case _ if obj.isInstanceOf[ReturnCreate] =>
      schemas
        .get(SubmitReturn)
        .fold[Either[Throwable, JsonSchema]](
          Left(new RuntimeException(s"Schema ${SubmitReturn.entryName} should have been loaded"))
        )(schema => Right(schema))
    case _                                   =>
      logger.error(s"Attempting schema validation against unsupported object type ${obj.getClass.getTypeName}")
      Left(
        new RuntimeException(
          s"Attempting schema validation against unsupported object type ${obj.getClass.getTypeName}"
        )
      )
  }

  /**
    * @return true if should continue, false if to fail with e.g. BadRequest
    */
  def validateAgainstSchema[T](obj: T)(implicit writes: Writes[T]): Boolean =
    selectSchema(obj) match {
      case Left(e)       =>
        logger.error(e.getMessage)
        false
      case Right(schema) =>
        val json               = Json.toJson(obj).toString()
        val validationFailures = schema.validate(json, InputFormat.JSON).asScala

        if (validationFailures.nonEmpty) {
          logger.error(s"Validation of return submission against schema failed: ${validationFailures.mkString("; ")}")
          false
        } else {
          logger.info(s"Validation of return submission against schema successful")
          true
        }
    }
}
