lazy val commonSettings = Seq(
  scalaVersion := "2.11.8",
  scalacOptions ++= Seq(
    "-deprecation",
    "-feature",
    "-unchecked",
    "-Xlint",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-unused",
    "-Ywarn-unused-import",
    "-Ywarn-value-discard"
  )
)

lazy val root = (project in file(".")).
  aggregate(core, benchmark)

lazy val core = (project in file("redica-core")).
  settings(commonSettings: _*).
  settings(
    name := "redica",
    version := "0.1-SNAPSHOT",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.0.0" % "test",
      "org.scalacheck" %% "scalacheck" % "1.13.2" % "test",
      "org.mockito" % "mockito-all" % "1.10.19" % "test"
    )
  )

lazy val benchmark = (project in file("redica-benchmark")).
  settings(commonSettings: _*).
  dependsOn(core).
  enablePlugins(JmhPlugin)

