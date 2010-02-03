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

/**
 * Tests using ValuConverter on a @Element annotation. XLT-52
 */
public class ValueConverterTest {

    private String xml = "" +
            "<root>" +
            "<one>1.123</one>" +
            "</root>";

    @Test
    public void basicTest() {

        StringReader reader = new StringReader(xml);

        // Double step to make Xmappr work harder (not necessary normally - do not copy)
        // Reads Class configuration, produces XML configuration from it and then feeds it to Xmappr
        StringReader configuration = XmlConfigTester.reader(Root.class);
        Xmappr xmappr = new Xmappr(configuration);

        Root root = (Root) xmappr.fromXML(reader);

        Assert.assertEquals(root.one, 1);
    }

    @RootElement
    public static class Root {

        @Element(converter = RoundingIntValueConverter.class)
        public int one;

    }

    /**
     * Coustom conveter that takes float as inlut and rounds it to int
     */
    public static class RoundingIntValueConverter extends ValueConverter {

        public boolean canConvert(Class type) {
            return type.equals(int.class) || Integer.class.isAssignableFrom(type);
        }

        /**
         * Method that does the rounding.
         * @param xmlValue
         * @param format
         * @param targetType
         * @param targetObject
         * @return
         */
        public Object fromValue(String xmlValue, String format, Class targetType, Object targetObject) {
            return Double.valueOf(xmlValue).intValue();
        }

        public String toValue(Object object, String format) {
            return ((Integer) object).toString();
        }
    }


}
