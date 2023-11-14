import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  private val bootstrapVersion = "7.22.0"
  private val hmrcMongoVersion = "1.3.0"

  val compile = Seq(
    "uk.gov.hmrc"       %% "bootstrap-backend-play-28" % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28"        % hmrcMongoVersion
  )

  val test = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-28"   % bootstrapVersion % "test, it",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-28"  % hmrcMongoVersion % "test, it",
    "org.mockito"       %% "mockito-scala"            % "1.17.27"        % "test, it",
    "org.scalatestplus" %% "scalatestplus-scalacheck" % "3.1.0.0-RC2"    % "test, it",
    "org.mockito"       %% "mockito-scala-scalatest"  % "1.17.14"        % "test, it"
  )
}
