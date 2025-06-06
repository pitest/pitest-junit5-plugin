# JUnit 5 Plugin 

Adds support to pitest for JUnit 5 platform test engines, e.g. Jupiter, Cucumber, or Spock.

## Versions

[![Maven Central Version](https://img.shields.io/maven-central/v/org.pitest/pitest-junit5-plugin)](https://central.sonatype.com/artifact/org.pitest/pitest-junit5-plugin)

## 1.2.3

Adds support for Quarkus 3.22.x and above.

Requires pitest 1.19.4 or above.

## Older Versions

Releases 1.2.1 and 1.2.2 requires pitest 1.15.2 or above.

Release 1.2.0 requires pitest 1.14.0 or above.

When used with the pitest-maven plugin, or version 1.15.0 of the gradle plugin, it will automatically work with JUnit platform 1.5.0 to 1.10.0-M1 (and probably above).

When used with earlier versions of the pitest gradle plugin a dependency must be added to a compatible version of junit-platform-launcher. Depending on how the gradle project is configured the must be added wither a scope of either pitest, or a testImplementation.

Older versions of the plugin must be matched to both the pitest and junit version in use as below.

* 1.1.2 requires pitest 1.9.0 or above and JUnit Platform 1.9.2 (Jupiter 5.9.2)
* 1.1.1 requires pitest 1.9.0 or above and JUnit Platform 1.9.1 (Jupiter 5.9.1)
* 1.1.0 requires pitest 1.9.0 or above and JUnit Platform 1.9.1 (Jupiter 5.9.1)
* 1.0.0 requires pitest 1.9.0 or above and JUnit Platform 1.8.x (Jupiter 5.8.0)
* 0.16 requires pitest 1.4.0 or above and JUnit Platform 1.8.x (Jupiter 5.8.0)
* 0.15 requires pitest 1.4.0 or above and JUnit Platform 1.8.x (Jupiter 5.8.0)
* 0.5-0.14 requires pitest 1.4.0 or above and JUnit Platform 1.7.x (Jupiter 5.7.x)
* 0.4 requires pitest 1.3.2 or above
* 0.3 requires pitest 1.3.0 or 1.3.1
* 0.2 requires pitest 1.2.5 

## Usage

The plugin has been built against the versions of JUnit platform noted above - you may encounter issues if you use it with a different version. 

To activate the plugin it must be placed on the classpath of the pitest tool (**not** on the classpath of the project being mutated).

### Maven

```xml
    <plugins>
      <plugin>
        <groupId>org.pitest</groupId>
        <artifactId>pitest-maven</artifactId>
        <version>1.18.2</version>
        <dependencies>
          <dependency>
            <groupId>org.pitest</groupId>
            <artifactId>pitest-junit5-plugin</artifactId>
            <version>1.2.2</version>
          </dependency>
        </dependencies>
      </plugin>
   </plugins>
```
For Pitest configuration options, have a look at http://pitest.org/quickstart/maven/.

### Gradle

```
plugins {
    id 'java'
    id 'info.solidsoft.pitest' version '1.15.0'
}

pitest {
    //adds dependency to org.pitest:pitest-junit5-plugin and sets "testPlugin" to "junit5"
    junit5PluginVersion = '1.2.2'
    pitestVersion = '1.18.2'
    // ...
}
```

See [gradle-pitest-plugin documentation](https://github.com/szpak/gradle-pitest-plugin#pit-test-plugins-support) for more configuration options.

## Release Notes

### 1.2.2

* #109 Set platform-launcher dependency to provided

The pitest maven and gradle plugins now automatically resolve the correct version of platform launcher at
runtime. The built against version of platform-launcher was however being included as a transative dependency sometimes
causing a conflict at runtime.

### 1.2.1

* #103 Report errors and failures during scan stage

### 1.1.1

* #73 Automatically disable parallel mode

### 0.16

* #64 Errors in `BeforeAll` methods do not register

## About

Plugin originally created by @tobiasstadler.
