/*
 * This software is released under the BSD license. Full license available at http://xmappr.googlecode.com
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xmappr contributors
 * All rights reserved.
 */
package org.xmappr;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmappr.converters.ElementConverter;
import org.xmappr.converters.ValueConverter;

import javax.xml.namespace.QName;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

/**
 * Tests ability of ValueConverters
 */
public class CustomConverterMultiTypeTest {

    private String xml = "" +
            "<root one='1' two='2' three='3'>" +
            "<one>11</one>" +
            "<two>22</two>" +
            "<three>33</three>" +
            "</root>";

    @Test
    public void customConverterMultiTypeTest() {
        StringReader reader = new StringReader(xml);
        Xmappr xmappr = new Xmappr(Root.class);
        Root root = (Root) xmappr.fromXML(reader);
        asserts(root);
    }

    @Test
    public void customConverterMultiTypeTestViaXML() {
        StringReader reader = new StringReader(xml);

        // Double step to make Xmappr work harder (not necessary normally - do not copy)
        // Reads Class configuration, produces XML configuration from it and then feeds it to Xmappr
        StringReader configuration = XmlConfigTester.reader(Root.class);
        Xmappr xmappr = new Xmappr(configuration);
        Root root = (Root) xmappr.fromXML(reader);
        asserts(root);
    }

    private void asserts(Root root) {
        // check that Map contains three attributes
        Assert.assertEquals(root.attributes.size(), 3);

        // check that each attribute name corresponds to the right type
        Assert.assertEquals(root.attributes.get(new QName("one")).getClass(), One.class);
        Assert.assertEquals(root.attributes.get(new QName("two")).getClass(), Two.class);
        Assert.assertEquals(root.attributes.get(new QName("three")).getClass(), Three.class);
    }

    /**
     * Custom ValueConverter that can convert to multiple classes
     */
    public static class CustomMultiTypeValueConverter extends ValueConverter {

        // This converter declares itself as capable
        //  of converting three classes: One, Two and Three
        public boolean canConvert(Class type) {
            return One.class.equals(type) || Two.class.equals(type) || Three.class.equals(type);
        }

        public Object fromValue(String value, String format, Class targetType, Object targetObject) {
            if (One.class.equals(targetType)) {
                return new One(Integer.valueOf(value));
            } else if (Two.class.equals(targetType)) {
                return new Two(Integer.valueOf(value));
            } else if (Three.class.equals(targetType)) {
                return new Three(Integer.valueOf(value));
            }
            return null;
        }

        // not used
        public String toValue(Object object, String format) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }


    /**
     * Custom ElementConverter that can convert to multiple classes
     */
    public static class CustomMultiTypeElementConverter implements ElementConverter {
        // capable of converting multiple types:
        // One.class, Two.class, Three.class
        public boolean canConvert(Class type) {
            return One.class.equals(type) || Two.class.equals(type) || Three.class.equals(type);
        }


        public Object fromElement(XMLSimpleReader reader, MappingContext mappingContext, String defaultValue, String format, Class targetType, Object targetObject) {
            // read the text value of current XML element
            String value = reader.getText();

            // when ElementConverter finishes, the reader must be positioned at the end of the base element
            reader.moveUp();

            // convert depending on target type
            if (One.class.equals(targetType)) {
                return new One(Integer.valueOf(value));
            } else if (Two.class.equals(targetType)) {
                return new Two(Integer.valueOf(value));
            } else if (Three.class.equals(targetType)) {
                return new Three(Integer.valueOf(value));
            }
            return null;
        }

        // not used
        public void toElement(Object object, QName nodeName, XMLSimpleWriter writer, MappingContext mappingContext, String defaultValue, String format) {
        }
    }

    @RootElement("root")
    public static class Root {
        @Attributes({
                @Attribute(name = "one", converter = CustomMultiTypeValueConverter.class, targetType = One.class),
                @Attribute(name = "two", converter = CustomMultiTypeValueConverter.class, targetType = Two.class),
                @Attribute(name = "three", converter = CustomMultiTypeValueConverter.class, targetType = Three.class)
        })
        public Map attributes;

        @Elements({
                @Element(name = "one", converter = CustomMultiTypeElementConverter.class, targetType = One.class),
                @Element(name = "two", converter = CustomMultiTypeElementConverter.class, targetType = Two.class),
                @Element(name = "three", converter = CustomMultiTypeElementConverter.class, targetType = Three.class)
        })
        public List subelements;
    }

    public static class One {
        public int number;

        public One(int integer) {
            this.number = integer;
        }
    }

    public static class Two {
        public int number;

        public Two(int integer) {
            this.number = integer;
        }
    }

    public static class Three {
        public int number;

        public Three(int integer) {
            this.number = integer;
        }
    }
}
