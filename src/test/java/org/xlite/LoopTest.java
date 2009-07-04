/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
package org.xlite;

import org.testng.annotations.ExpectedExceptions;

import java.io.StringReader;

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

    // classes Test and Back have a circular reference

    private static class Test {
        @XMLelement
        public Back node;
    }

    private static class Back {
        @XMLelement
        public Test node;
    }

}
