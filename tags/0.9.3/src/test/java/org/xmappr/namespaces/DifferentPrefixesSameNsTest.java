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

/**
 * Test where all elements belongs to the same namespace although different prefixes are used
 */
public class DifferentPrefixesSameNsTest {

    private static String xml = "" +
            "<lower:aaa xmlns:lower = \"lowercase\" xmlns:upper = \"lowercase\"\n" +
            "  xmlns:xnumber = \"lowercase\" >\n" +
            "  <lower:bbbb >\n" +
            "       <lower:cccc />\n" +
            "  </lower:bbbb>\n" +
            "  <upper:BBB >\n" +
            "       <upper:CCC />\n" +
            "  </upper:BBB>\n" +
            "  <xnumber:x111 >\n" +
            "       <xnumber:x222 />\n" +
            "  </xnumber:x111>\n" +
            "</lower:aaa>";

    @Test
    public void test() throws IOException, SAXException {
        StringReader reader = new StringReader(xml);

        Xmappr xmappr = new Xmappr(aaa.class);
        xmappr.addNamespace("u","lowercase");
        aaa a = (aaa) xmappr.fromXML(reader);

        asserts(xmappr, a);
    }
    @Test
    public void testViaXML() throws IOException, SAXException {
        StringReader reader = new StringReader(xml);

        // Double step to make Xmappr work harder (not necessary normally - do not copy)
        // Reads Class configuration, produces XML configuration from it and then feeds it to Xmappr
        StringReader configuration = XmlConfigTester.reader(aaa.class, new String[]{"u=lowercase"});
        Xmappr xmappr = new Xmappr(configuration);
        xmappr.addNamespace("u","lowercase");
        aaa a = (aaa) xmappr.fromXML(reader);

        asserts(xmappr, a);
    }

    private void asserts(Xmappr xmappr, aaa a) throws SAXException, IOException {
        Assert.assertTrue(a.node_bbbb.node_cccc != null);
        Assert.assertTrue(a.node_BBB.node_CCC != null);
        Assert.assertTrue(a.node_x111.node_x222 != null);

        // writing back to XML
        StringWriter sw = new StringWriter();
        xmappr.toXML(a, sw);
        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(xml, sw.toString());
    }

    @Namespaces("l=lowercase")
    @RootElement("l:aaa")
    public static class aaa {
        @Element("l:bbbb")
        public bbbb node_bbbb;

        @Element("u:BBB")
        public BBB node_BBB;

        @Namespaces("xn=lowercase")
        @Element("xn:x111")
        public x111 node_x111;
    }

    public static class bbbb {
        @Namespaces("l=lowercase")
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

    public static class x111 {
        @Namespaces("xn=lowercase")
        @Element("xn:x222")
        public x222 node_x222;
    }

    public static class x222 {
        @Text
        public String notext;
    }
}
