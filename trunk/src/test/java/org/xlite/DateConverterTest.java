package org.xlite;

import org.xml.sax.SAXException;
import org.testng.Assert;
import org.testng.annotations.ExpectedExceptions;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XMLAssert;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;

/**
 * Created by IntelliJ IDEA.
 * User: peter
 * Date: Jun 22, 2009
 * Time: 6:41:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class DateConverterTest {

    private static String inXml = "" +
            "<test>" +
            "<node>2001.07.04 12:08:56</node>" +
            "</test>";

    // wrong Date format for given formatter
    private static String inXml2 = "" +
            "<test>" +
            "<node>2001-07-04 12:08:56</node>" +
            "</test>";

    @org.testng.annotations.Test
    public void test() throws IOException, SAXException, ParseException {
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
        DateFormat df = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

        Assert.assertEquals(test.node, df.parse("2001.07.04 12:08:56"));
        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(inXml, ssw);

    }

    @org.testng.annotations.Test
    @ExpectedExceptions(XliteException.class)
    public void wrongFormatterTest() {
        StringReader reader = new StringReader(inXml2);
        Configuration conf = new AnnotationConfiguration(Test.class, "test");
        Xlite xlite = new Xlite(conf);

        Test test = (Test) xlite.fromXML(reader);
    }

    public static class Test {
        @XMLelement(format = "yyyy.MM.dd HH:mm:ss")
        public Date node;
    }

    @org.testng.annotations.Test
    @ExpectedExceptions(XliteConfigurationException.class)
    public void testEmptyFormat() {
        StringReader reader = new StringReader(inXml);
        Configuration conf = new AnnotationConfiguration(TestEmpty.class, "test");
        Xlite xlite = new Xlite(conf);

        TestEmpty test = (TestEmpty) xlite.fromXML(reader);
    }

    public static class TestEmpty {
        // format is empty
        @XMLelement(format = "")
        public Date node;
    }

    @org.testng.annotations.Test
    @ExpectedExceptions(XliteConfigurationException.class)
    public void testWrongFormat() {
        StringReader reader = new StringReader(inXml);
        Configuration conf = new AnnotationConfiguration(TestWrong.class, "test");
        Xlite xlite = new Xlite(conf);

        TestWrong test = (TestWrong) xlite.fromXML(reader);
    }

    public static class TestWrong {
        // format is plainly wrong
        @XMLelement(format = "wow this is a wrong format")
        public Date node;
    }
}
