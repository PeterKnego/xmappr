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
            "    <cccc xmlns:lower = \"xnumber\" >\n" +
            "      <lower:x111 />\n" +
            "    </cccc>\n" +
            "  </lower:BBB>\n" +
            "  <lower:x111 />\n" +
            "</lower:aaa>";

    @Test
    public void test() throws IOException, SAXException {
        StringReader reader = new StringReader(xml);

        // Double step to make Xmappr work harder (not necessary normally - do not copy)
        // Reads Class configuration, produces XML configuration from it and then feeds it to Xmappr
        StringReader configuration = XmlConfigTester.reader(aaa.class);
        Xmappr xmappr = new Xmappr(configuration);
//        conf.addNamespace("l=lowercase");

        aaa a = (aaa) xmappr.fromXML(reader);

        Assert.assertNotNull(a.node_BBB.node_ccc.node_x111);
        Assert.assertNotNull(a.node_BBB.node_x111);
        Assert.assertNotNull(a.node_x111);

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

        @Element("l:x111")
        public x111 node_x111;
    }

    @Namespaces("l=uppercase")
    public static class BBB {

        @Namespaces("l=xnumber")
        @Element("cccc")
        public ccc node_ccc;

        @Element("l:x111")
        public x111 node_x111;
    }

    @Namespaces("l=xnumber")
    public static class ccc {
        @Element("l:x111")
        public x111 node_x111;
    }

    public static class x111 {
        @Text
        public String notext;
    }


}
