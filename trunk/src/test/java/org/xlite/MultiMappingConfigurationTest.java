package org.xlite;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.testng.Assert;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * This test has two mappings configured:
 * <br>One.class maps to xml element 'one'
 * <br>Two.class maps to xml element 'two'
 * <br> It tests that the right mapping is invoked based on input XML.
 */
public class MultiMappingConfigurationTest {

    private static String xml1 = "<one>1</one>";
    private static String xml2 = "<two>2</two>";

    @org.testng.annotations.Test
    public void test() throws IOException, SAXException {
        StringReader reader1 = new StringReader(xml1);
        StringReader reader2 = new StringReader(xml2);

        // configure the first mapping
        Configuration conf = new AnnotationConfiguration(One.class);

        // add the second mapping
        conf.addMapping(Two.class);

        conf.setPrettyPrint(true);
        Xlite xlite = new Xlite(conf);

        // deserialize first XML
        One one = (One) xlite.fromXML(reader1);
        // test that the right mapping was picked
        Assert.assertEquals(one.text, "1");

        // deserialize second XML
        Two two = (Two) xlite.fromXML(reader2);
        // test that the right mapping was picked
        Assert.assertEquals(two.text, "2");

        // writing back to XML
        StringWriter sw = new StringWriter();
        xlite.toXML(one, sw);
        //test that output matches input
        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(xml1, sw.toString());

        // writing back to XML
        sw = new StringWriter();
        xlite.toXML(two, sw);
        //test that output matches input
        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(xml2, sw.toString());
    }

    @RootElement
    public static class One {
        @Text
        public String text;
    }

    @RootElement("two")
    public static class Two {
        @Text
        public String text;
    }
}
