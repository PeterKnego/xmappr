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
import java.util.LinkedHashMap;

/**
 * Created by IntelliJ IDEA.
 * User: peter
 * Date: Apr 20, 2009
 * Time: 11:31:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class MultiattributeMapTest {
    private static String in = "<root a='abc' b='123' c='mama' d='daddy' d1='daddy1' e='eee' f='555' g='ggg' />";
    private static String out = "<root a='abc' b='123' c='mama' d='daddy' d1='daddy1' d2='daddy2' e='eee' f='555' g='ggg' h='hhh' />";

    @org.testng.annotations.Test
    public void test() throws IOException, SAXException {

        StringReader reader = new StringReader(in);
        Configuration conf = new AnnotationConfiguration(Root.class, "root");
        conf.setPrettyPrint(false);

        Xlite xlite = new Xlite(conf);
        Root one = (Root) xlite.fromXML(reader);

        Assert.assertEquals(one.attrs.get(new QName("a")), "abc");
        Assert.assertEquals(one.attrs.get(new QName("b")), 123);
        Assert.assertEquals(one.attrs.get(new QName("c")), "mama");
        one.attrs.put("d2", "daddy2");   // this one is mapped to wildcard attribute "*"
        one.attrs2.put(new QName("h"), "hhh");
        one.attrs2.put(new QName("x"), "xxx");  // this one is not mapped and will be ignored

        // writing back to XML
        StringWriter sw = new StringWriter();
        xlite.toXML(one, sw);
        System.out.println("");
        System.out.println(in);
        System.out.println("");
        System.out.println(sw.toString());

        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(out, sw.toString());
    }

    public static class Root {
        @XMLattributes({
                @XMLattribute(name = "a"),
                @XMLattribute(name = "b", itemType = Integer.class),
                @XMLattribute(name = "c"),
                @XMLattribute("*")
        })
        public LinkedHashMap attrs;

        @XMLattributes({
                @XMLattribute(name = "e"),
                @XMLattribute(name = "f", itemType = Integer.class),
                @XMLattribute(name = "g"),
                @XMLattribute(name = "h")
        })
        public Map attrs2;
    }

}
