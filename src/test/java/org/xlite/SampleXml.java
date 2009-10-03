/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
package org.xlite;

public class SampleXml {

    static String xml =
            "<one attr1=\"text1\" attr2=\"1111\" attr3=\"1.1\">" +
                    "just a<!-- comment should be ignored --> text" +
                    "<ignored>Ignored<subignored/></ignored>" +
                    "<emptyNode attrEmpty=\"-1.6\"/>" +
                    "<two attr4=\"true\" attr5=\"x\" >" +
                    "textTwo" +
                    "<three1 val=\"42\">" +
                    "textThree" +
                    "</three1>" +
                    "<nodeWithSubnodes>" +
                    "<integer>2008</integer>" +
                    "<bool>true</bool>" +
                    "<char>f</char>" +
                    "<float>-15.555</float>" +
                    "</nodeWithSubnodes>" +
                    "</two>" +
                    "</one>";


    @RootElement("one")
    public static class One {

        @Attribute("attr1")
        public String attr;

        @Attribute
        public int attr2;

        @Attribute
        public float attr3;

        @Text
        public String text;

        @Element
        public Empty emptyNode;

        @Element
        public Two two;

    }

    public static class Two {

        @Attribute
        public boolean attr4;

        @Attribute("attr5")
        public char character;

        @Text
        public String text;

        @Element
        public Three three1;

        @Element("nodeWithSubnodes")
        public Four four;
    }

    public static class Three {

        @Attribute("val")
        public int attr;

        @Text
        public String textField;
    }

    public static class Four {

        @Element("integer")
        public int i;

        @Element("bool")
        public boolean b;

        @Element("char")
        public char c;

        @Element("float")
        public float f;

    }

    public static class Empty {
        @Attribute
        public double attrEmpty;
    }
}
