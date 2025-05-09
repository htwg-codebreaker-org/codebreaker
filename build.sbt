val scala3Version = "3.6.4"

lazy val root = project
  .in(file("."))
  .enablePlugins(CoverallsPlugin)   // ← wichtig für Coveralls
  .settings(
    name := "codebreaker",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    coverageEnabled := true,         // ← wichtig für scoverage

    // Bibliotheken für Tests und Hilfsmittel
    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit"              % "1.0.0"   % Test,
      "org.scalactic" %% "scalactic"          % "3.2.14",
      "org.scalatest" %% "scalatest"          % "3.2.14"  % Test,
      "org.scalatestplus" %% "mockito-3-12"   % "3.2.10.0" % Test 
    ),

    // Test-Framework für ScalaTest aktivieren
    testFrameworks += new TestFramework("org.scalatest.tools.Framework")
  )
