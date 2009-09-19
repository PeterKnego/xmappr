package org.xlite;

import org.testng.annotations.Test;
import org.testng.Assert;
import org.xlite.converters.ValueConverter;

import java.io.StringReader;


public class CustomConverterAssignedOnConfiguration {

    private String xml = "" +
            "<root>" +
            "<one>1.123</one>" +
            "</root>";

    @Test
    public void basicTest() {

        StringReader reader = new StringReader(xml);
        Configuration conf = new AnnotationConfiguration(Root.class);

        //
        conf.addConverter(new RoundedIntConverter());

        Xlite xlite = new Xlite(conf);

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
     * Coustom conveter that takes float as input and rounds it to int
     */
    public static class RoundedIntConverter extends ValueConverter {

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
