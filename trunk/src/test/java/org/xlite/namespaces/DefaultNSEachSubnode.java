package org.xlite.namespaces;

import org.xlite.XMLelement;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.IOException;

import org.testng.Assert;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XMLAssert;
import org.xml.sax.SAXException;
import org.xlite.Xlite;
import org.xlite.XMLnamespaces;

/**
 * Test where each subnode defines it's default namespace
 * @author peter
 */
public class DefaultNSEachSubnode {

       static String xml = "<aaa >\n" +
               "  <bbb xmlns = \"lowercase\" >\n" +
               "    <ccc />\n" +
               "  </bbb>\n" +
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
        Xlite xlite = new Xlite(aaa.class, "aaa");
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
        @XMLnamespaces("lowercase")
        @XMLelement("bbb")
        public bbb node_bbb;

        @XMLnamespaces("uppercase")
        @XMLelement("BBB")
        public BBB node_BBB;

        @XMLnamespaces("xnumber")
        @XMLelement("x111")
        public x111 node_x111;
    }

    @XMLnamespaces("lowercase")
    public static class bbb {
        @XMLelement("ccc")
        public ccc node_ccc;
    }

    public static class ccc {
    }

    @XMLnamespaces("uppercase")
    public static class BBB {
        @XMLelement("CCC")
        public CCC node_CCC;
    }

    public static class CCC {
    }

    @XMLnamespaces("xnumber")
    public static class x111 {
        @XMLelement("x222")
        public x222 node_x222;
    }

    public static class x222 {
    }
}
