import org.junit.Assert
import org.junit.Test

import tools.jackson.jr.ob.JSON

/**
 * A minor note on running/debugging this test on local, if you are using intellij, please
 * change `<packaging>pom</packaging>` to `<packaging>bundle</packaging>`. this is causing
 * some issue with the IDE.
 */
class GroovyObjectSupportTest {
    @Test
    void testSimpleGroovyObject() throws Exception {
        def json = JSON.std.asString(new GroovyOb())
        def expected = """{"AAAAA_A_Field_Starting_With_Two_Capital_Letters":"XYZ","aDouble":0.0,"aPublicInitializedInteger":56,"aPublicInitializedIntegerObject":1516,"aPublicUninitializedInteger":0,"anInitializedIntegerObject":1112,"anInitializedPublicString":"stringData","anInitializedString":"ABC","anInteger":0,"anIntegerWithValue":12}"""
        Assert.assertEquals(json, expected)
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
