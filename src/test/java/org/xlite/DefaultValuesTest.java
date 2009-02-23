package org.xlite;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.xml.sax.SAXException;
import org.xlite.Xlite;
import org.xlite.XMLelement;
import org.xlite.XMLtext;
import org.testng.Assert;

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
        Configuration conf = new AnnotationConfiguration(Root.class, "root");
        conf.setPrettyPrint(false);
        Xlite xlite = new Xlite(conf);

        Root root = (Root) xlite.fromXML(reader);

        // writing back to XML
        StringWriter sw = new StringWriter();
        xlite.toXML(root, sw);
        String ssw = sw.toString();
        System.out.println("");
        System.out.println(xml);
        System.out.println("");
        System.out.println(ssw);

        Assert.assertEquals(root.a.text, "text1");
        Assert.assertEquals(root.b, 0);
        Assert.assertEquals(root.c, 3);
        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(xml, ssw);
    }

    public static class Root {
        @XMLelement
        public A a;

        @XMLelement(defaultValue = "0")
        public int b;

        @XMLelement(defaultValue = "3")
        public int c;
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
