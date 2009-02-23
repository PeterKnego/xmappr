package org.xlite;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.xml.sax.SAXException;
import org.xlite.XMLelement;
import org.xlite.XMLelements;
import org.xlite.Xlite;
import org.xlite.XMLtext;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

/**
 * @author peter
 */
public class ElementHolderTest {
    public static String xml = "<root>" +
            "<b></b>" +
            "<a>text1</a>" +
            "<a></a>" +
//            "<unknown>textUnknown</unknown>" +
//            "<unknown2>textUnknown2</unknown2>" +
            "</root>";

    @org.testng.annotations.Test
    public void test() throws IOException, SAXException {

        StringReader reader = new StringReader(xml);
        Configuration conf = new AnnotationConfiguration(RootOne.class, "root");
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
        @XMLelements(
                {@XMLelement(name = "a", itemType = A.class)
//                @XMLelement(name = "unknown", converter = ElementHolderConverter.class)}
                }
        )
        public List letters;

        @XMLelement
        public B b;
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
