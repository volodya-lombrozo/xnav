# xnav

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.volodya-lombrozo/xnav/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.volodya-lombrozo/xnav)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](https://github.com/volodya-lombrozo/xnav/blob/main/LICENSE.txt)
[![Hits-of-Code](https://hitsofcode.com/github/volodya-lombrozo/xnav?branch=main&label=Hits-of-Code)](https://hitsofcode.com/github/volodya-lombrozo/xnav/view?branch=main&label=Hits-of-Code)
[![codecov](https://codecov.io/gh/volodya-lombrozo/xnav/branch/main/graph/badge.svg)](https://codecov.io/gh/volodya-lombrozo/xnav)

Xnav is a library designed for seamless navigation and querying of XML
documents, inspired by the simplicity and elegance of XPath. With Xnav,
developers can easily traverse and extract information from XML structures.

## Installation

The library is available on Maven Central. To add it to your project, include
the following snippet in your `pom.xml`:

```xml

<dependency>
  <groupId>com.github.volodya-lombrozo</groupId>
  <artifactId>xnav</artifactId>
  <version>0.0.4</version>
</dependency>
```

## How to Use

Xnav provides a fluent API for navigating and querying XML. Here's a basic
example of how to use it:

```java

public class XnavExample {
    public static void main(String[] args) {
        System.out.println(
            new Navigator("<root><item key='value'/></root>")
                .child("root")
                .child("item")
                .attr("key")
                .text()
                .orElse("default")
        ); // Output: value
    }
}
```

## Contribution

Fork repository, make changes, send us a pull request. We will review your
changes and apply them to the `main` branch shortly, provided they don't violate
our quality standards. To avoid frustration,
before sending us your pull request, please run full Maven build:

```bash
$ mvn clean install -Pqulice
```

You will need [Maven 3.3+](https://maven.apache.org) and Java 11+ installed.