val scala3Version = "3.5.1"

lazy val root = project
  .in(file("."))
  .settings(
    name := "scala-3-advanced",
    version := "0.1.0",
    scalaVersion := scala3Version,
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    scalafixOnCompile := true,
    scalacOptions ++= Seq(
      "-language:higherKinds",
      "-rewrite",
      "-Wunused:all",
      "--deprecation",
      "--explain",
      "--feature",
      "-Werror"
    ),
    libraryDependencies ++= Seq(
      "com.github.sbt.junit" % "jupiter-interface" % JupiterKeys.jupiterVersion.value % Test,
      "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.4"
    )
  )
