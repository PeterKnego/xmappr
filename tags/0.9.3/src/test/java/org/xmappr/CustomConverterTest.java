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

public class CustomConverterTest {

    private static String xml = "" +
            "<one>" +
            "should be upper case" +
            "<custom>" +
            "this is a text of a custom field" +
            "<three val=\"SHOULD BE LOWER CASE\" attr2=\"42\">" +
            "textThree" +
            "</three>" +
            "<ignored>this node is ignored</ignored>" +
            "</custom>" +
            "</one>";

    @Test
    public void customConverterTest() {
        StringReader reader = new StringReader(xml);
        Xmappr xmappr = new Xmappr(One.class);
        xmappr.addMapping(Three.class);

        One one = (One) xmappr.fromXML(reader);
        asserts(one);
    }

    @Test
    public void customConverterTestViaXML() {
        StringReader reader = new StringReader(xml);

        // Double step to make Xmappr work harder (not necessary normally - do not copy)
        // Reads Class configuration, produces XML configuration from it and then feeds it to Xmappr
        StringReader configuration = XmlConfigTester.reader(One.class);
        Xmappr xmappr = new Xmappr(configuration);
        xmappr.addMapping(Three.class);

        One one = (One) xmappr.fromXML(reader);
        asserts(one);
    }

    private void asserts(One one) {
        Assert.assertEquals(one.text, "SHOULD BE UPPER CASE"); // should be converted to upper case
        Assert.assertEquals(one.custom.getClass(), Custom.class);
        Assert.assertEquals(one.custom.value, "this is a text of a custom field");
        Assert.assertEquals(one.custom.three.attr, "should be lower case"); // should be converted to lower case
        Assert.assertEquals(one.custom.three.textField, "textThree");
    }

    @Test(expectedExceptions = XmapprException.class)
    public void wrongConverterTypeTest() {

        StringReader reader = new StringReader(xml);

        // Double step to make Xmappr work harder (not necessary normally - do not copy)
        // Reads Class configuration, produces XML configuration from it and then feeds it to Xmappr
        StringReader configuration = XmlConfigTester.reader(Wrong.class);
        Xmappr xmappr = new Xmappr(configuration);
        xmappr.fromXML(reader);
    }

    public static class CustomElementConverter implements ElementConverter {

        public boolean canConvert(Class type) {
            return Custom.class.equals(type);
        }


        public Object fromElement(XMLSimpleReader reader, MappingContext mappingContext, String defaultValue, String format, Class targetType, Object targetObject) {
            Custom custom = new Custom();
            custom.value = reader.getText();
            while (reader.moveDown()) {
                if (reader.getName().getLocalPart().equals("three")) {
                    Three tmp = (Three) mappingContext.processNextElement(Three.class, custom.three, reader, null, null);
                    if (tmp != null) custom.three = tmp;
                }
                reader.moveUp();
            }
            return custom;
        }

        public void toElement(Object object, QName elementName, XMLSimpleWriter writer, MappingContext mappingContext, String defaultValue, String format) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

    }

    public static class UpperCaseConverter extends ValueConverter {

        public boolean canConvert(Class type) {
            return String.class.equals(type);
        }

        public Object fromValue(String value, String format, Class targetType, Object targetObject) {
            return value.toUpperCase();
        }

        public String toValue(Object object, String format) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }

    public static class LowerCaseConverter extends ValueConverter {

        public boolean canConvert(Class type) {
            return String.class.equals(type);
        }

        public Object fromValue(String value, String format, Class targetType, Object targetObject) {
            return value.toLowerCase();
        }

        public String toValue(Object object, String format) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }

    public static class Custom {

        public String value;
        public Three three;
    }

    @RootElement("one")
    public static class One {

        @Text(converter = UpperCaseConverter.class)
        public String text;

        @Element(converter = CustomElementConverter.class)
        public CustomConverterTest.Custom custom;

    }

    @RootElement("three")
    public static class Three {

        @Attribute(name = "val", converter = LowerCaseConverter.class)
        public String attr;

        @Text
        public String textField;
    }


    @RootElement("one")
    public static class Wrong {
        // should throw an exception
        @Attribute(converter = LowerCaseConverter.class)
        public int attr;
    }
}
