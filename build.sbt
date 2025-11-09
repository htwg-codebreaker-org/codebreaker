val scala3Version = "3.6.4"

val setJavaFXVersion = settingKey[Seq[ModuleID]](
  "Sets the JavaFX version and classifier based on the system architecture"
)

setJavaFXVersion := {
  val arch = sys.props("os.arch")
  val osName = sys.props("os.name").toLowerCase
  val javafxVersion = "22"
  val javafxClassifier = osName match {
    case os if os.contains("windows") => "win"
    case os if os.contains("linux") =>
      arch match {
        case "aarch64" => "linux-aarch64"
        case _         => "linux"
      }
    case _ => throw new UnsupportedOperationException("Unsupported OS")
  }

  Seq(
    "org.openjfx" % "javafx-base" % javafxVersion classifier javafxClassifier,
    "org.openjfx" % "javafx-controls" % javafxVersion classifier javafxClassifier,
    "org.openjfx" % "javafx-fxml" % javafxVersion classifier javafxClassifier,
    "org.openjfx" % "javafx-graphics" % javafxVersion classifier javafxClassifier,
    "org.openjfx" % "javafx-media" % javafxVersion classifier javafxClassifier,
    "org.openjfx" % "javafx-swing" % javafxVersion classifier javafxClassifier,
    "org.openjfx" % "javafx-web" % javafxVersion classifier javafxClassifier
  )
}

lazy val root = project
  .in(file("."))
  .enablePlugins(CoverallsPlugin)
  .settings(
    name := "codebreaker",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,
    scalacOptions ++= Seq("-encoding", "UTF-8"),
    javaOptions += "-Dfile.encoding=UTF-8",

    coverageEnabled := true,
    coverageHighlighting := true,
    coverageFailOnMinimum := false,
    coverageMinimumStmtTotal := 70,
    coverageExcludedPackages := "<empty>;de\\.htwg\\.codebreaker\\.view\\.gui\\..*;de\\.htwg\\.codebreaker\\.Codebreaker",
    coverageExcludedFiles := ".*\\/view\\/gui\\/.*;.*\\/Main\\.scala",

    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit"              % "1.0.0"   % Test,
      "org.scalactic" %% "scalactic"          % "3.2.14",
      "org.scalatest" %% "scalatest"          % "3.2.14"  % Test,
      "org.scalatestplus" %% "mockito-3-12"   % "3.2.10.0" % Test,
      "org.scalafx" %% "scalafx"              % "22.0.0-R33" excludeAll (
        ExclusionRule(organization = "org.openjfx")
      )
    ),

    libraryDependencies ++= setJavaFXVersion.value,

    testFrameworks += new TestFramework("org.scalatest.tools.Framework"),

    addCommandAlias("fullTest", ";clean;coverage;test;coverageReport;coverageAggregate;coveralls")
  )
