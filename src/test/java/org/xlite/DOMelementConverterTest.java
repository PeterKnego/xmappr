/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
package org.xlite;

import org.xml.sax.SAXException;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XMLAssert;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

public class DOMelementConverterTest {

    private static String xml = "<root>" +
            "first some text" +
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
        Configuration conf = new AnnotationConfiguration(Root.class);
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

    @RootElement("root")
    public static class Root {
        @Elements({
                @Element(name = "b", itemType = B.class),
                @Element("*")
        })
        @Text
        public List subelements;

    }

    public static class B {
        @Text
        public String text;
    }
}
