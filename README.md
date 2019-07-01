# JUnit 5 Plugin 

Adds support to pitest for JUnit 5 and the Jupiter api.

## Versions

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.pitest/pitest-junit5-plugin/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/org.pitest/pitest-junit5-plugin)

* 0.5-0.10 requires pitest 1.4.0 or above
* 0.4 requires pitest 1.3.2 or above
* 0.3 requires pitest 1.3.0 or 1.3.1
* 0.2 requires pitest 1.2.5 

## Usage

The plugin has been built against JUnit platform 1.5.0 - you may encounter issues if you use it with a different version. 

To activate the plugin it must be placed on the classpath of the pitest tool (**not** on the classpath of the project being mutated).

e.g for maven

```xml
    <plugins>
      <plugin>
        <groupId>org.pitest</groupId>
        <artifactId>pitest-maven</artifactId>
        <version>1.4.9</version>

        <dependencies>
          <dependency>
            <groupId>org.pitest</groupId>
            <artifactId>pitest-junit5-plugin</artifactId>
            <version>0.10-SNAPSHOT</version>
          </dependency>
        </dependencies>

        <configuration>
blah
        </configuration>
      </plugin>
   </plugins>
```

or for gradle

```
buildscript {
   repositories {
       mavenCentral()
   }
   configurations.maybeCreate("pitest")
   dependencies {
       classpath 'info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.5.0'
       pitest 'org.pitest:pitest-junit5-plugin:0.10-SNAPSHOT'
   }
}

apply plugin: "info.solidsoft.pitest"

pitest {
    pitestVersion = "1.4.9"
    testPlugin = "junit5"
    targetClasses = ['our.base.package.*']  // by default "${project.group}.*"
}
```
See [gradle-pitest-plugin documentation](http://gradle-pitest-plugin.solidsoft.info/) for more configuration options.

## About

Plugin originally created by @tobiasstadler.
