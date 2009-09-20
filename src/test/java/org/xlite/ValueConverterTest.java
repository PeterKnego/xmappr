package org.xlite;

import org.testng.annotations.Test;
import org.testng.Assert;
import org.xlite.converters.ValueConverter;

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
        Configuration conf = new AnnotationConfiguration(Root.class);
        Xlite xlite = new Xlite(conf);

        Root root = (Root) xlite.fromXML(reader);

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
