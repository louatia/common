//logLevel := Level.Warn

// The Typesafe repository
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.8.5")

// the JaCoCo plugin for code coverage
addSbtPlugin("de.johoop" % "jacoco4sbt" % "2.1.6")