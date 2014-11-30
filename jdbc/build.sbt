name := "scala-cqrs-jdbc"

version := "1.0.0"

scalaVersion := "2.11.4"

resolvers ++= Seq("marpiec BinTray" at "http://dl.bintray.com/marpiec/maven/")

libraryDependencies ++= Seq("org.slf4j" % "slf4j-api" % "1.7.7",
  "pl.mpieciukiewicz.mpjsons" % "mpjsons" % "0.5.2" % Test,
  "org.scalatest" %% "scalatest" % "2.2.2" % Test,
  //"org.assertj" % "assertj-core" % "1.7.0" % Test, // Intellij has problem resolving assertThat, although during build it works fine
  "org.easytesting" % "fest-assert-core" % "2.0M10" % Test,
  "org.apache.commons" % "commons-dbcp2" % "2.0.1" % Test,
  "org.postgresql" % "postgresql" % "9.3-1102-jdbc41" % Test)