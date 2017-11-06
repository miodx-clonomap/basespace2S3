name          := "basespace2S3"
organization  := "era7bio"
description   := "basespace2S3 project"

scalaVersion  := "2.11.8"

bucketSuffix  := "era7.com"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-ws"         % "2.5.12",
  "ohnosequences"     %% "loquat"          % "2.0.0-RC4",
  "org.scalatest"     %% "scalatest"       % "3.0.4" % Test

)

dependencyOverrides ++= Seq(
  "joda-time" % "joda-time" % "2.9.6",
  "com.fasterxml.jackson.core"  % "jackson-databind"      % "2.7.8",
  "org.slf4j"                   % "slf4j-api"             % "1.7.25",
  "commons-logging"             % "commons-logging"       % "1.2",
  "org.scala-lang.modules"      %% "scala-xml"            % "1.0.5"
)

// Uncomment if you need to deploy this project as a Statika bundle:
// generateStatikaMetadataIn(Compile)

// Uncomment if you have release-only tests using the assembled fat-jar:
// fullClasspath in assembly := (fullClasspath in Test).value

// Uncomment for Java projects:
// enablePlugin(JavaOnlySettings)
