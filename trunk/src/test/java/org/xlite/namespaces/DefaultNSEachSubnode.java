/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
package org.xlite.namespaces;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.IOException;

import org.testng.Assert;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XMLAssert;
import org.xml.sax.SAXException;
import org.xlite.*;

/**
 * Test where each subnode defines its default namespace
 */
public class DefaultNSEachSubnode {

    private static String xml =
            "<aaa >\n" +
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

    @org.testng.annotations.Test
    public void test() throws IOException, SAXException {
        StringReader reader = new StringReader(xml);

        Configuration conf = new AnnotationConfiguration(aaa.class, "aaa");
        Xlite xlite = new Xlite(conf);
        aaa a = (aaa) xlite.fromXML(reader);

        Assert.assertTrue(a.node_bbb.node_cccc != null);
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
    }

    public static class BBB {
        @Namespaces("uppercase")
        @Element("CCC")
        public CCC node_CCC;
    }

    public static class CCC {
    }

    public static class x111 {
        @Namespaces("xnumber")
        @Element("x222")
        public x222 node_x222;
    }

    public static class x222 {
    }
}
