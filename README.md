# Overview

This is an experimental project that tries to provide an extremely simple ("naive"?)
and light-weight data-binder: one that only deals with basic Java `Map`s, `Collection`s
and wrapper types (`java.lang.Boolean`, `java.lang.Integer`, `java.lang.Double`),
allowing reading of JSON as "Maps, Collections and primitive wrappers"; and writing same
out as JSON.

Artifact only depends on [Jackson Streaming API](../../../jackson-core) package, so
combined size would stay well below 300 kilobytes (streaming API is tad below 200kB).

## License

Good old [Apache License](http://www.apache.org/licenses/LICENSE-2.0).

## Usage

Functionality of this package is contained in Java package `com.fasterxml.jackson.simple.ob`

USAGE EXAMPLE TO FOLLOW ONCE CODE IS READY.

## Get it!

Most commonly this package is used via Maven; if so, dependency is:

```xml
<dependency>
  <groupId>com.fasterxml.jackson.simple</groupId>
  <artifactId>jackson-simple-objects</artifactId>
  <version>2.3.0-SNAPSHOT</version>
</dependency>
```

but you can also download jars via [Central Maven repository](http://repo1.maven.org/maven2/com/fasterxml/jackson/simple/)
