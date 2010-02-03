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
 * Test where each subnode defines its default namespace
 */
public class DefaultNSEachSubnode {

    private static String xml =
            "<aaa xmlns=\"defaultRoot\">\n" +
                    "  <bbbb xmlns = \"lowercase\" >\n" +
                    "    <cccc />\n" +
                    "  </bbbb>\n" +
                    "  <BBB xmlns = \"uppercase\" >\n" +
                    "    <CCC />\n" +
                    "  </BBB>\n" +
                    "  <x111 xmlns = \"xnumber\" >\n" +
                    "    <x222 />\n" +
                    "  </x111>\n" +
                    "</aaa>";
     @Test
    public void test() throws IOException, SAXException {
        StringReader reader = new StringReader(xml);

        Xmappr xmappr = new Xmappr(aaa.class);

        // this is set for testing purposes - making sure that class defined namespace overrides this
        xmappr.addNamespace("","defaultRoot2");

        aaa a = (aaa) xmappr.fromXML(reader);

         asserts(xmappr, a);
    }

    @Test
    public void testViaXML() throws IOException, SAXException {
        StringReader reader = new StringReader(xml);

        // Double step to make Xmappr work harder (not necessary normally - do not copy)
        // Reads Class configuration, produces XML configuration from it and then feeds it to Xmappr
        StringReader configuration = XmlConfigTester.reader(aaa.class);
        Xmappr xmappr = new Xmappr(configuration);

        // this is set for testing purposes - making sure that class defined namespace overrides this
        xmappr.addNamespace("","defaultRoot2");

        aaa a = (aaa) xmappr.fromXML(reader);

        asserts(xmappr, a);
    }

    private void asserts(Xmappr xmappr, aaa a) throws SAXException, IOException {
        Assert.assertTrue(a.node_bbb.node_cccc != null);
        Assert.assertTrue(a.node_BBB.node_CCC != null);
        Assert.assertTrue(a.node_x111.node_x222 != null);

        // writing back to XML
        StringWriter sw = new StringWriter();
        xmappr.toXML(a, sw);
        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(xml, sw.toString());
    }

    // overrides the above defined namespace
    @Namespaces("defaultRoot")
    @RootElement("aaa")
    public static class aaa {
        @Namespaces("lowercase")
        @Element("bbbb")
        public bbbb node_bbb;

        @Namespaces("uppercase")
        @Element("BBB")
        public BBB node_BBB;

        @Namespaces("xnumber")
        @Element("x111")
        public x111 node_x111;
    }

    public static class bbbb {
        @Namespaces("lowercase")
        @Element("cccc")
        public cccc node_cccc;
    }

    public static class cccc {
        @Text
        public String notext;
    }

    public static class BBB {
        @Namespaces("uppercase")
        @Element("CCC")
        public CCC node_CCC;
    }

    public static class CCC {
        @Text
        public String notext;
    }

    public static class x111 {
        @Namespaces("xnumber")
        @Element("x222")
        public x222 node_x222;
    }

    public static class x222 {
        @Text
        public String notext;
    }
}
