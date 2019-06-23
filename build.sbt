name          := "basespace2S3"
organization  := "com.miodx.clonomap"
version       := "0.2.0-SNAPSHOT"
description   := "basespace2S3 project"

scalaVersion  := "2.11.11"

bucketSuffix  := "miodx.com"

libraryDependencies ++= Seq(
  "com.typesafe.play"  %% "play-ws"         % "2.5.12",
  "com.miodx.clonomap" %% "loquat"          % "2.1.0-SNAPSHOT",
  "org.scalatest"      %% "scalatest"       % "3.0.5" % Test

)

dependencyOverrides ++= Seq(
  "joda-time" % "joda-time" % "2.9.6",
  "com.fasterxml.jackson.core"  % "jackson-databind"      % "2.7.8",
  "org.slf4j"                   % "slf4j-api"             % "1.7.25",
  "commons-logging"             % "commons-logging"       % "1.2",
  "org.scala-lang.modules"      %% "scala-xml"            % "1.0.5"
)

// Uncomment if you need to deploy this project as a Statika bundle:
generateStatikaMetadataIn(Compile)

assemblyMergeStrategy in assembly := {
  case x if Assembly.isConfigFile(x) =>
      MergeStrategy.concat
    case PathList(ps @ _*) if Assembly.isReadme(ps.last) || Assembly.isLicenseFile(ps.last) =>
      MergeStrategy.rename
    case PathList("META-INF", xs @ _*) =>
      (xs map {_.toLowerCase}) match {
        case ("manifest.mf" :: Nil) | ("index.list" :: Nil) | ("dependencies" :: Nil) =>
          MergeStrategy.discard
        case ps @ (x :: xs) if ps.last.endsWith(".sf") || ps.last.endsWith(".dsa") =>
          MergeStrategy.discard
        case "plexus" :: xs =>
          MergeStrategy.discard
        case "services" :: xs =>
          MergeStrategy.filterDistinctLines
        case ("spring.schemas" :: Nil) | ("spring.handlers" :: Nil) =>
          MergeStrategy.filterDistinctLines
        case _ => MergeStrategy.first
      }
    case _ => MergeStrategy.first
}


// Uncomment if you have release-only tests using the assembled fat-jar:
// fullClasspath in assembly := (fullClasspath in Test).value

// Uncomment for Java projects:
// enablePlugin(JavaOnlySettings)
