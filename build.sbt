lazy val root = (project in file(".")).settings(
  inThisBuild(List(
    organization := "io.geekabyte",
    scalaVersion := "2.12.7",
    version      := "0.1.0-SNAPSHOT"
  )),
  name := "ristex",
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.0.4" % Test,
    "org.tpolecat" %% "atto-core"    % "0.6.4",
    "org.tpolecat" %% "atto-refined" % "0.6.4"
  ),
  resolvers += Resolver.sonatypeRepo("releases"),
  scalacOptions ++= Seq(
    "-language:higherKinds",
    "-feature",
    "-Xfatal-warnings",
    "-Ypartial-unification",
    "-language:implicitConversions"
  ))