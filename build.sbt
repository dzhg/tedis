name := "tedis"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.11.11"

libraryDependencies ++= {
  Seq(
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "net.debasishg" %% "redisclient" % "3.4" % "test",
    "org.scalatest" %% "scalatest" % "3.0.1" % "test"
  )
}

