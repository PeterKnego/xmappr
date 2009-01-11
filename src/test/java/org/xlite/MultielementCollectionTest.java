package org.xlite;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.xml.sax.SAXException;
import org.xlite.*;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

/**
 * @author peter
 */
public class MultielementCollectionTest {

    public static String xml = "<root>" +
            "<a>textA1</a>" +
            "<a>textA2</a>" +
//            "<unknown>XXX</unknown>" +
            "<b>textB1</b>" +
            "<b>textB2</b>" +
//            "<data>YYY</data>" +
            "</root>";

    @org.testng.annotations.Test
    public void test() throws IOException, SAXException {

        StringReader reader = new StringReader(xml);
        Xlite xlite = new Xlite(Root.class, "root");
        xlite.setStoringUnknownElements(false);
        xlite.setPrettyPrint(false);

        Root one = (Root) xlite.fromXML(reader);

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

    public static class Root {
        @XMLelements({
                @XMLelement(name = "a", itemType = A.class),
                @XMLelement(name = "b", itemType = B.class)
//                @XMLelement(name = "unknown", itemType = ElementHolder.class)
        })
//        @XMLelement(name = "a", itemType = A.class)
        public List letters;

//        @XMLelement("data")
        public ElementHolder holder;
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
