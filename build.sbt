//
//import de.johoop.jacoco4sbt.JacocoPlugin._
//import de.johoop.jacoco4sbt.{HTMLReport, XMLReport}

lazy val root = project.in(file(".")).settings(releaseSettings: _*)
// .settings(commonJacocoSettings: _*)

name := "api-common"

scalaVersion := "2.11.7"

organization := "com.github.louatia"

publishArtifact in Test := true


val playVersion = "2.3.9"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-java-ws" % playVersion  % "provided",
  "com.typesafe.play" % "play-test_2.11" % playVersion
//  "net.logstash.logback" % "logstash-logback-encoder" % "4.2"
)

//lazy val commonJacocoSettings = jacoco.settings ++ Seq(
//  parallelExecution in jacoco.Config := false,
//  jacoco.outputDirectory in jacoco.Config := file("target/jacoco"),
//  jacoco.excludes in jacoco.Config := Seq("views*", "*Routes*", "controllers*routes*", "controllers*Reverse*", "controllers*javascript*", "controller*ref*", "*InternalTest"),
//  jacoco.reportTitle in jacoco.Config := s"Jacoco Coverage Report for ${name.value} ${version.value}",
//  jacoco.reportFormats in jacoco.Config := Seq(XMLReport(encoding = "utf-8"), HTMLReport(encoding = "utf-8")))




publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := (
  <url>https://www.github.com/louatia</url>
    <licenses>
      <license>
        <name>BSD-style</name>
        <url>http://www.opensource.org/licenses/bsd-license.php</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:louatia/api-common.git</url>
      <connection>scm:git:git@github.com:louatia/api-common.git</connection>
    </scm>
    <developers>
      <developer>
        <id>louatia</id>
        <name>Amine Louati</name>
        <url>https://www.github.com/louatia</url>
      </developer>
    </developers>
  )

