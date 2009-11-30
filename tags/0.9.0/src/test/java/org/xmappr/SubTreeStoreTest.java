/*
 * This software is released under the BSD license. Full license available at http://xmappr.googlecode.com
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xmappr contributors
 * All rights reserved.
 */
package org.xmappr;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

public class SubTreeStoreTest {

//    static String xml = "<a xmlns=\"ns1\" xmlns:s=\"ns2\">\n" +
//            "<s:b>\n" +
//            "<c>\n" +
//            "<i:ignored xmlns:i=\"iii\" ia=\"11\" ia2=\"12\">" +
//            "<ign/>" +
//            "IGNORED" +
//            "<subignored asub=\"666\"><subsub/></subignored>" +
//            "</i:ignored>\n" +
//            "<d attrD=\"DDD\" ></d>\n" +
//            "</c>\n" +
//            "</s:b>\n" +
//            "</a>";


    private static String xml = "<x:a xmlns:x=\"ns1\" xmlns:s=\"ns2\" xmlns:w=\"ns3\">\n" +
            "<s:b>\n" +
            "<x:c>CCCCCCC\n" +
            "<x:d attrD=\"DDD\" ></x:d>\n" +
            "<x:e>EEEE</x:e>\n" +
//            "<w:emptico xmlns:w=\"www\"/>\n" +
            "<x:subignored asub=\"666\">\n" +
            "<x:subsub/>\n" +
            "</x:subignored>\n" +
            "<w:one aa=\"prvi\">\n" +
            "<w:empty/>\n" +
            "</w:one>\n" +
            "</x:c>\n" +
            "</s:b>\n" +
            "</x:a>";

    @Test
    public void testStoringNodes() throws IOException, SAXException, XpathException {
        StringReader reader = new StringReader(xml);

        Xmappr xmappr = new Xmappr(A.class);
        xmappr.addNamespace("x","ns1");
        xmappr.addNamespace("s","ns2");
        xmappr.addNamespace("w","ns3");

        Xmappr.Result result = xmappr.fromXMLwithUnmapped(reader);
        A a = (A) result.getObject();

        // writing back to XML
        StringWriter sw = new StringWriter();
        xmappr.toXML(a, result.getStore(), sw);
        String ssw = sw.toString();
        System.out.println("");
        System.out.println(xml);
        System.out.println("");
        System.out.println(ssw);

        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(xml, ssw);
    }

    @RootElement("x:a")
    public static class A {
        //        @Namespaces("s=ns2")
        @Element("s:b")
        public B b;
    }

    public static class B {
        @Element("x:c")
        public C c;
    }

    public static class C {

        @Element("x:d")
        public D d;

        @Text
        public String text;
    }

    public static class D {
        @Attribute
        public String attrD;
    }

}
