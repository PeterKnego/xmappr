/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
package org.xlite;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;

public class MultiattributeMapTest {
    private static String in = "<root a='abc' b='123' c='mama' d='daddy' d1='daddy1' e='eee' f='555' g='ggg' />";
    private static String out = "<root a='abc' b='123' c='mama' d='daddy' d1='daddy1' d2='daddy2' e='eee' f='555' g='ggg' h='hhh' />";

    @Test
    public void test() throws IOException, SAXException {

        StringReader reader = new StringReader(in);
        Configuration conf = new Configuration(Root.class);
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

    @RootElement("root")
    public static class Root {
        @Attributes({
                @Attribute(name = "a"),
                @Attribute(name = "b", itemType = Integer.class),
                @Attribute(name = "c"),
                @Attribute("*")
        })
        public LinkedHashMap attrs;

        @Attributes({
                @Attribute(name = "e"),
                @Attribute(name = "f", itemType = Integer.class),
                @Attribute(name = "g"),
                @Attribute(name = "h")
        })
        public Map attrs2;
    }

}
