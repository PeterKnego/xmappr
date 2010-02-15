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
import java.math.BigInteger;

public class BigIntegerConverterTest {

    private static String inXml = "" +
            "<test>" +
            "<node>987654321987654321</node>" +
            "</test>";

    @org.testng.annotations.Test
    public void test() throws IOException, SAXException {
        StringReader reader = new StringReader(inXml);
        Xmappr xmappr = new Xmappr(Test.class);

        Test test = (Test) xmappr.fromXML(reader);
        asserts(xmappr, test);
    }

    @org.testng.annotations.Test
    public void testViaXML() throws IOException, SAXException {
        StringReader reader = new StringReader(inXml);

         // Double step to make Xmappr work harder (not necessary normally - do not copy)
        // Reads Class configuration, produces XML configuration from it and then feeds it to Xmappr
        StringReader configuration = XmlConfigTester.reader(Test.class);
        Xmappr xmappr = new Xmappr(configuration);

        Test test = (Test) xmappr.fromXML(reader);
        asserts(xmappr, test);
    }

    private void asserts(Xmappr xmappr, Test test) throws SAXException, IOException {
        // writing back to XML
        StringWriter sw = new StringWriter();
        xmappr.toXML(test, sw);

        Assert.assertEquals(test.node, new BigInteger("987654321987654321"));
        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(inXml, sw.toString());
    }

    @RootElement("test")
    public static class Test {
        @Element
        public BigInteger node;
    }
}
