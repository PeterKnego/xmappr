/*
 * This software is released under the BSD license. Full license available at http://xmappr.googlecode.com
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xmappr contributors
 * All rights reserved.
 */
package org.xmappr;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.testng.Assert;
import org.testng.annotations.Test;
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

    @Test
    public void test() throws IOException, SAXException {
        StringReader reader1 = new StringReader(xml1);
        StringReader reader2 = new StringReader(xml2);
        Xmappr xmappr = new Xmappr(One.class);
        asserts(reader1, reader2, xmappr);
    }

     @Test
    public void testViaXML() throws IOException, SAXException {
        StringReader reader1 = new StringReader(xml1);
        StringReader reader2 = new StringReader(xml2);

        // Double step to make Xmappr work harder (not necessary normally - do not copy)
        // Reads Class configuration, produces XML configuration from it and then feeds it to Xmappr
        StringReader configuration = XmlConfigTester.reader(One.class);
        Xmappr xmappr = new Xmappr(configuration);
        asserts(reader1, reader2, xmappr);
    }

    private void asserts(StringReader reader1, StringReader reader2, Xmappr xmappr) throws SAXException, IOException {
        // add the second mapping
        xmappr.addMapping(Two.class);
        xmappr.setPrettyPrint(true);

        // deserialize first XML
        One one = (One) xmappr.fromXML(reader1);
        // test that the right mapping was picked
        Assert.assertEquals(one.text, "1");

        // deserialize second XML
        Two two = (Two) xmappr.fromXML(reader2);
        // test that the right mapping was picked
        Assert.assertEquals(two.text, "2");

        // writing back to XML
        StringWriter sw = new StringWriter();
        xmappr.toXML(one, sw);
        //test that output matches input
        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(xml1, sw.toString());

        // writing back to XML
        sw = new StringWriter();
        xmappr.toXML(two, sw);
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
