/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
package org.xlite;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

public class MultielementCollectionTest {

    private static String xml = "<root>" +
            "text at beginning" +
            "<a>textA1</a>" +
            "<b>textB2</b>" +
            "just some text" +
            "<a>textA2</a>" +
            "<b>textB1</b>" +
            "some more text" +
//            "<data>YYY</data>" +
            "</root>";

    @Test
    public void test() throws IOException, SAXException {

        StringReader reader = new StringReader(xml);

        // Double step to make Xlite work harder (not necessary normally - do not copy)
        // Reads Class configuration, produces XML configuration from it and then feeds it to Xlite
//        StringReader configuration = XmlConfigTester.reader(Root.class);
        Xlite xlite = new Xlite(Root.class);
        xlite.setPrettyPrint(true);

//        Xlite.Result result = xlite.fromXMLwithUnmapped(reader);
//        Root one = (Root) result.getObject();
        Root one = (Root) xlite.fromXML(reader);

        // writing back to XML
        StringWriter sw = new StringWriter();
//        xlite.toXML(one, result.getStore(), sw);
        xlite.toXML(one, sw);
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
                @Element(name = "a", itemType = A.class),
                @Element(name = "b", itemType = B.class)
        })
        @Text
        public ArrayList letters;

    }

    public static class A {
        @Text
        public String text;
    }

    public static class B {
        @Text
        public String text;
    }

}
