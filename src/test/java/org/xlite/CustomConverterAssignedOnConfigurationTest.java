/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
package org.xlite;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xlite.converters.ValueConverter;

import java.io.StringReader;


public class CustomConverterAssignedOnConfigurationTest {

    private String xml = "" +
            "<root>" +
            "<one>1.123</one>" +
            "</root>";

    @Test
    public void test() {

        StringReader reader = new StringReader(xml);
        Xlite xlite = new Xlite(Root.class);
        xlite.addConverter(new RoundedIntValueConverter());

        Root root = (Root) xlite.fromXML(reader);

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
