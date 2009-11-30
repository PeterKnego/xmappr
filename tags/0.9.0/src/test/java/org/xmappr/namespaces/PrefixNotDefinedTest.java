/*
 * This software is released under the BSD license. Full license available at http://xmappr.googlecode.com
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xmappr contributors
 * All rights reserved.
 */
package org.xmappr.namespaces;

import org.xmappr.*;

import java.io.StringReader;

public class PrefixNotDefinedTest {
    private static String inXml = "" +
            "<test xmlns:x='someNS'>" +
            "<x:node>some text</x:node>" +
            "</test";

    @org.testng.annotations.Test(expectedExceptions = XmapprException.class)
    public void test() {
        StringReader reader = new StringReader(inXml);

        // Double step to make Xmappr work harder (not necessary normally - do not copy)
        // Reads Class configuration, produces XML configuration from it and then feeds it to Xmappr
        StringReader configuration = XmlConfigTester.reader(Test.class);
        Xmappr xmappr = new Xmappr(configuration);

        Test test = (Test) xmappr.fromXML(reader);

    }


    @RootElement("test")
    public static class Test {
        // prefix 'x' is not declared 
        @Element("x:node")
        public String node;
    }
}
