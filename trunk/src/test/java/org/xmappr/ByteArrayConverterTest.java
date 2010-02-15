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

public class ByteArrayConverterTest {

    private static String inXml = "" +
            "<test>" +
            "<node>TG9yZW0gaXBzdW0gZG9sb3Igc2l0IGFtZXQsIGNvbnNlY3RldHVyIGFkaXBpc2NpbmcgZWxpdC4gTnVsbGEgYWRpcGlzY" +
            "2luZyB1bHRyaWNpZXMgcHVydXMgcXVpcyBzb2xsaWNpdHVkaW4uIEZ1c2NlIGV1aXNtb2QgcGxhY2VyYXQgZWxpdCwgdmVsIG1v" +
            "bGVzdGllIG5pc2kgcG9ydGEgbm9uLiBBbGlxdWFtIHNhZ2l0dGlzIGFyY3UgZWdldCBtYWduYSBwcmV0aXVtIGF0IGJpYmVuZHV" +
            "tIGxlY3R1cyBsYW9yZWV0LiBQZWxsZW50ZXNxdWUgcGVsbGVudGVzcXVlIG1hdXJpcyBpZCB0ZWxsdXMgY29uc2VjdGV0dXIgZX" +
            "UgZWxlbWVudHVtIG5pYmggcG9zdWVyZS4=</node>" +
            "</test>";

    private static String loremIpsum = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla adipiscing " +
            "ultricies purus quis sollicitudin. Fusce euismod placerat elit, vel molestie nisi porta non. Aliquam " +
            "sagittis arcu eget magna pretium at bibendum lectus laoreet. Pellentesque pellentesque mauris id " +
            "tellus consectetur eu elementum nibh posuere.";

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
        String ssw = sw.toString();

        Assert.assertEquals(new String(test.node), loremIpsum);
        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(inXml, ssw);
    }

    @RootElement("test")
    public static class Test {
        @Element
        public byte[] node;
    }
}