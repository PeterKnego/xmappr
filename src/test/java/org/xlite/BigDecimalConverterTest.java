package org.xlite;

import org.testng.Assert;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XMLAssert;
import org.xml.sax.SAXException;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: peter
 * Date: Jun 12, 2009
 * Time: 11:22:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class BigDecimalConverterTest {

    private static String inXml = "" +
            "<test>" +
            "<node>123.456</node>" +
            "</test>";

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

        Assert.assertEquals(test.node, new BigDecimal("123.456"));
        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(inXml, ssw);

    }

    public static class Test {
        @XMLelement
        public BigDecimal node;
    }
}
