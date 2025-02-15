package com.fasterxml.jackson.jr.ob.impl;

import java.util.*;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.jr.ob.*;
import com.fasterxml.jackson.jr.ob.api.ReaderWriterModifier;

import static org.junit.jupiter.api.Assertions.*;

public class POJODefinitionOverrideTest extends TestBase
{
    static class MyPropertyModifier extends ReaderWriterModifier
    {
        private final String _toDrop;
        
        public MyPropertyModifier(String toDrop) {
            _toDrop = toDrop;
        }

        @Override
        public POJODefinition pojoDefinitionForDeserialization(JSONReader readContext,
                Class<?> pojoType)
        {
            POJODefinition def = BeanPropertyIntrospector.instance().pojoDefinitionForDeserialization(readContext, pojoType);
            List<POJODefinition.Prop> newProps = new ArrayList<POJODefinition.Prop>();
            for (POJODefinition.Prop prop : def.getProperties()) {
                if (!_toDrop.equals(prop.name)) {
                    newProps.add(prop);
                }
            }
            return def.withProperties(newProps);
        }

        @Override
        public POJODefinition pojoDefinitionForSerialization(JSONWriter writeContext,
                Class<?> pojoType)
        {
            POJODefinition def = BeanPropertyIntrospector.instance().pojoDefinitionForSerialization(writeContext, pojoType);
            // and then reverse-order
            Map<String, POJODefinition.Prop> newProps = new TreeMap<String, POJODefinition.Prop>(Collections.reverseOrder());
            for (POJODefinition.Prop prop : def.getProperties()) {
                newProps.put(prop.name, prop);
            }
            return def.withProperties(newProps.values());
        }
    }

    static class NoOpModifier extends ReaderWriterModifier { }

    @Test
    public void testReadIgnoreProperty() throws Exception
    {
        // verify default read first
        final String INPUT = a2q("{'first':'Bob','last':'Burger'}");
        NameBean bean = JSON.std.beanFrom(NameBean.class, INPUT);
        assertEquals("Bob", bean.getFirst());
        assertEquals("Burger", bean.getLast());

        // but then use customized POJO introspection
        bean = jsonWithModifier(new MyPropertyModifier("last"))
                .beanFrom(NameBean.class, INPUT);
        assertEquals("Bob", bean.getFirst());
        assertNull(bean.getLast());
        
        // and last, to ensure no leakage of customizations
        bean = JSON.std.beanFrom(NameBean.class, INPUT);
        assertEquals("Bob", bean.getFirst());
        assertEquals("Burger", bean.getLast());
    }

    @Test
    public void testModifierPairForReading() throws Exception
    {
        final String INPUT = a2q("{'first':'Bob','last':'Burger'}");
        NameBean bean = jsonWithModifiers(new NoOpModifier(), new MyPropertyModifier("last"))
                .beanFrom(NameBean.class, INPUT);
        assertEquals("Bob", bean.getFirst());
        assertNull(bean.getLast());

        // or with null
        bean = jsonWithModifiers(null, new MyPropertyModifier("last"))
                .beanFrom(NameBean.class, INPUT);
        assertEquals("Bob", bean.getFirst());
        assertNull(bean.getLast());
    }

    @Test
    public void testWriteInReverseOrder() throws Exception
    {
        // verify default write first
        final NameBean input = new NameBean("Bob", "Burger");
        final String EXP_DEFAULT = a2q("{'first':'Bob','last':'Burger'}");

        assertEquals(EXP_DEFAULT, JSON.std.asString(input));

        // but then use customized POJO introspection
        String json = jsonWithModifier(new MyPropertyModifier("xxx"))
                .asString(input);
        assertEquals(a2q("{'last':'Burger','first':'Bob'}"), json);
        
        // and last, to ensure no leakage of customizations
        assertEquals(EXP_DEFAULT, JSON.std.asString(input));
    }

    @Test
    public void testModifierPairForWriting() throws Exception
    {
        final NameBean input = new NameBean("Bill", "Burger");

        String json = jsonWithModifiers(new NoOpModifier(), new MyPropertyModifier("xxx"))
                .asString(input);
        assertEquals(a2q("{'last':'Burger','first':'Bill'}"), json);

        // and nulls fine too wrt chaining
        json = jsonWithModifiers(null, new MyPropertyModifier("xxx"))
                .asString(input);
        assertEquals(a2q("{'last':'Burger','first':'Bill'}"), json);
    }
}
