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

public class NamespaceScopeTest {

    private static String xml = "" +
            "<lower:aaa xmlns:lower = \"lowercase\" >\n" +
            "  <lower:BBB xmlns:lower = \"uppercase\" >\n" +
            "    <lower:x111 />\n" +
            "    <lower:cccc xmlns:lower = \"xnumber\" >\n" +
            "      <lower:x222 />\n" +
            "    </lower:cccc>\n" +
            "  </lower:BBB>\n" +
            "  <lower:x333 />\n" +
            "</lower:aaa>";

    @Test
    public void test() throws IOException, SAXException {
        StringReader reader = new StringReader(xml);
        Xmappr xmappr = new Xmappr(aaa.class);
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
        aaa a = (aaa) xmappr.fromXML(reader);

        asserts(xmappr, a);
    }

    private void asserts(Xmappr xmappr, aaa a) throws SAXException, IOException {
        Assert.assertNotNull(a.node_BBB.node_ccc.x2);
        Assert.assertNotNull(a.node_BBB.x1);
        Assert.assertNotNull(a.x3);

        // writing back to XML
        StringWriter sw = new StringWriter();
        xmappr.toXML(a, sw);
        System.out.println(sw);
        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(xml, sw.toString());
    }

    @Namespaces("l=lowercase")
    @RootElement("l:aaa")
    public static class aaa {

        @Namespaces("l=uppercase")
        @Element("l:BBB")
        public BBB node_BBB;

//        @Namespaces("l=lowercase")
        @Element("l:x333")
        public x333 x3;
    }

    public static class BBB {

        @Namespaces("l=xnumber")
        @Element("l:cccc")
        public ccc node_ccc;

        @Namespaces("l=uppercase")
        @Element("l:x111")
        public x111 x1;
    }

    public static class ccc {
        @Namespaces("l=xnumber")
        @Element("l:x222")
        public x222 x2;
    }

    public static class x111 {
        @Text
        public String notext;
    }

    public static class x222 {
        @Text
        public String notext;
    }

    public static class x333 {
        @Text
        public String notext;
    }

}
