package com.fasterxml.jackson.jr.ob.impl;

import java.util.*;

import com.fasterxml.jackson.jr.ob.*;
import com.fasterxml.jackson.jr.ob.api.ReaderWriterModifier;

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

    public void testReadIgnoreProperty() throws Exception
    {
        // verify default read first
        final String INPUT = aposToQuotes("{'first':'Bob','last':'Burger'}");
        NameBean bean = JSON.std.beanFrom(NameBean.class, INPUT);
        assertEquals("Bob", bean.getFirst());
        assertEquals("Burger", bean.getLast());

        // but then use customized POJO introspection
        bean = JSON.std
                .with(new MyPropertyModifier("last"))
                .beanFrom(NameBean.class, INPUT);
        assertEquals("Bob", bean.getFirst());
        assertNull(bean.getLast());
        
        // and last, to ensure no leakage of customizations
        bean = JSON.std.beanFrom(NameBean.class, INPUT);
        assertEquals("Bob", bean.getFirst());
        assertEquals("Burger", bean.getLast());
    }

    public void testWriteInReverseOrder() throws Exception
    {
        // verify default write first
        final NameBean input = new NameBean("Bob", "Burger");
        final String EXP_DEFAULT = aposToQuotes("{'first':'Bob','last':'Burger'}");

        assertEquals(EXP_DEFAULT, JSON.std.asString(input));

        // but then use customized POJO introspection
        String json = JSON.std
                .with(new MyPropertyModifier("xxx"))
                .asString(input);
        assertEquals(aposToQuotes("{'last':'Burger','first':'Bob'}"), json);
        
        // and last, to ensure no leakage of customizations
        assertEquals(EXP_DEFAULT, JSON.std.asString(input));
    }

}
