package org.xlite.namespaces;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.testng.Assert;
import org.xml.sax.SAXException;
import org.xlite.XMLnamespaces;
import org.xlite.XMLelement;
import org.xlite.Xlite;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.IOException;

/**
 * Test where default namespaces are used, but namespaces for chosen elements can still be explicitly stated.
 *
 * @author peter
 */
public class DefaultNsOverridingTest {

    static String xml = "<aaa xmlns:upper = \"uppercase\" xmlns:xnumber = \"xnumber\" >\n" +
            "          <bbb xmlns = \"lowercase\" >\n" +
            "               <ccc />\n" +
            "               <upper:WWW />\n" +
            "               <xnumber:x666 />\n" +
            "          </bbb>\n" +
            "          <BBB xmlns = \"uppercase\" >\n" +
            "               <upper:WWW />\n" +
            "               <xnumber:x666 />\n" +
            "               <CCC />\n" +
            "          </BBB>\n" +
            "          <x111 xmlns = \"xnumber\" >\n" +
            "               <x222 />\n" +
            "               <upper:WWW />\n" +
            "               <xnumber:x666 />\n" +
            "          </x111>\n" +
            "     </aaa>";

    @org.testng.annotations.Test
    public void test() throws IOException, SAXException {
        StringReader reader = new StringReader(xml);
        Xlite xlite = new Xlite(aaa.class, "aaa"); //todo THIS SHOULD BE AN ERROR <l:aaa> should not match <aaa>

        // predefined namespaces
        xlite.addNamespace("u=uppercase");
        xlite.addNamespace("xn=xnumber");
        aaa a = (aaa) xlite.fromXML(reader);

        Assert.assertTrue(a.node_bbb.node_ccc != null);
        Assert.assertTrue(a.node_bbb.node_WWW != null);
        Assert.assertTrue(a.node_bbb.node_x666 != null);
        Assert.assertTrue(a.node_BBB.node_CCC != null);
        Assert.assertTrue(a.node_BBB.node_WWW != null);
        Assert.assertTrue(a.node_BBB.node_x666 != null);
        Assert.assertTrue(a.node_x111.node_x222 != null);
        Assert.assertTrue(a.node_x111.node_WWW != null);
        Assert.assertTrue(a.node_x111.node_x666 != null);

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

        @XMLelement("u:WWW")
        public WWW node_WWW;

        @XMLelement("xn:x666")
        public x666 node_x666;
    }

    public static class ccc {
    }

    @XMLnamespaces("uppercase")
    public static class BBB {
        @XMLelement("CCC")
        public CCC node_CCC;

        @XMLelement("u:WWW")
        public WWW node_WWW;

        @XMLelement("xn:x666")
        public x666 node_x666;
    }

    public static class CCC {
    }

    @XMLnamespaces("xnumber")
    public static class x111 {
        @XMLelement("x222")
        public x222 node_x222;

        @XMLelement("u:WWW")
        public WWW node_WWW;

        @XMLelement("xn:x666")
        public x666 node_x666;
    }

    public static class x222 {
    }

    public static class x666 {
    }

    public static class WWW {
    }
}

