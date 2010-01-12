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
import java.util.ArrayList;
import java.util.List;

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

        // Double step to make Xmappr work harder (not necessary normally - do not copy)
        // Reads Class configuration, produces XML configuration from it and then feeds it to Xmappr
//        StringReader configuration = XmlConfigTester.reader(Root.class);
        Xmappr xmappr = new Xmappr(Root.class);
        xmappr.setPrettyPrint(true);

//        Xmappr.Result result = xmappr.fromXMLwithUnmapped(reader);
//        Root one = (Root) result.getObject();
        Root one = (Root) xmappr.fromXML(reader);

        // writing back to XML
        StringWriter sw = new StringWriter();
//        xmappr.toXML(one, result.getStore(), sw);
        xmappr.toXML(one, sw);
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
                @Element(name = "a", targetType = A.class),
                @Element(name = "b", targetType = B.class)
        })
        @Text
        private List letters;

        public List getLetters() {
            return letters;
        }

        public void setLetters(List letters) {
            this.letters = letters;
        }
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
