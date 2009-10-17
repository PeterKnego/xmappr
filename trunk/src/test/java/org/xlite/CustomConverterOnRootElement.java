package org.xlite;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xlite.converters.ValueConverter;

import java.io.StringReader;

public class CustomConverterOnRootElement {

    private String xml = "" +
            "<root>1.123</root>";

    @Test
    public void test() {

        Xlite xlite = new Xlite(RoundedInt.class);
//        xlite.addConverter(new RoundedIntValueConverter());

        RoundedInt roundedInt = (RoundedInt) xlite.fromXML(new StringReader(xml));

        Assert.assertEquals(roundedInt.value, 1);
    }

    @RootElement(name = "root", converter = RoundedIntValueConverter.class)
    public static class RoundedInt {
        public int value;
    }


    /**
     * Custom converter that takes float as input and rounds it to int
     */
    public static class RoundedIntValueConverter extends ValueConverter {

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
