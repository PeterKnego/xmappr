package org.xlite.namespaces;

import org.testng.Assert;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XMLAssert;
import org.xml.sax.SAXException;
import org.xlite.*;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.IOException;

/**
 * Test where all namespaces are defined in root node
 *
 * @author peter
 */
public class RootNsTest {

    static String xml = "" +
            "<lower:aaa xmlns:lower=\"lowercase\" xmlns:upper=\"uppercase\" xmlns:xnumber=\"xnumber\">\n" +
            "  <lower:bbbb >\n" +
            "    <lower:cccc />\n" +
            "  </lower:bbbb>\n" +
            "  <upper:BBB >\n" +
            "    <upper:CCC />\n" +
            "  </upper:BBB>\n" +
            "  <xnumber:x111 >\n" +
            "    <xnumber:x222 />\n" +
            "  </xnumber:x111>\n" +
            "</lower:aaa>";

    @org.testng.annotations.Test
    public void test() throws IOException, SAXException {
        StringReader reader = new StringReader(xml);
        StringWriter writer = new StringWriter();
        Configuration conf = new AnnotationConfiguration(aaa.class, "l:aaa");

        // predefined namespaces
        conf.addNamespace("l=lowercase");
        conf.addNamespace("u=uppercase");

        Xlite xlite = new Xlite(conf);
        aaa a = (aaa) xlite.fromXML(reader);

        Assert.assertTrue(a.node_bbbb.node_cccc != null);
        Assert.assertTrue(a.node_BBB.node_CCC != null);
        Assert.assertTrue(a.node_x111.node_x222 != null);

        xlite.toXML(a, writer);
        System.out.println(writer.toString());

        // writing back to XML
        StringWriter sw = new StringWriter();
        xlite.toXML(a, sw);
        System.out.println(sw);
        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(xml, sw.toString());
    }

    public static class aaa {
        @XMLelement("l:bbbb")
        public bbbb node_bbbb;

        @XMLelement("u:BBB")
        public BBB node_BBB;

        @XMLnamespaces("xn=xnumber")
        @XMLelement("xn:x111")
        public x111 node_x111;
    }

    public static class bbbb {
        @XMLelement("l:cccc")
        public cccc node_cccc;
    }

    public static class cccc {
    }

    public static class BBB {
        @XMLelement("u:CCC")
        public CCC node_CCC;
    }

    public static class CCC {
    }

    @XMLnamespaces("xn=xnumber")
    public static class x111 {
        @XMLelement("xn:x222")
        public x222 node_x222;
    }

    public static class x222 {
    }

}
