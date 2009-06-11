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
 * @author peter
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
        @XMLnamespaces("lowercase")
        @XMLelement("bbbb")
        public bbbb node_bbb;

        @XMLnamespaces("uppercase")
        @XMLelement("BBB")
        public BBB node_BBB;

        @XMLnamespaces("xnumber")
        @XMLelement("x111")
        public x111 node_x111;
    }

    public static class bbbb {
        @XMLnamespaces("lowercase")
        @XMLelement("cccc")
        public cccc node_cccc;
    }

    public static class cccc {
    }

    public static class BBB {
        @XMLnamespaces("uppercase")
        @XMLelement("CCC")
        public CCC node_CCC;
    }

    public static class CCC {
    }

    public static class x111 {
        @XMLnamespaces("xnumber")
        @XMLelement("x222")
        public x222 node_x222;
    }

    public static class x222 {
    }
}
