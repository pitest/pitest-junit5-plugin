# JUnit 5 Plugin 

Adds support to pitest for JUnit 5 and the Juniper api.

## Usage

The plugin requires pitest 1.2.5 or later. It has been built against JUnit 5.0.2 - you may encounter issues if you use it with a different version. 

To activate the plugin it must be placed on the classpath of the pitest tool (**not** on the classpath of the project being mutated).

e.g for maven

```xml
    <plugins>
      <plugin>
        <groupId>org.pitest</groupId>
        <artifactId>pitest-maven</artifactId>
        <version>1.2.5</version>
        <dependencies>
          <dependency>
            <groupId>org.pitest</groupId>
            <artifactId>pitest-junit5-plugin</artifactId>
            <version>0.2</version>
          </dependency>
        </dependencies>

        <configuration>
blah
        </configuration>
      </plugin>
   </pluginsugin>
```

or for gradle

```
buildscript {
   repositories {
       mavenCentral()
   }
   configurations.maybeCreate("pitest")
   dependencies {
       classpath 'info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.2.4'
       pitest 'org.pitest:pitest-junit5-plugin:0.2'
   }
}

apply plugin: "info.solidsoft.pitest"

pitest {
    pitestVersion = "1.2.5"
    targetClasses = ['our.base.package.*']  // by default "${project.group}.*"
}
```
See [gradle-pitest-plugin documentation](http://gradle-pitest-plugin.solidsoft.info/) for more configuration options.

## About

Plugin originally created by @tobiasstadler.
