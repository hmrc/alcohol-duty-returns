import sbt.*

object AppDependencies {

  private val bootstrapVersion = "10.5.0"
  private val hmrcMongoVersion = "2.12.0"

  val compile = Seq(
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30" % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"        % hmrcMongoVersion,
    "org.typelevel"     %% "cats-core"                 % "2.13.0",
    "com.beachape"      %% "enumeratum-play"           % "1.9.0",
    "com.networknt"     % "json-schema-validator"      % "1.5.9" exclude("org.slf4j", "slf4j-api")
      exclude("com.fasterxml.jackson.core", "jackson-databind"),
  )

  val test = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-30"  % bootstrapVersion % Test,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30" % hmrcMongoVersion % Test,
    "org.scalatestplus" %% "scalacheck-1-17"         % "3.2.18.0"       % Test
  )

  val itDependencies = Seq.empty
}
