/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
package org.xlite;

import org.xml.sax.SAXException;
import org.testng.Assert;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XMLAssert;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;

public class ByteArrayConverterTest {

    private static String inXml = "" +
            "<test>" +
            "<node>TG9yZW0gaXBzdW0gZG9sb3Igc2l0IGFtZXQsIGNvbnNlY3RldHVyIGFkaXBpc2NpbmcgZWxpdC4gTnVsbGEgYWRpcGlzY" +
            "2luZyB1bHRyaWNpZXMgcHVydXMgcXVpcyBzb2xsaWNpdHVkaW4uIEZ1c2NlIGV1aXNtb2QgcGxhY2VyYXQgZWxpdCwgdmVsIG1v" +
            "bGVzdGllIG5pc2kgcG9ydGEgbm9uLiBBbGlxdWFtIHNhZ2l0dGlzIGFyY3UgZWdldCBtYWduYSBwcmV0aXVtIGF0IGJpYmVuZHV" +
            "tIGxlY3R1cyBsYW9yZWV0LiBQZWxsZW50ZXNxdWUgcGVsbGVudGVzcXVlIG1hdXJpcyBpZCB0ZWxsdXMgY29uc2VjdGV0dXIgZX" +
            "UgZWxlbWVudHVtIG5pYmggcG9zdWVyZS4=</node>" +
            "</test>";

    private static String loremIpsum = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla adipiscing " +
            "ultricies purus quis sollicitudin. Fusce euismod placerat elit, vel molestie nisi porta non. Aliquam " +
            "sagittis arcu eget magna pretium at bibendum lectus laoreet. Pellentesque pellentesque mauris id " +
            "tellus consectetur eu elementum nibh posuere.";

    @org.testng.annotations.Test
    public void test() throws IOException, SAXException {
        StringReader reader = new StringReader(inXml);
        Configuration conf = new AnnotationConfiguration(Test.class, "test");
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

        Assert.assertEquals(new String(test.node), loremIpsum);
        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(inXml, ssw);

    }

    public static class Test {
        @XMLelement
        public byte[] node;
    }
}