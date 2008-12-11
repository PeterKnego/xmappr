package info.documan.xlite;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.IOException;
import java.util.List;

import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XMLAssert;
import org.xml.sax.SAXException;

/**
 * @author peter
 */
public class ElementHolderTest {
        public static String xml = "<root>" +
            "<a>text1</a>" +
                "<a>text2</a>" +
//            "<unknown>textUnknown</unknown>" +
//            "<unknown2>textUnknown2</unknown2>" +
            "</root>";

    @org.testng.annotations.Test
    public void test() throws IOException, SAXException {

        StringReader reader = new StringReader(xml);
        Xlite xlite = new Xlite(RootOne.class, "root");

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
    }

    public static class A {
        @XMLtext
        public String text;
    }

}
