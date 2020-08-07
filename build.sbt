lazy val root = Project(BuildSettings.buildName, file("."))
.settings(BuildSettings.buildSettings ++ Seq(
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
  libraryDependencies ++= Seq(
    "com.typesafe.play" %% "play-json" % "2.9.0",
    "com.typesafe.play" %% "play-json-joda" % "2.9.0",
    "com.typesafe.play" %% "play-ahc-ws-standalone"  % "2.1.2",
    "com.typesafe.play" %% "play-ws-standalone-json" % "2.1.2",
    "io.lemonlabs" %% "scala-uri" % "2.2.4",
    "com.github.blemale"    %% "scaffeine"                      % "4.0.1" % "compile"
  )
))
