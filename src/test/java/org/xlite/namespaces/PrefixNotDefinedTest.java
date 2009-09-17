/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
package org.xlite.namespaces;

import org.testng.annotations.ExpectedExceptions;
import org.xlite.*;

import java.io.StringReader;

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
        @Element("x:node")
        public String node;
    }
}
