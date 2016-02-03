// Project name (artifact name in Maven)
name := "jmx-walker"

// organization name (e.g., the package name of the project)
organization := "com.bnpparibas.grp.jmx"

version := "0.1.0-SNAPSHOT"

// project description
description := "Application that allow the discovery of a JMX Tree on a server."

// Enables publishing to maven repo
publishMavenStyle := true

/* execute 'sbt publish' to put jar in this repo: */
publishTo := Some(Resolver.file("file", new File("C:\\workspace\\maven-3.0.4\\repository")))

// Do not append Scala versions to the generated artifacts
crossPaths := false

// This forbids including Scala related libraries into the dependency
// autoScalaLibrary := false

// Default main class to run : sbt run
// the jar can be directly run with 'java -jar' command.
mainClass in (Compile, run) := Some("PUT MAIN CLASS HERE")

// For maven generated pom.
pomExtra :=
  <build>
    <plugins>
      <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>exec-maven-plugin</artifactId>
        <version>1.2.1</version>
        <executions>
          <execution>
            <goals>
              <goal>java</goal>
            </goals>
          </execution>
        </executions>
        <configuration>
          <mainClass>PUT MAIN CLASS HERE</mainClass>
        </configuration>
      </plugin>
    </plugins>
  </build>


// library dependencies. (organization name) % (project name) % (version) [% (test)]
val guava: ModuleID = "com.google.guava" % "guava" % "19.0"
val apache_commons_lang: ModuleID = "org.apache.commons" % "commons-lang3" % "3.4"
val weblogic_full_client = "bea" % "wlfullclient" % "10.3.2"

val junit: ModuleID = "junit" % "junit" % "4.11" % "test"



libraryDependencies ++= Seq(
  guava,
  apache_commons_lang,
  weblogic_full_client,
  // Test dependencies
  junit
)