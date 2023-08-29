import sbt._
import Keys._

import com.typesafe.sbt.pgp.PgpKeys._

object ApplicationBuild extends Build {

  val appName         = "play2-memcached-" + playShortName
  val appVersion      = "0.8.1-BMD-SNAPSHOT"

  lazy val baseSettings = Seq(
    parallelExecution in Test := false
  )

  def playShortName: String = {
    val version = play.core.PlayVersion.current
    val majorMinor = version.split("""\.""").take(2).mkString("")
    s"play$majorMinor"
  }

  def playMajorAndMinorVersion: String = {
    val v = play.core.PlayVersion.current
    v.split("""\.""").take(2).mkString(".")
  }

  def playVersionSpecificSourceDirectoryUnder(sd: java.io.File): java.io.File = {
    val versionSpecificSrc = new java.io.File(sd, "play_" + playMajorAndMinorVersion)
    val defaultSrc = new java.io.File(sd, "default")
    if (versionSpecificSrc.exists) versionSpecificSrc else defaultSrc
  }

  def playVersionSpecificUnmanagedSourceDirectories(sds: Seq[java.io.File], sd: java.io.File) = {
    Seq(playVersionSpecificSourceDirectoryUnder(sd)) ++ sds
  }

  lazy val root = Project("root", base = file("."))
    .settings(baseSettings: _*)
    .settings(
      publishLocal := {},
      publish := {},
      publishLocalSigned := {},
      publishSigned := {}
    ).aggregate(plugin, scalaSample, javaSample)

  lazy val plugin = Project(appName, base = file("plugin"))
    .settings(baseSettings: _*)
    .settings(
      resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/",
      resolvers += DefaultMavenRepository,
      resolvers += Resolver.mavenLocal,
      resolvers += "Typesafe Maven Repository" at "https://repo.typesafe.com/typesafe/maven-releases/",
      resolvers += "Spy Repository" at "https://files.couchbase.com/maven2",
      libraryDependencies += "net.spy" % "spymemcached" % "2.9.1",
      libraryDependencies += "com.typesafe.play" %% "play" % play.core.PlayVersion.current % "provided",
      libraryDependencies += "com.typesafe.play" %% "play-cache" % play.core.PlayVersion.current % "provided",
      libraryDependencies += "com.typesafe.play" %% "play-test" % play.core.PlayVersion.current % "provided,test",
      libraryDependencies += "org.specs2" %% "specs2" % "3.7" % "test",
      organization := "com.github.mumoshu",
      version := appVersion,
      publishTo := {
        val v = version.value
        val nexus = "https://dev.bmd-software.com/nexus/"
        if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots")
        else                             Some("releases" at nexus + "content/repositories/releases")
      },
      publishMavenStyle := true,
      publishArtifact in Test := false,
      pomIncludeRepository := { _ => false },
      pomExtra := (
        <url>https://github.com/mumoshu/play2-memcached</url>
          <licenses>
            <license>
              <name>BSD-style</name>
              <url>http://www.opensource.org/licenses/bsd-license.php</url>
              <distribution>repo</distribution>
            </license>
          </licenses>
          <scm>
            <url>git@github.com:mumoshu/play2-memcached.git</url>
            <connection>scm:git:git@github.com:mumoshu/play2-memcached.git</connection>
          </scm>
          <developers>
            <developer>
              <id>you</id>
              <name>KUOKA Yusuke</name>
              <url>https://github.com/mumoshu</url>
            </developer>
          </developers>
        ),
      unmanagedSourceDirectories in Compile := {
        playVersionSpecificUnmanagedSourceDirectories((unmanagedSourceDirectories in Compile).value, (sourceDirectory in Compile).value)
      },
      unmanagedSourceDirectories in Test := {
        playVersionSpecificUnmanagedSourceDirectories((unmanagedSourceDirectories in Test).value, (sourceDirectory in Test).value)
      }
    )

    def playCache: ModuleID = {
      play2compat.PlayImport.cache
    }

    def playScalaPlugin: AutoPlugin = {
      play2compat.PlayScala
    }

    lazy val scalaSample = Project(
      "scala-sample",
      playVersionSpecificSourceDirectoryUnder(file("samples/scala"))
    ).enablePlugins(playScalaPlugin).settings(
        playScalaPlugin.projectSettings: _*
    ).settings(
      resolvers += "Typesafe Maven Repository" at "https://dl.bintray.com/typesafe/maven-releases/",
      libraryDependencies += playCache,
      parallelExecution in Test := false,
      publishLocal := {},
      publish := {},
      publishLocalSigned := {},
      publishSigned := {}
    ).dependsOn(plugin)

    lazy val javaSample = Project(
      "java-sample",
      playVersionSpecificSourceDirectoryUnder(file("samples/java"))
    ).enablePlugins(play2compat.PlayJava).settings(baseSettings: _*).settings(
      libraryDependencies += play2compat.PlayImport.cache,
      publishLocal := {},
      publish := {},
      publishLocalSigned := {},
      publishSigned := {}
    ).dependsOn(plugin)

}
