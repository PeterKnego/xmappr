/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
package org.xlite;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xlite.converters.ElementConverter;
import org.xlite.converters.ValueConverter;

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
        Xlite xlite = new Xlite(Root.class);
        Root root = (Root) xlite.fromXML(reader);

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
                @Attribute(name = "one", converter = CustomMultiTypeValueConverter.class, itemType = One.class),
                @Attribute(name = "two", converter = CustomMultiTypeValueConverter.class, itemType = Two.class),
                @Attribute(name = "three", converter = CustomMultiTypeValueConverter.class, itemType = Three.class)
        })
        public Map attributes;

        @Elements({
                @Element(name = "one", converter = CustomMultiTypeElementConverter.class, itemType = One.class),
                @Element(name = "two", converter = CustomMultiTypeElementConverter.class, itemType = Two.class),
                @Element(name = "three", converter = CustomMultiTypeElementConverter.class, itemType = Three.class)
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
