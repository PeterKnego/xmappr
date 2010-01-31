package org.xmappr;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmappr.converters.ValueConverter;

import java.io.StringReader;

public class CustomConverterOnRootElement {

    private String xml = "" +
            "<root>1.123</root>";

    @Test
    public void test() {
        Xmappr xmappr = new Xmappr(RoundedInt.class);
        RoundedInt roundedInt = (RoundedInt) xmappr.fromXML(new StringReader(xml));

        Assert.assertEquals(roundedInt.value, 1);
    }

    @Test
    public void testViaXML() {
        // Double step to make Xmappr work harder (not necessary normally - do not copy)
        // Reads Class configuration, produces XML configuration from it and then feeds it to Xmappr
        StringReader configuration = XmlConfigTester.reader(RoundedInt.class);
        Xmappr xmappr = new Xmappr(configuration);
        RoundedInt roundedInt = (RoundedInt) xmappr.fromXML(new StringReader(xml));

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
