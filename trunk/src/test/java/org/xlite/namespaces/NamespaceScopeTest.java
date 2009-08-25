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

    @org.testng.annotations.Test
    public void test() throws IOException, SAXException {
        StringReader reader = new StringReader(xml);
        Configuration conf = new AnnotationConfiguration(aaa.class, "l:aaa");
//        conf.addNamespace("l=lowercase");

        Xlite xlite = new Xlite(conf);
        aaa a = (aaa) xlite.fromXML(reader);

        Assert.assertNotNull(a.node_BBB.node_ccc.node_x111);
        Assert.assertNotNull(a.node_BBB.node_x111);
        Assert.assertNotNull(a.node_x111);

        // writing back to XML
        StringWriter sw = new StringWriter();
        xlite.toXML(a, sw);
        System.out.println(sw);
        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(xml, sw.toString());
    }

    @XMLnamespaces("l=lowercase")
    public static class aaa {

        @XMLnamespaces("l=uppercase")
        @XMLelement("l:BBB")
        public BBB node_BBB;

        @XMLelement("l:x111")
        public x111 node_x111;
    }

    @XMLnamespaces("l=uppercase")
    public static class BBB {

        @XMLnamespaces("l=xnumber")
        @XMLelement("cccc")
        public ccc node_ccc;

        @XMLelement("l:x111")
        public x111 node_x111;
    }

    @XMLnamespaces("l=xnumber")
    public static class ccc {
        @XMLelement("l:x111")
        public x111 node_x111;
    }

    public static class x111 {
    }


}
