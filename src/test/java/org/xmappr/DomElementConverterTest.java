/*
 * This software is released under the BSD license. Full license available at http://xmappr.googlecode.com
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xmappr contributors
 * All rights reserved.
 */
package org.xmappr;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

public class DomElementConverterTest {

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

    @Test
    public void test() throws IOException, SAXException {

        StringReader reader = new StringReader(xml);

        // Double step to make Xmappr work harder (not necessary normally - do not copy)
        // Reads Class configuration, produces XML configuration from it and then feeds it to Xmappr
        StringReader configuration = XmlConfigTester.reader(Root.class);
        Xmappr xmappr = new Xmappr(configuration);
        xmappr.setPrettyPrint(false);

        Root root = (Root) xmappr.fromXML(reader);

        // writing back to XML
        StringWriter sw = new StringWriter();
        xmappr.toXML(root, sw);
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
                @Element(name = "b", targetType = B.class),
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
