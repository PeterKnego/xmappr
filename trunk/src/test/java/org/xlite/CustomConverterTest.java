/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
package org.xlite;

import org.testng.Assert;
import org.testng.annotations.ExpectedExceptions;
import org.xlite.converters.ElementConverter;
import org.xlite.converters.ValueConverter;

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

    @org.testng.annotations.Test()
    public void customConverterTest() {

        StringReader reader = new StringReader(xml);
        Configuration conf = new AnnotationConfiguration(One.class);
        Xlite xlite = new Xlite(conf);
        One one = (One) xlite.fromXML(reader);

        Assert.assertEquals(one.text, "SHOULD BE UPPER CASE"); // should be converted to upper case
        Assert.assertEquals(one.custom.getClass(), Custom.class);
        Assert.assertEquals(one.custom.value, "this is a text of a custom field");
        Assert.assertEquals(one.custom.three.attr, "should be lower case"); // should be converted to lower case
        Assert.assertEquals(one.custom.three.textField, "textThree");

    }

    @org.testng.annotations.Test()
    @ExpectedExceptions(XliteException.class)
    public void wrongConverterTypeTest() {

        StringReader reader = new StringReader(xml);
        Configuration conf = new AnnotationConfiguration(Wrong.class);
        Xlite xlite = new Xlite(conf);
        xlite.fromXML(reader);
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
