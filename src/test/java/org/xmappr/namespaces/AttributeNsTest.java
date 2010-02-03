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

public class AttributeNsTest {

    private static String xml =
            "<lower:aaa xmlns:lower = \"lowercase\" xmlns:upper = \"uppercase\"\n" +
                    "          xmlns:xnumber = \"xnumber\" xmlns=\"defaultNS\" >\n" +
                    "  <lower:bbbb lower:zz = \"11\" >\n" +
                    "    <lower:cccc upper:WW = \"22\" />\n" +
                    "  </lower:bbbb>\n" +
                    "  <upper:BBB lower:sss = \"***\" xnumber:S111 = \"???\" />\n" +
                    "  <xnumber:x111 RRR=\"rrrdata\" />\n" +
                    "</lower:aaa>";

    @Test
    public void test() throws IOException, SAXException {
        StringReader reader = new StringReader(xml);

        Xmappr xmappr = new Xmappr(aaa.class);

        // predefined namespaces
        xmappr.addNamespace("l","lowercase");
        xmappr.addNamespace("u","uppercase");
        xmappr.addNamespace("xn","xnumber");
        xmappr.addNamespace("","defaultNS");

        aaa a = (aaa) xmappr.fromXML(reader);

        asserts(xmappr, a);
    }

    @Test
    public void testViaXML() throws IOException, SAXException {
        StringReader reader = new StringReader(xml);

        //default namespace
        // Double step to make Xmappr work harder (not necessary normally - do not copy)
        // Reads Class configuration, produces XML configuration from it and then feeds it to Xmappr
        StringReader configuration = XmlConfigTester.reader(aaa.class,
                new String[]{"l=lowercase","u=uppercase", "xn=xnumber", "defaultNS"});
        Xmappr xmappr = new Xmappr(configuration);

        // predefined namespaces
        xmappr.addNamespace("l","lowercase");
        xmappr.addNamespace("u","uppercase");
        xmappr.addNamespace("xn","xnumber");
        xmappr.addNamespace("","defaultNS");

        aaa a = (aaa) xmappr.fromXML(reader);

        asserts(xmappr, a);
    }

    private void asserts(Xmappr xmappr, aaa a) throws SAXException, IOException {
        Assert.assertEquals(a.node_bbbb.zz, 11);
        Assert.assertEquals(a.node_bbbb.node_ccc.WW, 22);
        Assert.assertEquals(a.node_BBB.sss, "***");
        Assert.assertEquals(a.node_BBB.S111, "???");
        Assert.assertNotNull(a.node_x111);
        Assert.assertEquals(a.node_x111.rrr, "rrrdata");

        StringWriter sw = new StringWriter();
        xmappr.toXML(a, sw);

        // writing back to XML
        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(xml, sw.toString());
    }

    @RootElement("l:aaa")
    public static class aaa {
        @Element("l:bbbb")
        public bbbb node_bbbb;

        @Element("u:BBB")
        public BBB node_BBB;

        @Element("xn:x111")
        public x111 node_x111;
    }

    public static class bbbb {
        @Element("l:cccc")
        public ccc node_ccc;

        @Attribute("l:zz")
        public int zz;
    }

    public static class ccc {
        @Attribute("u:WW")
        public int WW;
    }

    public static class BBB {
        @Attribute("l:sss")
        public String sss;

        @Attribute("xn:S111")
        public String S111;
    }

    public static class x111 {
        @Attribute("RRR")
        // no NS defined, but this does NOT mean default ns
        public String rrr;
    }

}
