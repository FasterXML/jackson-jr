# Overview

Jackson jr is a compact alternative to full [Jackson Databind](../../../jackson-databind) component.
It implements a subset of functionality, for example for cases where:

1. Size of jar matters (jackson-jr size is about 70kB)
2. Startup time matters (jackson-jr has very low initialization overhead)

In addition to basic datatypes (core JDK types like `List`s, `Map`s, wrapper types),
package supports reading and writing of standard Java Beans (implementation that mimics standard
JDK Bean Introspection): that is,
subset of POJOs that define setters/getters; no field access is used.

Jackson jr also adds  `composer` implementation that can be used to
construct JSON output with builder-style API, but without necessarily having
to build an in-memory representation: instead, it can directly use `streaming-api`
for direct output. It will be, however, also possible to build actual in-memory
JSON `String` or `byte[]` representation, if that is preferable.

Jackson jr artifact itself is currently about 70 kB in size, and only depends on
[Jackson Streaming API](../../../jackson-core) package.
Combined size, for "all" jar under 300 kB (streaming API is about 220kB),
for use cases where a single jar is preferred over more modular approach.
Finally, use of jar minimizers like [ProGuard](http://proguard.sourceforge.net/) can bring the jar
size down even further, by renaming and removing debug information.

## Usage

### Reading/writing Simple Objects, Beans, List/arrays thereof

Functionality of this package is contained in Java package `com.fasterxml.jackson.jr.ob`.

All functionality is accessed through main `JSON` Object; you can either used singleton `JSON.std`,
or construct individual objects -- either way, `JSON` instances are ALWAYS immutable and hence thread-safe.

We can start by reading JSON

```java
String INPUT = "{\"a\":[1,2,{\"b\":true},3],\"c\":3}";
Object ob = JSON.std.anyFrom(INPUT);
// or
Map<String,Object> map = JSON.std.mapFrom(INPUT);
// or
MyBean bean = JSON.std.beanFrom(MyBean.class, INPUT);
```

from any of the usual input sources (`InputStream`, `Reader`, `String` or `byte[]` that contains JSON, `URL`,
`JsonParser`); and can write same Objects as JSON:

```java
String json = JSON.std.asString(map);
JSON.std.write(ob, new File("/tmp/stuff.json");
// and with indentation; but skip writing of null properties
byte[] bytes = JSON.std
    .with(Feature.PRETTY_PRINT_OUTPUT)
    .without(Feature.WRITE_NULL_PROPERTIES)
    .asBytes(bean);
```

and may also read `List`s and arrays of simple and Bean types:

```java
List<MyType> beans = JSON.std.listOfFrom(MyType.class, INPUT);
```

(writing of `List`s and arrays works without addition effort: just pass List/array as-is)

### Writing with composers

An alternative method exists for writing: "fluent" style output can be used as follows:

```java
String json = JSON.std
  .with(JSON.Feature.PRETTY_PRINT_OUTPUT)
  .composeString()
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

would produce (since pretty-printing is enabled)

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

### Designing your Beans

To support readability and writability of your own types, your Java objects must either:

* Implement Bean style accesors (getters for accessing data to write and/or setter for binding JSON data into objects), and define no-argument (default) constructor, OR
* Define single-argument constructor if binding from JSON String (single-String argument) or JSON integer number (single-`long` or `Long` argument)

Note that although getters and setters need to be public (since JDK Bean Introspection does not find any other methods),
constructors may have any access right, including `private`.

Also note that fields are never used for reading or writing Bean properties.

### Customizing behavior with Features

There are many customizable features you can use with `JSON` object; see [Full List of Features](../../wiki/JSON-Features) for details. But usage itself is via fluent methods like so:

```java
String json = JSON.std
  .with(JSON.Feature.PRETTY_PRINT_OUTPUT)
  .without(JSON.Feature.FAIL_ON_DUPLICATE_MAP_KEYS)
  .writeValue(...);
```

## License

Good old [Apache License](http://www.apache.org/licenses/LICENSE-2.0).

## Get it!

You can use Maven dependency like:

```xml
<dependency>
  <groupId>com.fasterxml.jackson.jr</groupId>
  <artifactId>jackson-jr-objects</artifactId>
  <version>2.4.0</version>
</dependency>
```

and then you can also download jars via [Central Maven repository](http://repo1.maven.org/maven2/com/fasterxml/jackson/jr/jackson-jr-objects/).

Or you can also clone the project and build it locally with `mvn clean install`.

Alternatively if you want a single jar deployment, you can use `jackson-jr-all` jar which embeds `jackson-core`
(repackaged using Shade plug-in, so as not to conflict with "vanilla" `jackson-core`):

    http://repo1.maven.org/maven2/com/fasterxml/jackson/jr/jackson-jr-all/

## Performance

Initial performance testing using [JVM Serializers](https://github.com/eishay/jvm-serializers/wiki) benchmark
suggests that it is almost as fast as [full Jackson databind](https://github.com/FasterXML/jackson-databind) --
additional overhead for tests is 5-10% for both serialization and deserialization.
So performance is practically identical.

In fact, when only handling `List`s and `Map`s style content, speed `jackson-jr` speed fully matches `jackson-databind`
performance (Bean/POJO case is where full databinding's extensive optimizations help more).
So performance should be adequate, and choice should be more based on functionality, convenience and
deployment factors.

About the only thing missing is that there is no equivalent to [Afterburner](../../../jackson-module-afterburner), which
can further speed up databind by 20-30%, for most performance-sensitive systems.
