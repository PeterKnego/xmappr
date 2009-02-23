package org.xlite.namespaces;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.testng.Assert;
import org.xml.sax.SAXException;
import org.xlite.*;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Test where all elements belongs to the same namespace although different prefixes are used
 *
 * @author peter
 */
public class DifferentPrefixesSameNsTest {

    static String xml = "" +
            "<lower:aaa xmlns:lower = \"lowercase\" xmlns:upper = \"lowercase\"\n" +
            "  xmlns:xnumber = \"lowercase\" >\n" +
            "  <lower:bbb >\n" +
            "       <lower:ccc />\n" +
            "  </lower:bbb>\n" +
            "  <upper:BBB >\n" +
            "       <upper:CCC />\n" +
            "  </upper:BBB>\n" +
            "  <xnumber:x111 >\n" +
            "       <xnumber:x222 />\n" +
            "  </xnumber:x111>\n" +
            "</lower:aaa>";

    @org.testng.annotations.Test
    public void test() throws IOException, SAXException {
        StringReader reader = new StringReader(xml);
        Configuration conf = new AnnotationConfiguration(aaa.class, "l:aaa");

        // predefined namespaces
        conf.addNamespace("l=lowercase");
        conf.addNamespace("u=lowercase");

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

    public static class aaa {
        @XMLelement("l:bbb")
        public bbb node_bbb;

        @XMLelement("u:BBB")
        public BBB node_BBB;

        @XMLnamespaces("xn=lowercase")
        @XMLelement("xn:x111")
        public x111 node_x111;
    }

    public static class bbb {
        @XMLelement("l:ccc")
        public ccc node_ccc;
    }

    public static class ccc {
    }

    public static class BBB {
        @XMLelement("u:CCC")
        public CCC node_CCC;
    }

    public static class CCC {
    }

    @XMLnamespaces("xn=lowercase")
    public static class x111 {
        @XMLelement("xn:x222")
        public x222 node_x222;
    }

    @XMLnamespaces("xn=lowercase")
    public static class x222 {
    }
}
