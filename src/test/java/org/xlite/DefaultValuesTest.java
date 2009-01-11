package org.xlite;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.xml.sax.SAXException;
import org.xlite.Xlite;
import org.xlite.XMLelement;
import org.xlite.XMLtext;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * @author peter
 */
public class DefaultValuesTest {

    public static String xml = "<root>" +
            "<a>text1</a>" +
            "<b/>" +
            "<c></c>" +
            "</root>";

    @org.testng.annotations.Test
    public void test() throws IOException, SAXException {

        StringReader reader = new StringReader(xml);
        Xlite xlite = new Xlite(Root.class, "root");
        xlite.setPrettyPrint(false);

        Root root = (Root) xlite.fromXML(reader);

        // writing back to XML
        StringWriter sw = new StringWriter();
        xlite.toXML(root, sw);
        String ssw = sw.toString();
        System.out.println("");
        System.out.println(xml);
        System.out.println("");
        System.out.println(ssw);

        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(xml, ssw);
    }

    public static class Root {
        @XMLelement
        public A a;

        @XMLelement (defaultValue = "0")
        public Integer b;

        @XMLelement (defaultValue = "0")
        public Integer c;
    }

    public static class A {
        @XMLtext
        public String text;
    }

    public static class B {
        @XMLtext
        public String text;
    }
}
