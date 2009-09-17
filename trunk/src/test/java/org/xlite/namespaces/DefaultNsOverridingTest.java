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
import org.xml.sax.SAXException;
import org.xlite.*;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.IOException;

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

    @org.testng.annotations.Test
    public void test() throws IOException, SAXException {
        StringReader reader = new StringReader(xml);
        Configuration conf = new AnnotationConfiguration(aaa.class, "aaa");

        // predefined namespaces
        conf.addNamespace("u=uppercase");
        conf.addNamespace("xn=xnumber");

        Xlite xlite = new Xlite(conf);
        aaa a = (aaa) xlite.fromXML(reader);

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
        xlite.toXML(a, sw);
        System.out.println(xml);
        System.out.println(sw);
        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(xml, sw.toString());
    }

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
    }

    public static class x666 {
    }

    public static class WWW {
    }
}

