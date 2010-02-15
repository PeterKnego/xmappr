/*
 * This software is released under the BSD license. Full license available at http://xmappr.googlecode.com
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xmappr contributors
 * All rights reserved.
 */
package org.xmappr;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.xmappr.annotation.Element;
import org.xmappr.annotation.RootElement;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

public class LoopTest {

    private static String inXml = "" +
            "<test>" +
            " <back>" +
            "  <test>" +
            "   <back>" +
            "   </back>" +
            "  </test>" +
            " </back>" +
            "</test>";

    @org.testng.annotations.Test
    public void configurationLoopTest() throws IOException, SAXException {
        StringReader reader = new StringReader(inXml);
        Xmappr xmappr = new Xmappr(Test.class);
        xmappr.setPrettyPrint(true);
        Test test = (Test) xmappr.fromXML(reader);

        // writing back to XML
        StringWriter sw = new StringWriter();
        xmappr.toXML(test, sw);

        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(inXml, sw.toString());
    }

    // classes Test and Back have a circular reference
    @RootElement("test")
    public static class Test {
        @Element
        public Back back;
    }

    public static class Back {
        @Element
        public Test test;
    }

}
