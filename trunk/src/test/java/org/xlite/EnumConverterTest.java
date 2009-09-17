package org.xlite;

import org.testng.Assert;

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

    @org.testng.annotations.Test
    public void testEnum() {

        // do the mapping
        StringReader reader = new StringReader(xml);
        Configuration conf = new AnnotationConfiguration(Root.class, "root");
        Xlite xlite = new Xlite(conf);
        Root root = (Root) xlite.fromXML(reader);

        // check values
        Assert.assertEquals(root.simple, SimpleEnum.ONE);
        Assert.assertEquals(root.poly, PolyEnum.TEN);
    }

    /**
     * Container class
     */
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
