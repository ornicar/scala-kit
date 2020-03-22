import sbt._
import Keys._

object BuildSettings {

  val buildName = "scala-kit"
  val buildOrganization = "io.prismic"
  val buildVersion = "1.2.18-THIB213"
  val buildScalaVersion = "2.13.1"

  val buildSettings = Seq(
    organization := buildOrganization,
    version := buildVersion,
    scalaVersion := buildScalaVersion,
    // crossScalaVersions := Seq("2.10.4", "2.11.1"),
    scalacOptions := Seq("-deprecation", "-unchecked", "-feature"),
    publishTo := Some(
      Resolver.file("file", new File(sys.props.getOrElse("publishTo", "")))
    ),
    pomExtra := {
      <url>https://github.com/prismicio/scala-kit</url>
        <licenses>
          <license>
            <name>Apache 2</name>
            <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
          </license>
        </licenses>
        <scm>
          <connection>scm:git:github.com/prismicio/scala-kit.git</connection>
          <developerConnection>scm:git:git@github.com:prismicio/scala-kit.git</developerConnection>
          <url>github.com/prismicio/scala-kit.git</url>
        </scm>
        <developers>
          <developer>
            <name>Prismic.io Team</name>
            <email>contact@prismic.io</email>
            <organization>Prismic.io</organization>
            <organizationUrl>http://prismic.io</organizationUrl>
          </developer>
        </developers>
    }
  )
}
