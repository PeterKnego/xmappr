/*
 * This software is released under the BSD license. Full license available at http://xmappr.googlecode.com
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xmappr contributors
 * All rights reserved.
 */
package org.xmappr;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmappr.converters.ValueConverter;

import java.io.StringReader;


public class CustomConverterAssignedOnConfigurationTest {

    private String xml = "" +
            "<root>" +
            "<one>1.123</one>" +
            "</root>";

    @Test
    public void test() {
        StringReader reader = new StringReader(xml);
        Xmappr xmappr = new Xmappr(Root.class);
        xmappr.addConverter(new RoundedIntValueConverter());

        Root root = (Root) xmappr.fromXML(reader);
        Assert.assertEquals(root.one.value, 1);
    }

    @RootElement
    public static class Root {
        @Element
        public RoundedInt one;
    }

    /**
     * Custom class that RoundingIntConverter is converting
     */
    public static class RoundedInt {
        public int value;
    }

    /**
     * Custom converter that takes float as input and rounds it to int
     */
    public static class RoundedIntValueConverter extends ValueConverter {
        public boolean canConvert(Class type) {
            return RoundedInt.class.isAssignableFrom(type);
        }

        /**
         * Method that does the rounding.
         *
         * @param xmlValue
         * @param format
         * @param targetType
         * @param targetObject
         * @return
         */
        public Object fromValue(String xmlValue, String format, Class targetType, Object targetObject) {
            RoundedInt ri = new RoundedInt();
            ri.value = Double.valueOf(xmlValue).intValue();
            return ri;
        }

        public String toValue(Object object, String format) {
            return ((Integer) object).toString();
        }
    }

}
