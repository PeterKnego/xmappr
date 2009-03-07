package org.xlite;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.xml.sax.SAXException;
import org.xlite.converters.ElementStoreConverter;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

/**
 * @author peter
 */
public class ElementStoreTest {
    public static String xml = "<root>" +
            "<b></b>" +
            "<a>text1</a>" +
            "<a></a>" +
            "<unknown>textUnknown</unknown>" +
            "</root>";

    @org.testng.annotations.Test
    public void test() throws IOException, SAXException {

        StringReader reader = new StringReader(xml);
        Configuration conf = new AnnotationConfiguration(RootOne.class, "root");
        conf.setPrettyPrint(false);
        Xlite xlite = new Xlite(conf);

        RootOne one = (RootOne) xlite.fromXML(reader);

        // writing back to XML
        StringWriter sw = new StringWriter();
        xlite.toXML(one, sw);
        String ssw = sw.toString();
        System.out.println("");
        System.out.println(xml);
        System.out.println("");
        System.out.println(ssw);

        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(xml, ssw);
    }

    public static class RootOne {
        @XMLelements({
                @XMLelement(name = "a", itemType = A.class),
                @XMLelement(name = "b", itemType = B.class)
        })
        public List letters;

        @XMLelement(name="unknown")
        public ElementStore store;
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
