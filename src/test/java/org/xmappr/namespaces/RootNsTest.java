/*
 * This software is released under the BSD license. Full license available at http://xmappr.googlecode.com
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xmappr contributors
 * All rights reserved.
 */
package org.xmappr.namespaces;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmappr.*;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

public class RootNsTest {

    private static String xml = "" +
            "<lower:aaa xmlns:lower=\"lowercase\" xmlns:upper=\"uppercase\" xmlns:xnumber=\"xnumber\">\n" +
            "  <lower:bbbb >\n" +
            "    <lower:cccc />\n" +
            "  </lower:bbbb>\n" +
            "  <upper:BBB >\n" +
            "    <upper:CCC />\n" +
            "  </upper:BBB>\n" +
            "  <xnumber:x111 >\n" +
            "    <xnumber:x222 />\n" +
            "  </xnumber:x111>\n" +
            "</lower:aaa>";

    @Test
    public void test() throws IOException, SAXException {
        StringReader reader = new StringReader(xml);
        StringWriter writer = new StringWriter();
        Xmappr xmappr = new Xmappr(aaa.class);

        // predefined namespaces
        xmappr.addNamespace("l","lowercase");
        xmappr.addNamespace("u","uppercase");

        aaa a = (aaa) xmappr.fromXML(reader);
        asserts(writer, xmappr, a);
    }

    @Test
    public void testViaXML() throws IOException, SAXException {
        StringReader reader = new StringReader(xml);
        StringWriter writer = new StringWriter();

        // Double step to make Xmappr work harder (not necessary normally - do not copy)
        // Reads Class configuration, produces XML configuration from it and then feeds it to Xmappr
        StringReader configuration = XmlConfigTester.reader(aaa.class, new String[]{"l=lowercase","u=uppercase"});
        Xmappr xmappr = new Xmappr(configuration);

        // predefined namespaces
        xmappr.addNamespace("l","lowercase");
        xmappr.addNamespace("u","uppercase");

        aaa a = (aaa) xmappr.fromXML(reader);
        asserts(writer, xmappr, a);
    }

    private void asserts(StringWriter writer, Xmappr xmappr, aaa a) throws SAXException, IOException {
        Assert.assertTrue(a.node_bbbb.node_cccc != null);
        Assert.assertTrue(a.node_BBB.node_CCC != null);
        Assert.assertTrue(a.node_x111.node_x222 != null);

        xmappr.toXML(a, writer);
        System.out.println(writer.toString());

        // writing back to XML
        StringWriter sw = new StringWriter();
        xmappr.toXML(a, sw);
        System.out.println(sw);
        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(xml, sw.toString());
    }

    @RootElement("l:aaa")
    public static class aaa {
        @Element("l:bbbb")
        public bbbb node_bbbb;

        @Element("u:BBB")
        public BBB node_BBB;

        @Namespaces("xn=xnumber")
        @Element("xn:x111")
        public x111 node_x111;
    }

    public static class bbbb {
        @Element("l:cccc")
        public cccc node_cccc;
    }

    public static class cccc {
        @Text
        public String notext;
    }

    public static class BBB {
        @Element("u:CCC")
        public CCC node_CCC;
    }

    public static class CCC {
        @Text
        public String notext;
    }

    @Namespaces("xn=xnumber")
    public static class x111 {
        @Element("xn:x222")
        public x222 node_x222;
    }

    public static class x222 {
        @Text
        public String notext;
    }

}
