package org.xlite;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.xml.sax.SAXException;

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
            "just some text"+
            "<b>textB1</b>" +
            "<b>textB2</b>" +
            "some more text"+
            "<data>YYY</data>" +
            "</root>";

    @org.testng.annotations.Test
    public void test() throws IOException, SAXException {

        StringReader reader = new StringReader(xml);
        Configuration conf = new AnnotationConfiguration(Root.class, "root");
        conf.setPrettyPrint(false);

        Xlite xlite = new Xlite(conf);
        Xlite.Result result = xlite.fromXMLwithUnknown(reader);
        Root one = (Root) result.getObject();

        // writing back to XML
        StringWriter sw = new StringWriter();
        xlite.toXML(one, result.getStore(), sw);
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
//                @XMLelement(name = "unknown", itemType = ElementStore.class)
        })
        @XMLtext
        public List letters;

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
