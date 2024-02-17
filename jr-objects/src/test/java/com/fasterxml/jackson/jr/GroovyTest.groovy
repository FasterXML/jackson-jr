package com.fasterxml.jackson.jr

import com.fasterxml.jackson.jr.ob.JSON
import com.fasterxml.jackson.jr.ob.TestBase

class GroovyTest extends TestBase {

    void testSimpleObject() throws Exception {
        var data = JSON.std.asString(new MyClass())
        var expected = "{\"aDouble\":0.0,\"aStr\":\"stringData\",\"anInt\":0,\"metaClass\":{}}";
        assertEquals(data, expected)
    }

    private class MyClass {
        public int anInt;                       //testing groovy primitive
        public String aStr = "stringData";      //testing allocated object

        public double aDouble;                  //
        public Double aDoublesd;                //testing boxing object
    }
}
