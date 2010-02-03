/*
 * This software is released under the BSD license. Full license available at http://xmappr.googlecode.com
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xmappr contributors
 * All rights reserved.
 */
package org.xmappr;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmappr.converters.ValueConverter;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Testing a custom ValueConverter that changes directly targetObject passed to it.
 */
public class CustomConverterChangesTargetTest {

    static final String xml1 = "<one>1,2,3</one>";
    static final String xml2 = "<one>0,1,2,3</one>";

     @Test
    public void basicTest() {
        StringReader reader = new StringReader(xml1);
        Xmappr xmappr = new Xmappr(One.class);
        xmappr.setPrettyPrint(false);

        One one = (One) xmappr.fromXML(reader);
        asserts(xmappr, one);
    }

    @Test
    public void basicTestViaXML() {
        StringReader reader = new StringReader(xml1);

        // Double step to make Xmappr work harder (not necessary normally - do not copy)
        // Reads Class configuration, produces XML configuration from it and then feeds it to Xmappr
        StringReader configuration = XmlConfigTester.reader(One.class);
        Xmappr xmappr = new Xmappr(configuration);
        xmappr.setPrettyPrint(false);

        One one = (One) xmappr.fromXML(reader);
        asserts(xmappr, one);
    }

    private void asserts(Xmappr xmappr, One one) {
        Assert.assertEquals(one.getValues().size(), 4);
        Assert.assertEquals((int) one.getValues().get(0), 0);
        Assert.assertEquals((int) one.getValues().get(1), 1);
        Assert.assertEquals((int) one.getValues().get(2), 2);
        Assert.assertEquals((int) one.getValues().get(3), 3);

        Writer w = new StringWriter();
        xmappr.toXML(one, w);
        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertEquals(xml2, w.toString());
    }

    @RootElement("one")
    public static class One {
        private List<Integer> values = new ArrayList<Integer>();

        public One() {
            values.add(0);
        }

        @Text(converter = IntegerCollectionConverter.class)
        public List<Integer> getValues() {
            return values;
        }

        @Text(converter = IntegerCollectionConverter.class)
        public void setValues(List<Integer> values) {
            this.values = values;
        }
    }

    /**
     * Custom ValueConverter that changes the targetObject passed to it.
     */
    public static class IntegerCollectionConverter extends ValueConverter {
        public boolean canConvert(Class type) {
            return Collection.class.isAssignableFrom(type);
        }

        public Object fromValue(String value, String format, Class targetType, Object targetObject) {

            // targetObject must be a Collection
            if (!Collection.class.isAssignableFrom(targetObject.getClass())) {
                return null;
            }

            // cast to collection
            Collection collection = (Collection) targetObject;

            // perform direct manupulation of target object
            if (value != null && value.length() > 0) {
                String[] values = value.split(",");
                for (String v : values) {
                    collection.add(Integer.valueOf(v));
                }
            }
            return null;
        }

        public String toValue(Object object, String format) {
            StringBuilder builder = new StringBuilder();
            Collection<Integer> collection = (Collection<Integer>) object;
            for (Integer integer : collection) {
                builder.append(",").append(integer);
            }
            return collection.isEmpty() ? null : builder.substring(1);
        }
    }

}