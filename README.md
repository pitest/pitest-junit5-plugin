# JUnit 5 Plugin 

Adds support to pitest for JUnit 5 platform test engines, e.g. Jupiter, Cucumber, or Spock.

## Versions

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.pitest/pitest-junit5-plugin/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/org.pitest/pitest-junit5-plugin)

Release 1.2.0 requires pitest 1.14.0 or above.

When used with the pitest-maven plugin, it will automatically work with JUnit platform 1.5.0 to 1.10.0-M1 (and probably above).

When used with the pitest gradle plugin, it will automatically work with JUnit platform 1.9.2. To work with other versions an explicit dependency
on junit-platform-launcher must be added to the project under test.

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
        <version>1.14.2</version>
        <dependencies>
          <dependency>
            <groupId>org.pitest</groupId>
            <artifactId>pitest-junit5-plugin</artifactId>
            <version>1.2.0</version>
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
    id 'info.solidsoft.pitest' version '1.7.4'
}

pitest {
    //adds dependency to org.pitest:pitest-junit5-plugin and sets "testPlugin" to "junit5"
    junit5PluginVersion = '1.2.0'
    pitestVersion = '1.14.2'
    // ...
}
```

See [gradle-pitest-plugin documentation](https://github.com/szpak/gradle-pitest-plugin#pit-test-plugins-support) for more configuration options.

## Release Notes

### 1.1.1

* #73 Automatically disable parallel mode

### 0.16

* #64 Errors in `BeforeAll` methods do not register

## About

Plugin originally created by @tobiasstadler.
