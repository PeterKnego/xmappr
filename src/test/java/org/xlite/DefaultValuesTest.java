/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */

package org.xlite;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.xml.sax.SAXException;
import org.xlite.Xlite;
import org.xlite.XMLelement;
import org.xlite.XMLtext;
import org.testng.Assert;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

public class DefaultValuesTest {

    //todo Default values for attributes?
    private static String inXml = "<root attr='' >" +
            "<a>text1</a>" +
            "<b/>" +
            "</root>";

    private static String outXml = "<root attr='' >" +
            "<b/>" +
            "<c></c>" +
            "</root>";


    @org.testng.annotations.Test
    public void test() throws IOException, SAXException {

        StringReader reader = new StringReader(inXml);
        Configuration conf = new AnnotationConfiguration(Root.class, "root");
        conf.setPrettyPrint(false);
        Xlite xlite = new Xlite(conf);

        Root root = (Root) xlite.fromXML(reader);

        // check values
        Assert.assertEquals(root.a.text, "text1");
        Assert.assertEquals(root.b, 2);

        // change value to null - this is going to omit the element from output
        root.a = null;
        // 3 is a default value so this is going to produce an empty element
        root.c = 3;

        // writing back to XML
        StringWriter sw = new StringWriter();
        xlite.toXML(root, sw);
        String ssw = sw.toString();
        System.out.println("");
        System.out.println(outXml);
        System.out.println("");
        System.out.println(ssw);

        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(outXml, ssw);
    }

    public static class Root {

        @XMLattribute(defaultValue = "5")
        public int attr;

        @XMLelement
        public A a;

        @XMLelement(defaultValue = "2")
        public int b;

        @XMLelement(defaultValue = "3")
        public Integer c;
    }

    public static class A {
        @XMLtext
        public String text;
    }

}
