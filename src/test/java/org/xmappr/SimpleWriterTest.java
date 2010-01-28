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

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

public class SimpleWriterTest {

    private static String xml1 = "<a xmlns=\"ns1\" xmlns:sec=\"ns2\">\n" +
            "<sec:b>\n" +
            "<c>\n" +
            "<d attr=\"DDD\" />\n" +
            "</c>\n" +
            "</sec:b>\n" +
            "</a>";

    @Test
    public void testEquality() throws XMLStreamException, IOException, SAXException {
        StringReader reader = new StringReader(xml1);

        // Double step to make Xmappr work harder (not necessary normally - do not copy)
        // Reads Class configuration, produces XML configuration from it and then feeds it to Xmappr
//        StringReader configuration = XmlConfigTester.reader(A.class);
        Xmappr xmappr = new Xmappr(A.class);
        xmappr.addNamespace("", "ns1");

        A a = (A) xmappr.fromXML(reader);
//        System.out.println(xml1);

        // writing back to XML
        StringWriter sw = new StringWriter();
        xmappr.toXML(a, sw);
//        System.out.println(sw);
        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(xml1, sw.toString());
    }

    @RootElement("a")
    public static class A {
        @Namespaces("s=ns2")
        @Element("s:b")
        public B b;
    }

    public static class B {
        @Element
        public C c;

        @Element
        public C c2;
    }

    public static class C {
        @Element
        public D d;
    }

    public static class D {
        @Attribute
        public String attr;
    }

}
