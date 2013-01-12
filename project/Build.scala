import sbt._
import Keys._
import PlayProject._
import cloudbees.Plugin._

object ApplicationBuild extends Build {

    import Tasks._

    val appName         = "BreizhCamp-cfp"
    val appVersion      = "1.1"

    val appDependencies = Seq(
      "com.typesafe" %% "play-plugins-mailer" % "2.0.2",
      "org.mindrot" % "jbcrypt" % "0.3m",
      "mysql" % "mysql-connector-java" % "5.1.21",
      "securesocial" % "securesocial_2.9.1" % "2.0.8"
    )

    lazy val s = Defaults.defaultSettings ++ Seq(generateAPIDocsTask)

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA, settings = s)
    .settings(cloudBeesSettings :_*)
    .settings(CloudBees.applicationId := Some("breizhcamp/cfp")).settings(
    // Add your own project settings here
    resolvers += "Apache" at "http://repo1.maven.org/maven2/",
    resolvers += "jBCrypt Repository" at "http://repo1.maven.org/maven2/org/",
    resolvers += Resolver.url("SecureSocial Repository", url("http://securesocial.ws/repository/releases/"))(Resolver.ivyStylePatterns)
    )

    object Tasks {

        val generateAPIDocsTask = TaskKey[Unit]("app-doc") <<= (fullClasspath in Test, compilers, streams) map { (classpath, cs, s) => 
        
          IO.delete(file("documentation/api"))
          // Scaladoc
          var scalaVersionForSbt = Option(System.getProperty("scala.version")).getOrElse("2.9.1")

          val sourceFiles = 
            (file("app") ** "*.scala").get ++ 
            (file("test") ** "*.scala").get ++ 
            (file("target/scala-" + scalaVersionForSbt + "/src_managed/main/views/html") ** "*.scala").get
          new Scaladoc(10, cs.scalac)(appName + " " + appVersion + " Scala API", sourceFiles, classpath.map(_.data), file("documentation/api/scala"), Nil, s.log)
          
          // Javadoc
          val javaSources = Seq(file("app"), file("test")).mkString(":")
          val javaApiTarget = file("documentation/api/java")
          val javaClasspath = classpath.map(_.data).mkString(":")
          val javaPackages = "controllers:models"

          val cmd = <x>javadoc -windowtitle {appName} -doctitle {"\"" + appName + "&nbsp;" + appVersion + "&nbsp;Java&nbsp;API\""} -sourcepath {javaSources} -d {javaApiTarget} -subpackages {javaPackages} -classpath {javaClasspath}</x>
          //println("Executing: "+cmd.text)
          cmd ! s.log

          println(appName + " " + appVersion + " Documentation generated under documentation/api/ ")
        }
    }

}


      
      
      

      
