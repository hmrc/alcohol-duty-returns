import CodeCoverageSettings.scoverageExcludedList
import sbt.Setting
import scoverage.ScoverageKeys

object CodeCoverageSettings {

  private val scoverageExcludedList: Seq[String] = Seq(
    "<empty>",
    "Reverse.*",
    "uk.gov.hmrc.BuildInfo",
    "app.*",
    ".*SchemaValidationService.*",
    "prod.*",
    ".*Routes.*",
    "testOnly.*",
    "testOnlyDoNotUseInAppConf.*"
  )

  val settings: Seq[Setting[_]] = Seq(
    ScoverageKeys.coverageExcludedFiles := scoverageExcludedList.mkString(";"),
    ScoverageKeys.coverageMinimumStmtTotal := 99,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true
  )
}
