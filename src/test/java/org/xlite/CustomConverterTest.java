package org.xlite;

import org.xlite.converters.ElementConverter;
import org.xlite.converters.ValueConverter;
import org.testng.Assert;
import org.testng.annotations.ExpectedExceptions;

import javax.xml.namespace.QName;
import java.io.StringReader;

/**
 * @author peter
 */
public class CustomConverterTest {

    static String xml = "" +
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
    @ExpectedExceptions(XliteException.class)
    public void customConverterTest() {

        StringReader reader = new StringReader(xml);
        Configuration conf = new AnnotationConfiguration(One.class, "one");
        Xlite xlite = new Xlite(conf);
        One one = (One) xlite.fromXML(reader);

        Assert.assertEquals(one.text, "SHOULD BE UPPER CASE"); // should be converted to upper case
        Assert.assertEquals(one.custom.getClass(), Custom.class);
        Assert.assertEquals(one.custom.value, "this is a text of a custom field");
        Assert.assertEquals(one.custom.three.attr, "should be lower case"); // should be converted to lower case
        Assert.assertEquals(one.custom.three.textField, "textThree");

    }

    public static class CustomElementConverter implements ElementConverter {

        public boolean canConvert(Class type) {
            return Custom.class.equals(type);
        }


        public Object fromElement(XMLSimpleReader reader, MappingContext mappingContext, String defaultValue) {
            Custom custom = new Custom();
            custom.value = reader.getText();
            while (reader.moveDown()) {
                if (reader.getName().getLocalPart().equals("three")) {
                    custom.three = (Three) mappingContext.processNextElement(Three.class, reader);
                }
                reader.moveUp();
            }
            return custom;
        }

        public void toElement(Object object, QName elementName, XMLSimpleWriter writer, MappingContext mappingContext, String defaultValue) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

    }

    public static class UpperCaseConverter extends ValueConverter {

        public boolean canConvert(Class type) {
            return String.class.equals(type);
        }

        public Object fromValue(String value) {
            return value.toUpperCase();
        }

        public String toValue(Object object) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }

    public static class LowerCaseConverter extends ValueConverter {

        public boolean canConvert(Class type) {
            return String.class.equals(type);
        }

        public Object fromValue(String value) {
            return value.toLowerCase();
        }

        public String toValue(Object object) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }

    public static class Custom {

        public String value;
        public Three three;
    }

    public static class One {

        @XMLtext(converter = UpperCaseConverter.class)
        public String text;

        @XMLelement(converter = CustomElementConverter.class)
        public CustomConverterTest.Custom custom;

    }

    public static class Three {

        @XMLattribute(name = "val", converter = LowerCaseConverter.class)
        public String attr;

        @XMLattribute(converter = LowerCaseConverter.class)
        // should throw an exception
        public int attr2;

        @XMLtext
        public String textField;
    }
}
