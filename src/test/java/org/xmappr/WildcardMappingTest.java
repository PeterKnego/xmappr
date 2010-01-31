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
 * It maps various elements: <one>, <two> and <three>.
 */
public class WildcardMappingTest {

    private String xml = "<root>\n" +
            "  <one>1</one>\n" +
            "  <two>2</two>\n" +
            "  <three>3</three>\n" +
            "</root>";

    /**
     * Maps to a custom class Numbers with a custom NumbersConverter
     */
    @RootElement
    public static class Root {

        @Element(name = "*", converter = NumbersConverter.class)
        public Numbers numbers;
    }

    /**
     * Maps to a java.util.List with a custom SingleNumberConverter and adds some mapper mixing
     */
    @RootElement("root")
    public static class RootTwo {

        @Elements({
                @Element(name = "one", targetType = Integer.class),
                @Element(name = "*", converter = SingleNumberConverter.class)
        })
        public List numbers;
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
        asserts(reader, xmappr);
    }

    @Test
    public void testViaXML() throws IOException, SAXException {
        Reader reader = new StringReader(xml);
        // Double step to make Xmappr work harder (not necessary normally - do not copy)
        // Reads Class configuration, produces XML configuration from it and then feeds it to Xmappr
        StringReader configuration = XmlConfigTester.reader(Root.class);
        Xmappr xmappr = new Xmappr(configuration);
        asserts(reader, xmappr);
    }

    private void asserts(Reader reader, Xmappr xmappr) throws SAXException, IOException {
        // read XML
        Root root = (Root) xmappr.fromXML(reader);

        // write out XML
        Writer writer = new StringWriter();
        xmappr.toXML(root, writer);

        // check that three elements were read
        Assert.assertEquals(root.numbers.data.size(), 3);
        //test that output matches input
        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(xml, writer.toString());
    }

    @Test
    public void testList() throws IOException, SAXException {
        Reader reader = new StringReader(xml);
        Xmappr xmappr = new Xmappr(RootTwo.class);
        assertsList(reader, xmappr);
    }

    @Test
    public void testListViaXML() throws IOException, SAXException {
        Reader reader = new StringReader(xml);
        // Double step to make Xmappr work harder (not necessary normally - do not copy)
        // Reads Class configuration, produces XML configuration from it and then feeds it to Xmappr
        StringReader configuration = XmlConfigTester.reader(Root.class);
        Xmappr xmappr = new Xmappr(configuration);
        assertsList(reader, xmappr);
    }

    private void assertsList(Reader reader, Xmappr xmappr) throws SAXException, IOException {
        // read XML
        RootTwo rootTwo = (RootTwo) xmappr.fromXML(reader);

        // check that three elements were read
        Assert.assertEquals(rootTwo.numbers.size(), 3);

        // write out XML
        Writer writer = new StringWriter();
        xmappr.toXML(rootTwo, writer);

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

            // if target object is not yet instantiated
            Numbers target = (Numbers) targetObject;
            if (target == null) {
                // instantiate target object
                target = new Numbers();
            }
            // add the current number
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

    public static class SingleNumberConverter implements ElementConverter {

        public Object fromElement(XMLSimpleReader reader, MappingContext mappingContext, String defaultValue,
                                  String format, Class targetType, Object targetObject) {

            // read the text value of XML element
            String value = reader.getText();
            Number number = new Number();
            number.name = reader.getName().getLocalPart();
            number.value = Integer.valueOf(value);

            return number;
        }

        public void toElement(Object object, QName nodeName, XMLSimpleWriter writer, MappingContext mappingContext,
                              String defaultValue, String format) {

            Number number = (Number) object;

            writer.startElement(new QName(number.name));
            writer.addText(String.valueOf(number.value));
            writer.endElement();
        }

        public boolean canConvert(Class type) {
            return Number.class.equals(type);
        }
    }
}
