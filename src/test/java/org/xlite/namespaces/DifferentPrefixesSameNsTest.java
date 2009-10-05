/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
package org.xlite.namespaces;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xlite.*;
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

        // Double step to make Xlite work harder (not necessary normally - do not copy)
        // Reads Class configuration, produces XML configuration from it and then feeds it to Xlite
        StringReader configuration = XmlConfigTester.reader(aaa.class);
        Xlite xlite = new Xlite(configuration);

        // predefined namespaces
//        xlite.addNamespace("l","lowercase");
        xlite.addNamespace("u","lowercase");

        aaa a = (aaa) xlite.fromXML(reader);

        Assert.assertTrue(a.node_bbbb.node_cccc != null);
        Assert.assertTrue(a.node_BBB.node_CCC != null);
        Assert.assertTrue(a.node_x111.node_x222 != null);

        // writing back to XML
        StringWriter sw = new StringWriter();
        xlite.toXML(a, sw);
        System.out.println(xml);
        System.out.println(sw);
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

    @Namespaces("l=lowercase")
    public static class bbbb {
        @Element("l:cccc")
        public cccc node_cccc;
    }

    public static class cccc {
    }

    public static class BBB {
        @Element("u:CCC")
        public CCC node_CCC;
    }

    public static class CCC {
    }

    @Namespaces("xn=lowercase")
    public static class x111 {
        @Element("xn:x222")
        public x222 node_x222;
    }

    @Namespaces("xn=lowercase")
    public static class x222 {
    }
}
