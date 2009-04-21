package org.xlite;

import org.xml.sax.SAXException;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XMLAssert;
import org.testng.Assert;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: peter
 * Date: Apr 20, 2009
 * Time: 11:31:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class MultiattributeMapTest {
    public static String xml = "<root a='abc' b='123' c='mama' d='ata'/>";

    @org.testng.annotations.Test
    public void test() throws IOException, SAXException {

        StringReader reader = new StringReader(xml);
        Configuration conf = new AnnotationConfiguration(Root.class, "root");
        conf.setPrettyPrint(false);

        Xlite xlite = new Xlite(conf);
        Root one = (Root) xlite.fromXML(reader);

        Assert.assertEquals(one.attrs.get(new QName("a")), "abc");
        Assert.assertEquals(one.attrs.get(new QName("b")), "123");

        // writing back to XML
        StringWriter sw = new StringWriter();
        xlite.toXML(one, sw);
        System.out.println("");
        System.out.println(xml);
        System.out.println("");
        System.out.println(sw.toString());

//        XMLUnit.setIgnoreWhitespace(true);
//        XMLAssert.assertXMLEqual(xml, ssw);
    }

    public static class Root {
        @XMLattributes({
                @XMLattribute(name = "a", itemType = String.class),
                @XMLattribute(name = "b", itemType = Integer.class),
                @XMLattribute(name = "c", itemType = String.class),
                @XMLattribute(name = "d", itemType = String.class)
//                @XMLattribute(name = "a"),
//                @XMLattribute(name = "b")
        })
        public Map attrs;

    }
}
