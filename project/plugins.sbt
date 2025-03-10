resolvers ++= Seq(
  "Era7 maven releases" at "https://s3-eu-west-1.amazonaws.com/releases.era7.com",
  "repo.jenkins-ci.org" at "https://repo.jenkins-ci.org/public", // why??
  "Typesafe Repository" at "https://repo.typesafe.com/typesafe/releases/"
)

addSbtPlugin("ohnosequences" % "nice-sbt-settings" % "0.8.0-RC4")

// test coverage
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.4.0")
// codacy
addSbtPlugin("com.codacy" % "sbt-codacy-coverage" % "1.3.4")
