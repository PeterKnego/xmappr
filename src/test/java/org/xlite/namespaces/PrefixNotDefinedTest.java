package org.xlite.namespaces;

import org.testng.annotations.ExpectedExceptions;
import org.xlite.*;

import java.io.StringReader;

/**
 * Created by IntelliJ IDEA.
 * User: peter
 * Date: Jun 14, 2009
 * Time: 12:02:21 AM
 * To change this template use File | Settings | File Templates.
 */
public class PrefixNotDefinedTest {
    private static String inXml = "" +
            "<test xmlns:x='someNS'>" +
            "<x:node>some text</x:node>" +
            "</test";

    @org.testng.annotations.Test
    @ExpectedExceptions(XliteConfigurationException.class)
    public void test() {
        StringReader reader = new StringReader(inXml);
        Configuration conf = new AnnotationConfiguration(Test.class, "test");
        Xlite xf = new Xlite(conf);

        Test test = (Test) xf.fromXML(reader);

    }


    public static class Test {
        // prefix 'x' is not declared 
        @XMLelement("x:node")
        public String node;
    }
}
