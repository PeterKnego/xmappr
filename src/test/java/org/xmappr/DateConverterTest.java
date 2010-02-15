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
import org.xmappr.annotation.Element;
import org.xmappr.annotation.RootElement;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateConverterTest {

    private static String inXml = "" +
            "<test>" +
            "<node>2001.07.04 12:08:56</node>" +
            "</test>";

    // wrong Date format for given formatter
    private static String inXml2 = "" +
            "<test>" +
            "<node>2001-07-04 12:08:56</node>" +
            "</test>";

     @org.testng.annotations.Test
    public void test() throws IOException, SAXException, ParseException {
        StringReader reader = new StringReader(inXml);
        Xmappr xmappr = new Xmappr(Test.class);

        Test test = (Test) xmappr.fromXML(reader);
        asserts(xmappr, test);
    }

    @org.testng.annotations.Test
    public void testViaXML() throws IOException, SAXException, ParseException {
        StringReader reader = new StringReader(inXml);

        // Double step to make Xmappr work harder (not necessary normally - do not copy)
        // Reads Class configuration, produces XML configuration from it and then feeds it to Xmappr
        StringReader configuration = XmlConfigTester.reader(Test.class);
        Xmappr xmappr = new Xmappr(configuration);

        Test test = (Test) xmappr.fromXML(reader);
        asserts(xmappr, test);
    }

    private void asserts(Xmappr xmappr, Test test) throws ParseException, SAXException, IOException {
        // writing back to XML
        StringWriter sw = new StringWriter();
        xmappr.toXML(test, sw);
        DateFormat df = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

        Assert.assertEquals(test.node, df.parse("2001.07.04 12:08:56"));
        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(inXml, sw.toString());
    }

    @org.testng.annotations.Test(expectedExceptions = XmapprException.class)
    public void wrongFormatterTest() {
        StringReader reader = new StringReader(inXml2);

        // Double step to make Xmappr work harder (not necessary normally - do not copy)
        // Reads Class configuration, produces XML configuration from it and then feeds it to Xmappr
        StringReader configuration = XmlConfigTester.reader(Test.class);
        Xmappr xmappr = new Xmappr(configuration);

        Test test = (Test) xmappr.fromXML(reader);
    }

    @RootElement("test")
    public static class Test {
        @Element(format = "yyyy.MM.dd HH:mm:ss")
        public Date node;
    }

    @org.testng.annotations.Test(expectedExceptions = XmapprException.class)
    public void testEmptyFormat() {
        StringReader reader = new StringReader(inXml);

        // Double step to make Xmappr work harder (not necessary normally - do not copy)
        // Reads Class configuration, produces XML configuration from it and then feeds it to Xmappr
        StringReader configuration = XmlConfigTester.reader(TestEmpty.class);
        Xmappr xmappr = new Xmappr(configuration);

        TestEmpty test = (TestEmpty) xmappr.fromXML(reader);
    }

    @RootElement("test")
    public static class TestEmpty {
        // format is empty
        @Element(format = "")
        public Date node;
    }

    @org.testng.annotations.Test(expectedExceptions = XmapprException.class)
    public void testWrongFormat() {
        StringReader reader = new StringReader(inXml);

        // Double step to make Xmappr work harder (not necessary normally - do not copy)
        // Reads Class configuration, produces XML configuration from it and then feeds it to Xmappr
        StringReader configuration = XmlConfigTester.reader(TestWrong.class);
        Xmappr xmappr = new Xmappr(configuration);

        TestWrong test = (TestWrong) xmappr.fromXML(reader);
    }

    @RootElement("test")
    public static class TestWrong {
        // format is plainly wrong
        @Element(format = "wow this is a wrong format")
        public Date node;
    }
}
