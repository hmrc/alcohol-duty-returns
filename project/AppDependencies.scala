import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  private val bootstrapVersion = "9.12.0"
  private val hmrcMongoVersion = "2.6.0"
  private val mockitoScalaVersion      = "1.17.37"

  val compile = Seq(
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30" % bootstrapVersion,
    "ai.x"              %% "play-json-extensions"      % "0.42.0",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"        % hmrcMongoVersion,
    "org.typelevel"     %% "cats-core"                 % "2.12.0",
    "com.beachape"      %% "enumeratum-play"           % "1.8.1",
    "com.networknt"     % "json-schema-validator"      % "1.5.1" exclude("org.slf4j", "slf4j-api")
      exclude("com.fasterxml.jackson.core", "jackson-databind"),
  )

  val test = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-30"   % bootstrapVersion % Test,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30"  % hmrcMongoVersion % Test,
    "org.mockito"       %% "mockito-scala"            % mockitoScalaVersion        % Test,
    "org.mockito"       %% "mockito-scala-scalatest"  % mockitoScalaVersion        % Test,
    "org.scalatestplus" %% "scalatestplus-scalacheck" % "3.1.0.0-RC2"    % Test
  )

  val itDependencies = Seq.empty
}
