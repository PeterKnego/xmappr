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
 * Test where default namespaces are used, but namespaces for chosen elements can still be explicitly stated.
 */
public class DefaultNsOverridingTest {

    private static String xml = "" +
            "<aaa xmlns:upper = \"uppercase\" xmlns:xnumber = \"xnumber\" >\n" +
            "  <bbbb xmlns = \"lowercase\" >\n" +
            "       <cccc />\n" +
            "       <upper:WWW />\n" +
            "       <xnumber:x666 />\n" +
            "  </bbbb>\n" +
            "  <BBB xmlns = \"uppercase\" >\n" +
            "       <upper:WWW />\n" +
            "       <xnumber:x666 />\n" +
            "       <CCC />\n" +
            "  </BBB>\n" +
            "  <x111 xmlns = \"xnumber\" >\n" +
            "       <x222 />\n" +
            "       <upper:WWW />\n" +
            "       <xnumber:x666 />\n" +
            "  </x111>\n" +
            "</aaa>";

     @Test
    public void test() throws IOException, SAXException {
        StringReader reader = new StringReader(xml);

        Xmappr xmappr = new Xmappr(aaa.class);
        asserts(reader, xmappr);
    }
    @Test
    public void testViaXML() throws IOException, SAXException {
        StringReader reader = new StringReader(xml);

        // Double step to make Xmappr work harder (not necessary normally - do not copy)
        // Reads Class configuration, produces XML configuration from it and then feeds it to Xmappr
        StringReader configuration = XmlConfigTester.reader(aaa.class, new String[]{"u=uppercase", "xn=xnumber"});
        Xmappr xmappr = new Xmappr(configuration);
        asserts(reader, xmappr);
    }

    private void asserts(StringReader reader, Xmappr xmappr) throws SAXException, IOException {
        // predefined namespaces
        xmappr.addNamespace("u", "uppercase");
        xmappr.addNamespace("xn", "xnumber");

        aaa a = (aaa) xmappr.fromXML(reader);

        Assert.assertTrue(a.node_bbbb.node_cccc != null);
        Assert.assertTrue(a.node_bbbb.node_WWW != null);
        Assert.assertTrue(a.node_bbbb.node_x666 != null);
        Assert.assertTrue(a.node_BBB.node_CCC != null);
        Assert.assertTrue(a.node_BBB.node_WWW != null);
        Assert.assertTrue(a.node_BBB.node_x666 != null);
        Assert.assertTrue(a.node_x111.node_x222 != null);
        Assert.assertTrue(a.node_x111.node_WWW != null);
        Assert.assertTrue(a.node_x111.node_x666 != null);

        // writing back to XML
        StringWriter sw = new StringWriter();
        xmappr.toXML(a, sw);
        System.out.println(xml);
        System.out.println(sw);
        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(xml, sw.toString());
    }

    @RootElement("aaa")
    public static class aaa {
        @Namespaces("lowercase")
        @Element("bbbb")
        public bbbb node_bbbb;

        @Namespaces("uppercase")
        @Element("BBB")
        public BBB node_BBB;

        @Namespaces("xnumber")
        @Element("x111")
        public x111 node_x111;
    }

    @Namespaces("lowercase")
    public static class bbbb {
        @Element("cccc")
        public cccc node_cccc;

        @Element("u:WWW")
        public WWW node_WWW;

        @Element("xn:x666")
        public x666 node_x666;
    }

    public static class cccc {
        @Text
        public String notext;
    }

    @Namespaces("uppercase")
    public static class BBB {
        @Element("CCC")
        public CCC node_CCC;

        @Element("u:WWW")
        public WWW node_WWW;

        @Element("xn:x666")
        public x666 node_x666;
    }

    public static class CCC {
        @Text
        public String notext;
    }

    @Namespaces("xnumber")
    public static class x111 {
        @Element("x222")
        public x222 node_x222;

        @Element("u:WWW")
        public WWW node_WWW;

        @Element("xn:x666")
        public x666 node_x666;
    }

    public static class x222 {
        @Text
        public String notext;
    }

    public static class x666 {
        @Text
        public String notext;
    }

    public static class WWW {
        @Text
        public String notext;
    }
}

