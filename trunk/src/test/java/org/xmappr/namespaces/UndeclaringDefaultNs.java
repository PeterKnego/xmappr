/*
 * This software is released under the BSD license. Full license available at http://xmappr.googlecode.com
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xmappr contributors
 * All rights reserved.
 */
package org.xmappr.namespaces;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.testng.Assert;
import org.xmappr.*;
import org.xmappr.annotation.Element;
import org.xmappr.annotation.Namespaces;
import org.xmappr.annotation.RootElement;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

public class UndeclaringDefaultNs {
    private static String xml = "" +
            "<aaa xmlns = \"lowercase\" >\n" +
            "  <bbbb >\n" +
            "    <cccc xmlns = \"\" >\n" +
            "      <ddd />\n" +
            "    </cccc>\n" +
            "  </bbbb>\n" +
            "</aaa>";

    @Test
    public void test() throws IOException, SAXException {
        StringReader reader = new StringReader(xml);
        Xmappr xmappr = new Xmappr(aaa.class);

        // predefined default namespace
        xmappr.addNamespace("","lowercase");

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

        // predefined default namespace
        xmappr.addNamespace("","lowercase");

        aaa a = (aaa) xmappr.fromXML(reader);
        asserts(xmappr, a);
    }

    private void asserts(Xmappr xmappr, aaa a) throws SAXException, IOException {
        Assert.assertTrue(a.node_bbb.node_ccc.node_ddd != null);

        // writing back to XML
        StringWriter sw = new StringWriter();
        xmappr.toXML(a, sw);
//        System.out.println(sw);
        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(xml, sw.toString());
    }

    @RootElement("aaa")
    public static class aaa {
        @Element("bbbb")
        public bbb node_bbb;
    }

    public static class bbb {
        @Namespaces("")
        @Element("cccc")
        public ccc node_ccc;
    }

    @Namespaces("")
    public static class ccc {
        @Element("ddd")
        public ddd node_ddd;
    }

    public static class ddd {
    }

}
