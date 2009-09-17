/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
package org.xlite;

import org.testng.Assert;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XMLAssert;
import org.xml.sax.SAXException;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.IOException;

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
        Configuration conf = new AnnotationConfiguration(Test.class, "test");
        conf.setPrettyPrint(true);
        Xlite xlite = new Xlite(conf);

        Test test = (Test) xlite.fromXML(reader);
        Assert.assertNotNull(test.back);

        // writing back to XML
        StringWriter sw = new StringWriter();
        xlite.toXML(test, sw);

        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(inXml, sw.toString());
    }

    // classes Test and Back have a circular reference

    public static class Test {
        @Element
        public Back back;
    }

    public static class Back {
        @Element
        public Test test;
    }

}
