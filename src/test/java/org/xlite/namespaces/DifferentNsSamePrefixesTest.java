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
 * Test where xml elements belong to different namespaces although they have the same prefixes.
 *
 * @author peter
 */
public class DifferentNsSamePrefixesTest {

    static String xml = "" +
            "<aaa >\n" +
            "  <lower:bbb xmlns:lower = \"lowercase\" >\n" +
            "     <lower:ccc />\n" +
            "  </lower:bbb>\n" +
            "  <lower:BBB xmlns:lower = \"uppercase\" >\n" +
            "    <lower:CCC />\n" +
            "  </lower:BBB>\n" +
            "  <lower:x111 xmlns:lower = \"xnumber\" >\n" +
            "    <lower:x222 />\n" +
            "  </lower:x111>\n" +
            "</aaa>";

    @org.testng.annotations.Test
    public void test() throws IOException, SAXException {
        StringReader reader = new StringReader(xml);
        Configuration conf = new AnnotationConfiguration(aaa.class, "aaa");

        Xlite xlite = new Xlite(conf);
        aaa a = (aaa) xlite.fromXML(reader);

        Assert.assertTrue(a.node_bbb.node_ccc != null);
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

    // node aaa is in default namespace
    public static class aaa {
        @XMLnamespaces("lower=lowercase")
        @XMLelement("lower:bbb")
        public bbb node_bbb;

        @XMLnamespaces("lower=uppercase")
        @XMLelement("lower:BBB")
        public BBB node_BBB;

        @XMLnamespaces("lower=xnumber")
        @XMLelement("lower:x111")
        public x111 node_x111;
    }

    public static class bbb {
        @XMLnamespaces("lower=lowercase")
        @XMLelement("lower:ccc")
        public ccc node_ccc;
    }

    public static class ccc {
    }

    public static class BBB {
        @XMLnamespaces("lower=uppercase")
        @XMLelement("lower:CCC")
        public CCC node_CCC;
    }

    public static class CCC {
    }

    @XMLnamespaces("lower=xnumber")
    public static class x111 {
        @XMLelement("lower:x222")
        public x222 node_x222;
    }

    public static class x222 {
    }
}
