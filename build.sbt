
// Project name (artifact name in Maven)
name := "jmx-walker"

// organization name (e.g., the package name of the project)
organization := "com.github.morinb"

version := "0.1.5-SNAPSHOT"

// project description
description := "Application that allow the discovery of a JMX Tree on a server."

// Enables publishing to maven repo
publishMavenStyle := true

/* execute 'sbt publish' to put jar in this repo: */
publishTo := Some(Resolver.file("file", new File("C:\\workspace\\maven-3.0.4\\repository")))

// Do not append Scala versions to the generated artifacts
crossPaths := false

// This forbids including Scala related libraries into the dependency (for java only projects)
// autoScalaLibrary := false

// Default main class to run : sbt run
// the jar can be directly run with 'java -jar' command.
mainClass in(Compile, run) := Some("com.github.morinb.jmx.walker.JmxWalkerMain")

pomExtra := <build>
  <pluginManagement>
    <plugins>
      <plugin>
        <groupId>org.scala-tools</groupId>
        <artifactId>maven-scala-plugin</artifactId>
        <version>2.15.2</version>
      </plugin>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.1</version>
      </plugin>
      <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>build-helper-maven-plugin</artifactId>
        <version>1.9</version>
      </plugin>
    </plugins>
  </pluginManagement>
  <plugins>
    <plugin>
      <groupId>org.scala-tools</groupId>
      <artifactId>maven-scala-plugin</artifactId>
      <executions>
        <execution>
          <id>scala-compile-first</id>
          <phase>process-resources</phase>
          <goals>
            <goal>add-source</goal>
            <goal>compile</goal>
          </goals>
        </execution>
        <execution>
          <id>scala-test-compile</id>
          <phase>process-test-resources</phase>
          <goals>
            <goal>testCompile</goal>
          </goals>
        </execution>
      </executions>
      <configuration>
        <jvmArgs>
          <jvmArg>-Xms64m</jvmArg>
          <jvmArg>-Xmx1024m</jvmArg>
        </jvmArgs>
      </configuration>
    </plugin>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-compiler-plugin</artifactId>
      <executions>
        <execution>
          <phase>compile</phase>
          <goals>
            <goal>compile</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
    <plugin>
      <groupId>org.codehaus.mojo</groupId>
      <artifactId>build-helper-maven-plugin</artifactId>
      <executions>
        <execution>
          <id>add-source</id>
          <phase>generate-sources</phase>
          <goals>
            <goal>add-source</goal>
          </goals>
          <configuration>
            <sources>
              <source>src/main/scala</source>
            </sources>
          </configuration>
        </execution>
        <execution>
          <id>add-test-source</id>
          <phase>generate-sources</phase>
          <goals>
            <goal>add-test-source</goal>
          </goals>
          <configuration>
            <sources>
              <source>src/test/scala</source>
            </sources>
          </configuration>
        </execution>
      </executions>
    </plugin>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-shade-plugin</artifactId>
      <version>2.3</version>
      <executions>
        <!-- Run shade goal on package phase -->
        <execution>
          <phase>package</phase>
          <goals>
            <goal>shade</goal>
          </goals>
          <configuration>
            <transformers>
              <!-- add Main-Class to manifest file -->
              <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                <mainClass>com.github.morinb.jmx.walker.JmxWalkerMain</mainClass>
              </transformer>
            </transformers>
          </configuration>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
  <repositories>
    <repository>
      <id>scala-tools.org</id>
      <name>Scala-tools Maven2 Repository</name>
      <url>http://scala-tools.org/repo-releases</url>
    </repository>
  </repositories>
  <pluginRepositories>
    <pluginRepository>
      <id>scala-tools.org</id>
      <name>Scala-tools Maven2 Repository</name>
      <url>http://scala-tools.org/repo-releases</url>
    </pluginRepository>
  </pluginRepositories>

// library dependencies. (organization name) % (project name) % (version) [% (test)]
val guava: ModuleID = "com.google.guava" % "guava" % "19.0"
val apache_commons_lang: ModuleID = "org.apache.commons" % "commons-lang3" % "3.4"
val weblogic_full_client = "bea" % "wlfullclient" % "10.3.2"

val junit: ModuleID = "junit" % "junit" % "4.11" % "test"
val scalatest: ModuleID = "org.scalatest" % "scalatest_2.10" % "2.0" % "test"
val jline: ModuleID = "jline" % "jline" % "2.13"


libraryDependencies ++= Seq(
  guava,
  apache_commons_lang,
  weblogic_full_client,
  jline,
  // Test dependencies
  junit,
  scalatest
)