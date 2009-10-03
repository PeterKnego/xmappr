/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
package org.xlite;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.testng.Assert;
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
        Configuration conf = new Configuration(Test.class);
        Xlite xlite = new Xlite(conf);

        Test test = (Test) xlite.fromXML(reader);

        // writing back to XML
        StringWriter sw = new StringWriter();
        xlite.toXML(test, sw);
        String ssw = sw.toString();
        System.out.println("");
        System.out.println(inXml);
        System.out.println("");
        System.out.println(ssw);
        DateFormat df = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

        Assert.assertEquals(test.node, df.parse("2001.07.04 12:08:56"));
        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(inXml, ssw);

    }

    @org.testng.annotations.Test(expectedExceptions = XliteException.class)
    public void wrongFormatterTest() {
        StringReader reader = new StringReader(inXml2);
        Configuration conf = new Configuration(Test.class);
        Xlite xlite = new Xlite(conf);

        Test test = (Test) xlite.fromXML(reader);
    }

    @RootElement("test")
    public static class Test {
        @Element(format = "yyyy.MM.dd HH:mm:ss")
        public Date node;
    }

    @org.testng.annotations.Test(expectedExceptions = XliteException.class)
    public void testEmptyFormat() {
        StringReader reader = new StringReader(inXml);
        Configuration conf = new Configuration(TestEmpty.class);
        Xlite xlite = new Xlite(conf);

        TestEmpty test = (TestEmpty) xlite.fromXML(reader);
    }

    @RootElement("test")
    public static class TestEmpty {
        // format is empty
        @Element(format = "")
        public Date node;
    }

    @org.testng.annotations.Test(expectedExceptions = XliteException.class)
    public void testWrongFormat() {
        StringReader reader = new StringReader(inXml);
        Configuration conf = new Configuration(TestWrong.class);
        Xlite xlite = new Xlite(conf);

        TestWrong test = (TestWrong) xlite.fromXML(reader);
    }

    @RootElement("test")
    public static class TestWrong {
        // format is plainly wrong
        @Element(format = "wow this is a wrong format")
        public Date node;
    }
}
