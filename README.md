# Jmockit 1 #

[![Java CI](https://github.com/hazendaz/jmockit1/workflows/Java%20CI/badge.svg)](https://github.com/hazendaz/jmockit1/actions?query=workflow%3A%22Java+CI%22)
[![Coverage Status](https://coveralls.io/repos/github/hazendaz/jmockit1/badge.svg?branch=master)](https://coveralls.io/github/hazendaz/jmockit1?branch=master)
[![Maven central](https://maven-badges.herokuapp.com/maven-central/com.github.hazendaz.jmockit/jmockit/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.hazendaz.jmockit/jmockit)
[![MIT](https://img.shields.io/badge/license-MIT-blue.svg)](https://opensource.org/licenses/MIT)

![hazendaz](src/site/resources/images/hazendaz-banner.jpg)

Codebase for JMockit 1.x releases - [Documentation](https://jmockit.github.io) - [Historical Release notes](https://jmockit.github.io/changes.html)

All releases under 'hazendaz' since 1.49 are located in github releases

See site pages [here](https://hazendaz.github.io/jmockit1/)

How to build the project:
* use JDK 11 or newer
* use Maven 3.9.6 or newer; the following are the top-level modules:
    1. main/pom.xml: builds jmockit-1.n.jar, running JUnit 4/5 and TestNG test suites
    2. coverageTests/pom.xml: runs JUnit 5 tests for the coverage tool
    3. samples/pom.xml: various sample test suites (tutorial, LoginService, java8testing) using JUnit 5 or TestNG 7
    4. samples/petclinic/pom.xml: integration testing example using Java EE 8

This fork contains pull requests from main repo as well as updated libraries within build.

  - [665](https://github.com/jmockit/jmockit1/pull/665) from fork [vimil](https://github.com/vimil/jmockit1) condy arrayindexoutofboundsexception fix
  - [695](https://github.com/jmockit/jmockit1/pull/695) from fork [don-vip](https://github.com/don-vip/jmockit1) Fix NPE when className is null
  - [697](https://github.com/jmockit/jmockit1/pull/697) from fork [Saljack](https://github.com/Saljack/jmockit1) Fix Tested fullyInitialized instance with interfaces in constructor
  - [712](https://github.com/jmockit/jmockit1/pull/712) from fork [Saljack](https://github.com/Saljack/jmockit1) Add method name check for generic methods Expectations
  - [734](https://github.com/jmockit/jmockit1/pull/734) from fork [tsmock](https://github.com/tsmock/jmockit1) Mocks created by JUnit4 tests are not cleaned up when run with JUnit5
  - [736](https://github.com/jmockit/jmockit1/pull/736) from fork [Col-E](https://github.com/Col-E/jmockit1) Add suport for Java 11+ based off this repo
  - [68](https://github.com/hazendaz/jmockit1/pull/68) from fork [Col-E](https://github.com/Col-E/jmockit1) after sync up from PR 736.

This fork is the new home for jmockit continuation.  All pull requests are welcome including anyone that wants to bring back support that was deleted from original making upgrades difficult.

Considerations

  - Testing confirmed to work from jdk 11 through jdk 25
  - New launcher pom in root to build entire project and entire project with demos are distributed to maven central now.
