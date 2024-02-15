## Overview

This module extends the functionalities of jackson-jr adding support for a subset of Java 8 Date & Time objects such as `LocalDateTime` & `DateTime`.

### Example

```java
import com.fasterxml.jackson.jr.extension.datetime.JacksonLocalDateTimeExtension;
import com.fasterxml.jackson.jr.ob.JSON;

import java.time.LocalDateTime;

class Application {
    private static final JSON JACKSON = JSON.builder()
            .register(new JacksonLocalDateTimeExtension())
            .build();

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
