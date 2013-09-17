# Overview

This is an experimental project that tries to provide an extremely simple ("naive"?)
and light-weight data-binder: one that only deals with basic Java `Map`s, `Collection`s
and wrapper types (`java.lang.Boolean`, `java.lang.Integer`, `java.lang.Double`),
allowing reading of JSON as "Maps, Collections and primitive wrappers"; and writing same
out as JSON.

Artifact itself is currently about `50 kB` in size, and only depends on
[Jackson Streaming API](../../../jackson-core) package.
Combined size would stay well below 300 kilobytes (streaming API is about 200kB).

## Usage

### Reading/writing Simple Objects

Functionality of this package is contained in Java package `com.fasterxml.jackson.simple.ob`.

All functionality is accessed through main `JSON` Object; you can either used singleton `JSON.std`,
or construct individual objects -- either way, `JSON` instances are ALWAYS immutable and hence thread-safe.

We can start by reading JSON

```java
String INPUT = "{\"a\":[1,2,{\"b\":true},3],\"c\":3}";
Object ob = JSON.std.from(INPUT);
// or
Map<String,Object> map = JSON.std.mapFrom(INPUT);
```

as well as writing Objects as JSON:

```java
String json = JSON.std.asString(map);
JSON.std.write(ob, new File("/tmp/stuff.json");
// and with indentation; but skip writing of null properties
byte[] bytes = JSON.std
    .with(Feature.PRETTY_PRINT_OUTPUT)
    .without(Feature.WRITE_NULL_PROPERTIES)
    .asBytes(ob);
```

Note that while reading will only produce `Map`s, `List`s, `String`s, `Boolean`s, `Number`s and null,
writing works with slightly wider range of types: Java `Enum`s, `java.util.Date`s and such will be written
as expected; and unknown types are serialized using their `toString()` method.

### Writing with composers

An alternative method exists for writing: "fluent" style output can be used as follows:

```java
String json = JSON.std.composeString()
  .startObject()
    .put("a", 1)
    .startArrayField("arr")
      .add(1).add(2).add(3)
    .end()
    .startObjectField("ob")
      .put("x", 3)
      .put("y", 4)
      .startArrayField("args").add("none").end()
    .end()
    .put("last", true)
  .end()
  .finish();
```

would produce (if pretty-printing enabled)

```json
{
  "a" : 1,
  "arr" : [1,2,3],
  "ob" : {
    "x" : 3,
    "y" : 4,
    "args" : ["none"]
  },
  "last" : true
}
```


## License

Good old [Apache License](http://www.apache.org/licenses/LICENSE-2.0).

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
