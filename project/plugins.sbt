ivyLoggingLevel := UpdateLogging.Full

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

// The Typesafe repository
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

val playVersion = scala.util.Properties.envOrElse("PLAY_VERSION", "2.4.0")

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % playVersion)

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")

