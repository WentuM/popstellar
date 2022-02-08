import scala.util.{Try, Success, Failure}
import sbtsonar.SonarPlugin.autoImport.sonarProperties

name := "pop"

version := "0.1"

scalaVersion := "2.13.7"

scalacOptions ++= Seq(
    "-deprecation", // deprecation warnings
    "-feature",     // usage of features that should be imported separately
)

//Reload changes automatically
Global / onChangedBuildSource := ReloadOnSourceChanges
Global / cancelable := true

//Fork run task in compile scope
Compile/ run / fork := true
Compile/ run / connectInput := true
Compile/ run / javaOptions += "-Dscala.config=src/main/scala/ch/epfl/pop/config"

//Make test execution synchronized
Test/ test/ parallelExecution := false

//Create task to copy the protocol folder to resources
lazy val copyProtocolTask = taskKey[Unit]("Copy protocol to resources")
copyProtocolTask := {
    val log = streams.value.log
    log.info("Executing Protocol folder copy...")
    val scalaDest = "be2-scala"
    baseDirectory.value.name
    if(! baseDirectory.value.name.equals(scalaDest)){
        log.error(s"Please make sure you working dir is $scalaDest !")
    }else{
        val source = new File("../protocol")
        val dest   = new File("./src/main/resources/protocol")
        Try(IO.copyDirectory(source,dest, overwrite = true)) match {
            case Success(_) => log.info("Copied !!")
            case Failure(exception) =>
                log.error("Could not copy protocol to resource folder")
                exception.printStackTrace()
        }
    }
}
//Add the copyProtocolTask to compile and test scopes
(Compile/ compile) := ((Compile/ compile) dependsOn copyProtocolTask).value
(Test/ test) := ((Test/ test) dependsOn copyProtocolTask).value

//Setup resource directory for jar assembly
(Compile /packageBin / resourceDirectory) := file(".") / "./src/main/resources"

//Make resourceDirectory setting global to remove sbt warning
(Global / excludeLintKeys) += resourceDirectory

//Setup main calass task context/confiuration
Compile/ run/ mainClass := Some("ch.epfl.pop.Server")
Compile/ packageBin/ mainClass := Some("ch.epfl.pop.Server")

lazy val scoverageSettings = Seq(
  Compile/ coverageEnabled  := true,
  Test/ coverageEnabled  := true,
  packageBin/ coverageEnabled  := false,
)

ThisBuild/ scapegoatVersion := "1.4.11"

scapegoatReports := Seq("xml")

// temporarily report scapegoat errors as warnings, to avoid broken builds
Scapegoat/ scalacOptions += "-P:scapegoat:overrideLevels:all=Warning"

// Configure Sonar
sonarProperties := Map(
  "sonar.organization" -> "dedis",
  "sonar.projectKey" -> "dedis_popstellar_be2",

  "sonar.sources" -> "src/main/scala",
  "sonar.tests" -> "src/test/scala",

  "sonar.sourceEncoding" -> "UTF-8",
  "sonar.scala.version" -> "2.13.7",
  // Paths to the test and coverage reports
  "sonar.scala.coverage.reportPaths" -> "./target/scala-2.13/scoverage-report/scoverage.xml",
  "sonar.scala.scapegoat.reportPaths" -> "./target/scala-2.13/scapegoat-report/scapegoat.xml"
)

assembly/ assemblyMergeStrategy  := {
    case PathList("module-info.class") => MergeStrategy.discard
    case PathList("reference.conf") => MergeStrategy.concat
    case PathList("META-INF","MANIFEST.MF") => MergeStrategy.discard
    case _ => MergeStrategy.defaultMergeStrategy("")
}

// For websockets
val AkkaVersion = "2.6.8"
val AkkaHttpVersion = "10.2.0"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion)

// Logging for akka
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.3" % Runtime

// distributed pub sub cluster
libraryDependencies += "com.typesafe.akka" %% "akka-cluster-tools" % AkkaVersion

// Akka actor test kit
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % AkkaVersion % Test

// For LevelDB database
// https://mvnrepository.com/artifact/org.iq80.leveldb/leveldb
libraryDependencies += "org.iq80.leveldb" % "leveldb" % "0.12"
libraryDependencies += "org.xerial.snappy" % "snappy-java" % "1.1.7.3"
// missing binary dependency, leveldbjni
//libraryDependencies += "com.typesafe.akka" %% "akka-persistence" % AkkaVersion


// Json Parser (https://github.com/spray/spray-json)
libraryDependencies += "io.spray" %%  "spray-json" % "1.3.5"

// Encryption
libraryDependencies += "com.google.crypto.tink" % "tink" % "1.5.0"

// Scala unit tests
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.9" % Test

// Jackson Databind (for Json Schema Validation)
libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.0.0-RC3"

// Json Schema Validator
libraryDependencies += "com.networknt" % "json-schema-validator" % "1.0.60"

conflictManager := ConflictManager.latestCompatible
