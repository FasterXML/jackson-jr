## Overview

This module extends the functionality of jackson-jr by adding support for (a subset of) Java 8 Date/Time types (value types in JDK `java.time` package).

### Status

Added in Jackson 2.17.

### Usage
To be able to use supported annotations, you need to register extension like so:
```java
    private static final JSON JACKSON = JSON.builder()
        .register(new JacksonJrJavaTimeExtension())
        .build();
```
after which you can use normal read and write operations as usual:

```java
import java.time.LocalDateTime;

public class Application {
    public static void main(String[] args) {
        final LocalDateTime now = LocalDateTime.now();
        MyClass myObject = new MyClass(now, 'Some Other Values....');
        String myObjectJsonString = JACKSON.asString(myObject);
        MyClass myObjectFromJson = JACKSON.beanFrom(MyClass, myObjectJsonString);
        assert myObjectFromJson.getTime().equals(now);
    }
}

// ...

public class MyClass {
    private LocalDateTime time;
    private String otherItems;

    public MyClass(LocalDateTime datetime, String others) {
        //...
    }

    public LocalDateTime getTime() {
        return time;
    }
    // other getters & setters
}
```

### Date Classes currently supported by `JacksonJrJavaTimeExtension`

- `java.time.LocalDateTime`

### Plans for Future

- Add support for other Java 8 Date/Time types
