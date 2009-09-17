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
import java.math.BigDecimal;

public class BigDecimalConverterTest {

    private static String inXml = "" +
            "<test>" +
            "<node>123.456</node>" +
            "</test>";

    @org.testng.annotations.Test
    public void test() throws IOException, SAXException {
        StringReader reader = new StringReader(inXml);
        Configuration conf = new AnnotationConfiguration(Test.class);
        Xlite xlite = new Xlite(conf);

        Test test = (Test) xlite.fromXML(reader);

        // writing back to XML
        StringWriter sw = new StringWriter();
        xlite.toXML(test, sw);
        String ssw = sw.toString();
        System.out.println("");
        System.out.println(inXml);
        System.out.println("");
        System.out.println(ssw);

        Assert.assertEquals(test.node, new BigDecimal("123.456"));
        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(inXml, ssw);

    }

    @RootElement("test")
    public static class Test {
        @Element
        public BigDecimal node;
    }
}
