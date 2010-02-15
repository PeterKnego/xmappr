/*
 * This software is released under the BSD license. Full license available at http://xmappr.googlecode.com
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xmappr contributors
 * All rights reserved.
 */
package org.xmappr;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmappr.annotation.Attribute;
import org.xmappr.annotation.Element;
import org.xmappr.annotation.RootElement;

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
        StringReader reader = new StringReader(xml);
        Xmappr xmappr = new Xmappr(Root.class);
        Root root = (Root) xmappr.fromXML(reader);

        asserts(root);
    }

    @Test
    public void testEnumViaXML() {
        StringReader reader = new StringReader(xml);

        // Double step to make Xmappr work harder (not necessary normally - do not copy)
        // Reads Class configuration, produces XML configuration from it and then feeds it to Xmappr
        StringReader configuration = XmlConfigTester.reader(Root.class);
        Xmappr xmappr = new Xmappr(configuration);
        Root root = (Root) xmappr.fromXML(reader);

        asserts(root);
    }

    private void asserts(Root root) {
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
