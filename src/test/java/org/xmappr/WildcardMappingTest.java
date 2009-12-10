package org.xmappr;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmappr.converters.ElementConverter;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests wildcard name mapping ("*") with a custom element converter.
 * It maps various elements: <one>, <two> and <three> to a Numbers class using NumbersConverter.
 */
public class WildcardMappingTest {

    private String xml = "<root>\n" +
            "  <one>1</one>\n" +
            "  <two>2</two>\n" +
            "  <three>3</three>\n" +
            "</root>";

    @RootElement
    public static class Root {

        @Element(name = "*", converter = NumbersConverter.class)
        public Numbers numbers;
    }

    public static class Numbers {
        public List<Number> data = new ArrayList<Number>();
    }

    public static class Number {
        public String name;
        public int value;
    }

    @Test
    public void test() throws IOException, SAXException {
        Reader reader = new StringReader(xml);
        Xmappr xmappr = new Xmappr(Root.class);

        // read XML
        Root root = (Root) xmappr.fromXML(reader);

        // check that three elements were read
        Assert.assertEquals(root.numbers.data.size(), 3);

        // write out XML
        Writer writer = new StringWriter();
        xmappr.toXML(root, writer);

        //test that output matches input
        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(xml, writer.toString());
    }

    public static class NumbersConverter implements ElementConverter {

        public Object fromElement(XMLSimpleReader reader, MappingContext mappingContext, String defaultValue,
                                  String format, Class targetType, Object targetObject) {

            // read the text value of current XML element
            String value = reader.getText();
            Number number = new Number();
            number.name = reader.getName().getLocalPart();
            number.value = Integer.valueOf(value);

            // it does it's own handling of the target object
            Numbers target = (Numbers) targetObject;
            if (target == null) {
                target = new Numbers();
            }
            target.data.add(number);
            return target;

        }

        public void toElement(Object object, QName nodeName, XMLSimpleWriter writer, MappingContext mappingContext,
                              String defaultValue, String format) {
            Numbers numbers = (Numbers) object;

            for (Number number : numbers.data) {
                writer.startElement(new QName(number.name));
                writer.addText(String.valueOf(number.value));
                writer.endElement();
            }
        }

        public boolean canConvert(Class type) {
            return Number.class.equals(type);
        }
    }

}
