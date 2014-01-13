logLevel := Level.Warn

libraryDependencies += ("com.google.javascript" % "closure-compiler" % "rr2079.1" notTransitive())
    .exclude("args4j", "args4j")
    .exclude("com.google.guava", "guava")
    .exclude("org.json", "json")
    .exclude("com.google.protobuf", "protobuf-java")
    .exclude("org.apache.ant", "ant")
    .exclude("com.google.code.findbugs", "jsr305")
    .exclude("com.googlecode.jarjar", "jarjar")
    .exclude("junit", "junit")

resolvers ++= Seq(
    DefaultMavenRepository,
    Resolver.url("Play", url("http://download.playframework.org/ivy-releases/"))(Resolver.ivyStylePatterns),
    "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
    "Sonatype OSS Snasphots" at "http://oss.sonatype.org/content/repositories/snapshots/"
)


addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.2.0")

addSbtPlugin("com.cloudbees.deploy.play" % "sbt-cloudbees-play-plugin" % "0.5-SNAPSHOT")