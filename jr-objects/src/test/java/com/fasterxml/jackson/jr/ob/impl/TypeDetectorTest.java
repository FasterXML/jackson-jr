package com.fasterxml.jackson.jr.ob.impl;

import java.util.*;

import com.fasterxml.jackson.jr.ob.*;

public class TypeDetectorTest extends TestBase
{
    static class TestBean {
        public int getX() { return 1; }
        public void setX(int x) { }

        public String getName() { return null; }

        public void setY(int y) { }

        public int bebop() { return 3; }
        public void zipdoo(int value) { }

        // not a regular getter (could be indexed)
        public void setEmUp(int index, String value) { }
    }

    public void testSimple() 
    {
        TypeDetector td = TypeDetector.rootDetector(JSON.Feature.defaults());
        BeanDefinition def = td._resolveBean(TestBean.class);
        assertNotNull(def);

        List<BeanProperty> props = Arrays.asList(def._properties);
        assertEquals(3, props.size());
        Map<String, BeanProperty> map = new HashMap<String, BeanProperty>();
        for (BeanProperty prop : props) {
            map.put(prop._name.getValue(), prop);
        }

        BeanProperty prop;

        prop = map.get("x");
        assertNotNull(prop);
        assertNotNull(prop._getMethod);
        assertNotNull(prop._setMethod);
        prop = map.get("y");
        assertNotNull(prop);
        assertNull(prop._getMethod);
        assertNotNull(prop._setMethod);
        prop = map.get("name");
        assertNotNull(prop);
        assertNotNull(prop._getMethod);
        assertNull(prop._setMethod);
    }
}
