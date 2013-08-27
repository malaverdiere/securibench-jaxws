name := "securibench-jaxws"

organization := "ca.polymtl.gigl"

version := "0.1"

scalaVersion := "2.10.2"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

//Logging
libraryDependencies ++= Seq(
  "com.typesafe" %% "scalalogging-slf4j" % "1.0.1",
  "org.slf4j" % "slf4j-api" % "1.7.5",
  "ch.qos.logback" % "logback-core" % "1.0.13",
  "ch.qos.logback" % "logback-classic" % "1.0.13",
  "org.slf4j" % "log4j-over-slf4j" % "1.7.5",
  "org.slf4j" % "jcl-over-slf4j" % "1.7.5"
)

//JEE Dependencies
libraryDependencies ++= Seq(
  "javax.xml.ws" % "jaxws-api" % "2.2.11" intransitive(),
  "javax.xml.bind" % "jaxb-api" % "2.2.9" intransitive(),
  "wsdl4j" % "wsdl4j" % "1.6.3",
  "javax.ejb" % "ejb-api" % "3.0",
  "javax.annotation" % "jsr250-api" % "1.0"
)

libraryDependencies += "junit" % "junit" % "4.11"

//Scalatra
libraryDependencies ++= Seq(
  "org.scalatra" %% "scalatra" % "2.2.1",
  "org.scalatra" %% "scalatra-specs2" % "2.2.1",
  "org.scalatra" %% "scalatra-test" % "2.2.1"
)