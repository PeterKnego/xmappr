package org.xmappr;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.StringReader;
import java.util.List;

public class ComplexParentClassTest {

    private static String xml = "" +
            "<base>" +
            "<one>onetext</one>" +
            "<two>twotext" +
            "<inner>2</inner>" +
            "</two>" +
            "</base> ";

    @Test
    public void mainTest() {

        StringReader reader = new StringReader(xml);

        Xmappr xmappr = new Xmappr(Base.class);

        Base base = (Base) xmappr.fromXML(reader);
       

    }

    public static class Parent {

        @Text
        public String text;

    }

    public static class One extends Parent {

    }

    public static class Two extends Parent {

        @Element
        public int inner;
    }

    @RootElement
    public static class Base {

        @Elements({
                @Element(name = "one", targetType = One.class),
                @Element(name = "two", targetType = Two.class)
        })
        public List parent;
    }
}
