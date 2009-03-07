package org.xlite;

import org.xlite.XMLattribute;
import org.xlite.XMLelement;
import org.xlite.XMLtext;

/**
 * @author peter
 */
public class SampleXml {

    static String xml =
            "<one attr1=\"text1\" attr2=\"1111\" attr3=\"1.1\">" +
                    "just a<!-- comment should be ignored --> text" +
                    "<ignored>Ignored<subignored/></ignored>" +
                    "<emptyNode attrEmpty=\"-1.6\"/>" +
                    "<two attr4=\"true\" attr5=\"x\" >" +
                    "textTwo" +
                    "   <three1 val=\"42\">" +
                    "   textThree" +
                    "   </three1>" +
                    "   <nodeWithSubnodes>" +
                    "       <integer>2008</integer>" +
                    "       <bool>true</bool>" +
                    "       <char>f</char>" +
                    "       <float>-15.555</float>" +
                    "   </nodeWithSubnodes>" +
                    "</two>" +
                    "</one>";


    public static class One {

        @XMLattribute("attr1")
        public String attr;

        @XMLattribute
        public int attr2;

        @XMLattribute
        public float attr3;

        @XMLtext
        public String text;

        @XMLelement
        public Empty emptyNode;

        @XMLelement
        public Two two;

    }

    public static class Two {

        @XMLattribute
        public boolean attr4;

        @XMLattribute("attr5")
        public char character;

        @XMLtext
        public String text;

        @XMLelement
        public Three three1;

        @XMLelement("nodeWithSubnodes")
        public Four four;
    }

    public static class Three {

        @XMLattribute("val")
        public int attr;

        @XMLtext
        public String textField;
    }

    public static class Four {

        @XMLelement("integer")
        public int i;

        @XMLelement("bool")
        public boolean b;

        @XMLelement("char")
        public char c;

        @XMLelement("float")
        public float f;

    }

    public static class Empty {
        @XMLattribute
        public double attrEmpty;
    }
}
