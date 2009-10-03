/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
package org.xlite;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.StringReader;

/**
 * Tests the EnumConverter
 * Both for simple enums and Polymorphic enums
 */
public class EnumConverterTest {

    String xml = "" +
            "<root simple='ONE'>" +
            "  <poly>TEN</poly>" +
            "</root>";

    @Test
    public void testEnum() {

        // do the mapping
        StringReader reader = new StringReader(xml);
        Xlite xlite = new Xlite(Root.class);
        Root root = (Root) xlite.fromXML(reader);

        // check values
        Assert.assertEquals(root.simple, SimpleEnum.ONE);
        Assert.assertEquals(root.poly, PolyEnum.TEN);
    }

    /**
     * Container class
     */
    @RootElement("root")
    public static class Root {
        @Attribute
        public SimpleEnum simple;

        @Element
        public PolyEnum poly;
    }

    /**
     * Just a simple enum
     */
    enum SimpleEnum {
        ONE, TWO, THREE
    }

    /**
     * Polymorphic enum is an enum that implements an additional interface.
     * JVM handles this in a quirky way: they appear as if they are an inner class
     */
    enum PolyEnum implements Iface {
        TEN() {
            public String exponent() {
                return "1";
            }
        },
        TWENTY() {
            public String exponent() {
                return "2";
            }
        }
    }

    public static interface Iface {
        public String exponent();
    }

}
