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

public class UndeclaringDefaultNs {
    private static String xml = "" +
            "<aaa xmlns = \"lowercase\" >\n" +
            "  <bbbb >\n" +
            "    <cccc xmlns = \"\" >\n" +
            "      <ddd />\n" +
            "    </cccc>\n" +
            "  </bbbb>\n" +
            "</aaa>";

    @org.testng.annotations.Test
    public void test() throws IOException, SAXException {
        StringReader reader = new StringReader(xml);
        Configuration conf = new AnnotationConfiguration(aaa.class, "aaa");

        // predefined default namespace
        conf.addNamespace("lowercase");

        Xlite xlite = new Xlite(conf);
        aaa a = (aaa) xlite.fromXML(reader);

        Assert.assertTrue(a.node_bbb.node_ccc.node_ddd != null);

        // writing back to XML
        StringWriter sw = new StringWriter();
        xlite.toXML(a, sw);
//        System.out.println(sw);
        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(xml, sw.toString());

    }

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
