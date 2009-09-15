package org.xlite;

import org.testng.Assert;
import org.xlite.converters.ValueConverter;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Map;
import java.io.StringReader;

/**
 * Tests
 */
public class CustomConverterMultiTypeTest {

    private String xml = "" +
            "<root one='1' two='2' three='3'>" +
            "</root>";

    @org.testng.annotations.Test()
    public void customConverterMultiTypeTest() {
        StringReader reader = new StringReader(xml);
        Configuration conf = new AnnotationConfiguration(Root.class, "root");
        Xlite xlite = new Xlite(conf);
        Root root = (Root) xlite.fromXML(reader);

        // check that Map contains three attributes
        Assert.assertEquals(root.data.size(), 3);

        // check that each attribute name corresponds to the right type
        Assert.assertEquals(root.data.get(new QName("one")).getClass(), One.class);
        Assert.assertEquals(root.data.get(new QName("two")).getClass(), Two.class);
        Assert.assertEquals(root.data.get(new QName("three")).getClass(), Three.class);

    }

    public static class CustomMultiTypeConverter extends ValueConverter {

        // This converter declares itself as capable
        //  of converting three classes: One, Two and Three
        public boolean canConvert(Class type) {
            return One.class.equals(type) || Two.class.equals(type) || Three.class.equals(type);
        }


        public Object fromValue(String value, String format, Class targetType) {
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

    public static class Root {
        @XMLattributes({
                @XMLattribute(name = "one", converter = CustomMultiTypeConverter.class, itemType = One.class),
                @XMLattribute(name = "two", converter = CustomMultiTypeConverter.class, itemType = Two.class),
                @XMLattribute(name = "three", converter = CustomMultiTypeConverter.class, itemType = Three.class)
        })
        public Map data;
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
