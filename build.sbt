lazy val root = Project(BuildSettings.buildName, file("."))
.settings(BuildSettings.buildSettings ++ Seq(
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
  scalacOptions in (Compile, doc) ++= Seq("-doc-root-content", baseDirectory.value + "/root-doc.txt"),
  /* resolvers += Resolver.sonatypeRepo("releases"), */
  /* resolvers += Resolver.typesafeRepo("releases"), */

  libraryDependencies ++= Seq(
    // "com.typesafe.play" %% "play-iteratees" % "2.4.2",
    "com.typesafe.play" %% "play-json" % "2.8.0",
    "com.typesafe.play" %% "play-json-joda" % "2.8.0",
    "com.typesafe.play" %% "play-ws" % "2.8.0-RC2",
    "org.apache.commons" % "commons-collections4" % "4.4",
    "io.lemonlabs" %% "scala-uri" % "1.5.0"
  )
))
