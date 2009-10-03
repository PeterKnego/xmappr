/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
package org.xlite.namespaces;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.testng.Assert;
import org.xlite.*;
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
        Xlite xlite = new Xlite(aaa.class);

        // predefined default namespace
        xlite.addNamespace("","lowercase");

        aaa a = (aaa) xlite.fromXML(reader);

        Assert.assertTrue(a.node_bbb.node_ccc.node_ddd != null);

        // writing back to XML
        StringWriter sw = new StringWriter();
        xlite.toXML(a, sw);
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
