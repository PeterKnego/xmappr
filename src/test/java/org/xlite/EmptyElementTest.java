/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
package org.xlite;

import org.testng.annotations.ExpectedExceptions;
import org.testng.Assert;

import java.io.StringReader;

public class EmptyElementTest {

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

    public static class Test {
        @XMLelement
        public Integer node;
    }
}
