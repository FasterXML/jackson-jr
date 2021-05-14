## Overview

This package contains a simple extension, `JacksonAnnotationExtension` (subtype of
`JacksonJrExtension`), registering of which allows use of some of
[Core Jackson Annotations](../../../../jackson-annotations) with Jackson jr.

### Status

Added in Jackson 2.11, considered stable as of 2.12 release.

### Usage

To be able to use supported annotations, you need to register extension like so:

```java
import com.fasterxml.jackson.jr.annotationsupport.JacksonAnnotationExtension;

JSON json = JSON.builder()
    .register(JacksonAnnotationExtension.std)
    .build();
```

after which you can use normal read and write operations as usual:

```java
static class Point {
  @JsonProperty("x")
  protected int _x;

  @JsonProperty("y")
  protected int _y;

  protected Point() { } // for deserialization

  public Point(int x, int y) {
    _x = x;
    _y = y;
  }

  public int getX() { return x; }
  public int getY() { return y; }
}

// now works; without annotations would neither find fields to assign
// nor (if they were public) match the name:
Point p = json.beanFrom(Point.class, "{\"x\":1, \"y\":2}");
```

### Supported annotations

Following Jackson annotations are supported either partially or completely:

* `@JsonAlias` (complete: accessor)
* `@JsonAutoDetect` (complete: class)
    * note: merged with extension default visibility configuration, see below
* `@JsonPropertyOrder` (complete: class)
* `@JsonIgnore` (complete: accessor)
* `@JsonIgnoreProperties` (partial: class, NOT on accessors)
* `@JsonProperty` (partial: accessor, only for inclusion/renaming (other properties ignored)
    * In 2.13, will also support renaming of `Enum` constants
* `@JsonPropertyOrder` (complete: class)

Support for additional properties is possible in future versions.

### General annotation limitations

Compared to full [Jackson-databind](../../../../jackson-databind), handling of annotations is
limited in some ways for all annotations:

* No inheritance: only annotations directly associated by class or accessors (Fields,
  Methods) are applied. Jackson-databind will scan the whole inheritance hierarchy
    * In future handling of Class annotations may be improved if this seems feasible
* No support for "mix-in" annotations

### Other configuration

In addition to annotations, there is following configuration available

#### Default visibility overrides

By default Jackson-jr will only detect `public` fields, getters and setters. But it is possible to
change these defaults; both by `@JsonAutoDetect` annotation (on class) and by configuring extension like:

```java
// note: for better example see `BasicVisibilityTest.java`
// to auto-detect non-private fields:

JsonAutoDetect.Value vis = JacksonAnnotationExtension.DEFAULT_VISIBILITY
    .withFieldVisibility(JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC);
JSON json = json.builder()
    .register(JacksonAnnotationExtension.builder()
        .withVisibility(vis)
        .build()
    ).build();
```

Note that precedence is such that extension defaults are the starting point, but may be overridden and
changed by per-class annotations.

