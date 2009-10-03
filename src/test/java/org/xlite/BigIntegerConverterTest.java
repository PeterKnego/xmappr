/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
package org.xlite;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.testng.Assert;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;

public class BigIntegerConverterTest {

    private static String inXml = "" +
            "<test>" +
            "<node>987654321987654321</node>" +
            "</test>";

    @org.testng.annotations.Test
    public void test() throws IOException, SAXException {
        StringReader reader = new StringReader(inXml);
        Configuration conf = new Configuration(Test.class);
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

        Assert.assertEquals(test.node, new BigInteger("987654321987654321"));
        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(inXml, ssw);

    }

    @RootElement("test")
    public static class Test {
        @Element
        public BigInteger node;
    }
}
