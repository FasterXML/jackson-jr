package com.fasterxml.jackson.jr

import com.fasterxml.jackson.jr.ob.JSON
import org.junit.Assert
import org.junit.Test

class GroovyTest {
    @Test
    void testSimpleGroovyObject() throws Exception {
        def json = JSON.std.asString(new GroovyOb())
        def expected = """{"AAAAA_A_Field_Starting_With_Two_Capital_Letters":"XYZ","aDouble":0.0,"aPublicInitializedInteger":56,"aPublicInitializedIntegerObject":1516,"aPublicUninitializedInteger":0,"anInitializedIntegerObject":1112,"anInitializedPublicString":"stringData","anInitializedString":"ABC","anInteger":0,"anIntegerWithValue":12}"""
        Assert.assertEquals(json, expected)
    }

    @Test
    void testRecord() throws Exception {
        def json = JSON.std.asString(new Cow("foo", Map<String,String>.of("foo", "bar")))
        def expected = """{"message":"foo","object":{"foo":"bar"}}"""
        Assert.assertEquals(json, expected)
    }

    @Test
    void testRecordEquivalentObjects() throws Exception {
        def expected = """{"message":"foo","object":{"foo":"bar"}}"""

        def json = JSON.std.asString(new SurrogateCowObject("foo", Map<String,String>.of("foo", "bar")))
        Assert.assertEquals(json, expected)

        def json2 = JSON.std.asString(new SurrogateCowObject2("foo", Map<String,String>.of("foo", "bar")))
        Assert.assertEquals(json2, expected)
    }
}

class GroovyOb {
    int anInteger
    int anIntegerWithValue = 12

    static int anStaticInteger = 34
    static int anStaticIntegerWithValue = 34

    public int aPublicUninitializedInteger
    public int aPublicInitializedInteger = 56

    private int aPrivateUninitializedInteger
    private int aPrivateInitializedInteger = 78

    public static int aPublicStaticUninitializedInteger
    public static int aPublicStaticInitializedInteger = 910

    Integer anIntegerObject
    Integer anInitializedIntegerObject = 1112

    static Integer aStaticIntegerObject
    static Integer aStaticInitializedIntegerObject = 1314

    public Integer aPublicUninitializedIntegerObject
    public Integer aPublicInitializedIntegerObject = 1516

    public static Integer aPublicStaticUninitializedIntegerObject
    public static Integer aPublicStaticInitializedIntegerObject = 1718

    String aString
    String anInitializedString = "ABC"

    static String aStaticString = "jacksonJR"

    public String aPublicString
    public String anInitializedPublicString = "stringData"

    public String AAAAA_A_Field_Starting_With_Two_Capital_Letters = "XYZ"
    //Other Items
    public static String staticStr = "jacksonJR"        // Public Static Object
    static int anStaticInt                              // Uninitialized Static Object
    public double aDouble                               // uninitialized primitive
    public Double aDoubleObject                         // testing boxing object
    private int hiddenvalue = 123                       // private value
}

class SurrogateCowObject{
    public final String message;
    public final Map<String,String> object;

    SurrogateCowObject(String message,Map<String,String> object) {
        this.message = message
        this.object = object
    }
}

class SurrogateCowObject2{
    private final String message;
    private final Map<String,String> object;

    SurrogateCowObject2(String message,Map<String,String> object) {
        this.message = message
        this.object = object
    }

    String message() {
        return message
    }

    Map<String, String> object() {
        return object
    }
}
record Cow(String message, Map<String, String> object) {}
