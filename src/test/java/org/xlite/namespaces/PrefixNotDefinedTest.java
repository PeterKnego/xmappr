/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
package org.xlite.namespaces;

import org.xlite.*;

import java.io.StringReader;

public class PrefixNotDefinedTest {
    private static String inXml = "" +
            "<test xmlns:x='someNS'>" +
            "<x:node>some text</x:node>" +
            "</test";

    @org.testng.annotations.Test(expectedExceptions = XliteException.class)
    public void test() {
        StringReader reader = new StringReader(inXml);
        Xlite xlite = new Xlite(Test.class);

        Test test = (Test) xlite.fromXML(reader);

    }


    @RootElement("test")
    public static class Test {
        // prefix 'x' is not declared 
        @Element("x:node")
        public String node;
    }
}
