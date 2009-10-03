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

        Xlite xlite = new Xlite(aaa.class);

        // this is set for testing purposes - making sure that class defined namespace overrides this
        xlite.addNamespace("","defaultRoot2");

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
