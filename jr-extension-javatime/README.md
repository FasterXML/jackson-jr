## Overview

This module extends the functionalities of jackson-jr adding support for a subset of Java 8 Date & Time objects such as `LocalDateTime` & `DateTime`.

### Status
Added in Jackson 2.17.

### Usage
To be able to use supported annotations, you need to register extension like so:
```java
    private static final JSON JACKSON = JSON.builder()
        .register(new JacksonLocalDateTimeExtension())
        .build();
```
after which you can use normal read and write operations as usual:

```java


import java.time.LocalDateTime;

class Application {

    public static void main(String[] args) {
        // Create the class that we need to  
        final LocalDateTime now = LocalDateTime.now();
        MyClass myObject = new MyClass(now, 'Some Other Values....');
        String myObjectJsonString = JACKSON.asString(myObject);
        MyClass myObjectFromJson = JACKSON.beanFrom(MyClass, myObjectJsonString);
        assert myObjectFromJson.time == now;
    }

}
```

### Extension List
- LocalDateTime (`JacksonLocalDateTimeExtension`)

### Plans for Future
- Other Java 8 DateTime classes
