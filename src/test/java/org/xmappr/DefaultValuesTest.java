/*
 * This software is released under the BSD license. Full license available at http://xmappr.googlecode.com
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xmappr contributors
 * All rights reserved.
 */

package org.xmappr;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

public class DefaultValuesTest {

    private static String inXml = "<root>" +
            "<a>text1</a>" +
            "<b/>" +
            "</root>";

    private static String outXml = "<root>" +
            "<b/>" +
            "<c/>" +
            "</root>";


    @Test
    public void test() throws IOException, SAXException {

        StringReader reader = new StringReader(inXml);

        // Double step to make Xmappr work harder (not necessary normally - do not copy)
        // Reads Class configuration, produces XML configuration from it and then feeds it to Xmappr
        StringReader configuration = XmlConfigTester.reader(Root.class);
        Xmappr xmappr = new Xmappr(configuration);
        xmappr.setPrettyPrint(false);

        Root root = (Root) xmappr.fromXML(reader);

        // check values
        Assert.assertEquals(root.one, 5);
        Assert.assertEquals(root.a.text, "text1");
        Assert.assertEquals(root.b, 2);
        Assert.assertNull(root.c);

        // change value to null - this is going to omit the element from output
        root.a = null;
        // 3 is a default value so this is going to produce an empty element
        root.c = 3;

        // writing back to XML
        StringWriter sw = new StringWriter();
        xmappr.toXML(root, sw);
        String ssw = sw.toString();
        System.out.println("");
        System.out.println(outXml);
        System.out.println("");
        System.out.println(ssw);

        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(outXml, ssw);
    }

    @RootElement
    public static class Root {

        @Attribute(defaultValue = "5")
        public int one;

        @Element
        public A a;

        @Element(defaultValue = "2")
        public int b;

        @Element(defaultValue = "3")
        public Integer c;
    }

    public static class A {
        @Text
        public String text;
    }

}
