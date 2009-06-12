package org.xlite;

import org.xml.sax.SAXException;
import org.testng.Assert;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XMLAssert;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Created by IntelliJ IDEA.
 * User: peter
 * Date: Jun 12, 2009
 * Time: 11:47:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class BigIntegerConverterTest {

    private static String inXml = "" +
            "<test>" +
            "<node>987654321987654321</node>" +
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

        Assert.assertEquals(test.node, new BigInteger("987654321987654321"));
        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(inXml, ssw);

    }

    public static class Test {
        @XMLelement
        public BigInteger node;
    }
}
