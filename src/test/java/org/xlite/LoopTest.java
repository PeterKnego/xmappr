package org.xlite;

import org.testng.annotations.ExpectedExceptions;

import java.io.StringReader;

/**
 * Created by IntelliJ IDEA.
 * User: peter
 * Date: Jun 12, 2009
 * Time: 9:39:44 AM
 * To change this template use File | Settings | File Templates.
 */
public class LoopTest {

    private static String inXml = "" +
            "<test>" +
            "<node></node>" +
            "</test";

    @org.testng.annotations.Test
    @ExpectedExceptions(XliteConfigurationException.class)
    public void configurationLoopTest() {
        StringReader reader = new StringReader(inXml);
        Configuration conf = new AnnotationConfiguration(Test.class, "test");
        Xlite xf = new Xlite(conf);

        Test test = (Test) xf.fromXML(reader);

    }

    private static class Test {
        @XMLelement
        public Back node;
    }

    private static class Back {
        @XMLelement
        public Test node;
    }

}
