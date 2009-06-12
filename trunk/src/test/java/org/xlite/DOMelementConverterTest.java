package org.xlite;

import org.xml.sax.SAXException;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XMLAssert;
import org.xlite.converters.DOMelementConverter;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: peter
 * Date: Jun 4, 2009
 * Time: 4:29:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class DOMelementConverterTest {

    private static String xml = "<root>" +
            "first some text"+
            "<b>textB2</b>" +
            "<a>textA2" +
            "<a2 a2attr1='some' a2attr2='someMore' >a2text</a2>" +
            "</a>" +
            "just some text" +
            "<a>textA1</a>" +
            "<b>textB1</b>" +
            "some more text" +
            "<data>YYY</data>" +
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

        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(xml, ssw);
    }

    public static class Root {
        @XMLelements({
                @XMLelement(name = "b", itemType = B.class),
                @XMLelement("*")
        })
        @XMLtext
        public List subelements;

    }

    public static class B {
        @XMLtext
        public String text;
    }
}
