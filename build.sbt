lazy val root = (project in file(".")).settings(
  inThisBuild(List(
    organization := "io.geekabyte.ristex",
    scalaVersion := "2.12.7",
    version      := "0.1.0",
    homepage     := Some(url("https://github.com/ip-num-tools/ristex")),
    scmInfo := Some(ScmInfo(url("https://github.com/ip-num-tools/ristex"), "git@github.com:ip-num-tools/ristex.git")),
    developers := List(Developer("dadepo", "Dadepo Aderemi", "dadepo@gmail.com", url("https://github.com/dadepo"))),
    licenses += ("MIT License", url("http://opensource.org/licenses/MIT")),
    publishMavenStyle := true
  )),
  name := "ristex",
  sonatypeProfileName := "io.geekabyte",
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.0.4" % Test,
    "org.tpolecat" %% "atto-core"    % "0.6.4",
    "org.tpolecat" %% "atto-refined" % "0.6.4"
  ),
  publishTo := Some(
    if (isSnapshot.value)
      Opts.resolver.sonatypeSnapshots
    else
      Opts.resolver.sonatypeStaging
  ),
  resolvers += Resolver.sonatypeRepo("releases"),
  scalacOptions ++= Seq(
    "-language:higherKinds",
    "-feature",
    "-Xfatal-warnings",
    "-Ypartial-unification",
    "-language:implicitConversions"
  ))