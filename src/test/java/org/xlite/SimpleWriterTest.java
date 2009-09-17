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

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.xlite.Xlite;
import org.xlite.Attribute;
import org.xlite.Namespaces;
import org.xlite.Element;

public class SimpleWriterTest {

    private static String xml1 = "<a xmlns=\"ns1\" xmlns:sec=\"ns2\">\n" +
            "<sec:b>\n" +
            "<c>\n" +
            "<d attr=\"DDD\" />\n" +
            "</c>\n" +
            "</sec:b>\n" +
            "</a>";

    @org.testng.annotations.Test
    public void testEquality() throws XMLStreamException, IOException, SAXException {
        StringReader reader = new StringReader(xml1);

        Configuration conf = new AnnotationConfiguration(A.class);
        conf.addNamespace("ns1");

        Xlite xlite = new Xlite(conf);
        A a = (A) xlite.fromXML(reader);
//        System.out.println(xml1);

        // writing back to XML
        StringWriter sw = new StringWriter();
        xlite.toXML(a, sw);
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

    //    @Namespaces("ns2")
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
