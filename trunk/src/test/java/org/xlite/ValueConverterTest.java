package org.xlite;

import org.testng.annotations.ExpectedExceptions;
import org.testng.Assert;

import java.io.StringReader;

/**
 * Created by IntelliJ IDEA.
 * User: peter
 * Date: Jun 11, 2009
 * Time: 11:39:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class ValueConverterTest {

    private static String inXml = "" +
            "<test>" +
            "<node></node>" +
            "</test";

    @org.testng.annotations.Test
    @ExpectedExceptions(XliteException.class)
    public void test() {
        StringReader reader = new StringReader(inXml);
        Configuration conf = new AnnotationConfiguration(Test.class, "test");
        Xlite xf = new Xlite(conf);

        Test test = (Test) xf.fromXML(reader);

    }

    private static class Test {
        @XMLelement
        public Integer node;
    }
}
