import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  private val bootstrapVersion = "8.5.0"
  private val hmrcMongoVersion = "1.8.0"

  val compile = Seq(
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30" % bootstrapVersion,
    "ai.x"              %% "play-json-extensions"      % "0.42.0",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"        % hmrcMongoVersion,
    "org.typelevel"     %% "cats-core"                 % "2.10.0",
    "com.beachape"      %% "enumeratum-play"           % "1.8.0",
    "com.networknt"     % "json-schema-validator"      % "1.5.1" exclude("org.slf4j", "slf4j-api")
      exclude("com.fasterxml.jackson.core", "jackson-databind"),
  )

  val test = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-30"   % bootstrapVersion % Test,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30"  % hmrcMongoVersion % Test,
    "org.mockito"       %% "mockito-scala"            % "1.17.30"        % Test,
    "org.scalatestplus" %% "scalatestplus-scalacheck" % "3.1.0.0-RC2"    % Test,
    "org.mockito"       %% "mockito-scala-scalatest"  % "1.17.30"        % Test
  )

  val itDependencies = Seq.empty
}
