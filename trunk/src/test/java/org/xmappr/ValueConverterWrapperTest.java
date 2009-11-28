/*
 * This software is released under the BSD license. Full license available at http://xmappr.googlecode.com
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xmappr contributors
 * All rights reserved.
 */
package org.xmappr;

import java.io.StringReader;

public class ValueConverterWrapperTest {

    public static String xml = "" +
            "<root a=\"2.2\">\n" +
            "some text\n" +
            "<node>\n" +
            "123\n" +      // On purpose on it's own line
            "</node>\n" +
            "</root>";

    public static void main(String[] args) {

        StringReader reader = new StringReader(xml);

        // Double step to make Xmappr work harder (not necessary normally - do not copy)
        // Reads Class configuration, produces XML configuration from it and then feeds it to Xmappr
        StringReader configuration = XmlConfigTester.reader(Root.class);
        Xmappr xmappr = new Xmappr(configuration);
        Root root = (Root) xmappr.fromXML(reader);
    }

    @RootElement
    public static class Root {

        @Attribute
        public float a;

        @Element
        public Integer node;

        @Text
        public String text;
    }


}

