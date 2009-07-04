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

    @org.testng.annotations.Test
    public void test() throws IOException, SAXException {
        StringReader reader = new StringReader(xml);

        //default namespace
        Configuration conf = new AnnotationConfiguration(aaa.class, "l:aaa");

        // predefined namespaces
        conf.addNamespace("l=lowercase");
        conf.addNamespace("u=uppercase");
        conf.addNamespace("xn=xnumber");
        conf.addNamespace("defaultNS");

        Xlite xlite = new Xlite(conf);
        aaa a = (aaa) xlite.fromXML(reader);

        Assert.assertEquals(a.node_bbbb.zz, 11);
        Assert.assertEquals(a.node_bbbb.node_ccc.WW, 22);
        Assert.assertEquals(a.node_BBB.sss, "***");
        Assert.assertEquals(a.node_BBB.S111, "???");
        Assert.assertNotNull(a.node_x111);
        Assert.assertEquals(a.node_x111.rrr, "rrrdata");

        StringWriter sw = new StringWriter();
        xlite.toXML(a, sw);
        System.out.println(xml);
        System.out.println(sw.toString());

        // writing back to XML
        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(xml, sw.toString());
    }

    public static class aaa {
        @XMLelement("l:bbbb")
        public bbbb node_bbbb;

        @XMLelement("u:BBB")
        public BBB node_BBB;

        @XMLelement("xn:x111")
        public x111 node_x111;
    }

    public static class bbbb {
        @XMLelement("l:cccc")
        public ccc node_ccc;

        @XMLattribute("l:zz")
        public int zz;
    }

    public static class ccc {
        @XMLattribute("u:WW")
        public int WW;
    }

    public static class BBB {
        @XMLattribute("l:sss")
        public String sss;

        @XMLattribute("xn:S111")
        public String S111;
    }

    public static class x111 {
        @XMLattribute("RRR")
        // no NS defined, but this does NOT mean default ns
        public String rrr;
    }

}
