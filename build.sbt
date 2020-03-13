lazy val root = Project(BuildSettings.buildName, file("."))
.settings(BuildSettings.buildSettings ++ Seq(
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
  scalacOptions in (Compile, doc) ++= Seq("-doc-root-content", baseDirectory.value + "/root-doc.txt"),

  libraryDependencies ++= Seq(
    "com.typesafe.play" %% "play-json" % "2.8.1",
    "com.typesafe.play" %% "play-json-joda" % "2.8.1",
    "com.typesafe.play" %% "play-ws" % "2.8.1",
    "io.lemonlabs" %% "scala-uri" % "2.2.0",
    "com.github.blemale"    %% "scaffeine"                      % "3.1.0" % "compile"
  )
))
