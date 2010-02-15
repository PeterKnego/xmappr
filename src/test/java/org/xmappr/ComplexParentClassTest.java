package org.xmappr;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmappr.annotation.Element;
import org.xmappr.annotation.Elements;
import org.xmappr.annotation.RootElement;
import org.xmappr.annotation.Text;

import java.io.StringReader;

public class ComplexParentClassTest {

    private static String xmlOne = "" +
            "<root>" +
            "<one>onetext</one>" +
            "</root> ";

    private static String xmlTwo = "" +
            "<root>" +
            "<two>twotext" +
            "<inner>13</inner>" +
            "</two>" +
            "</root> ";

    @Test
    public void mainTest() {

        StringReader readerOne = new StringReader(xmlOne);
        StringReader readerTwo = new StringReader(xmlTwo);

        Xmappr xmappr = new Xmappr(Root.class);

        Root first = (Root) xmappr.fromXML(readerOne);
        Root second = (Root) xmappr.fromXML(readerTwo);

        // first XML produces instance of One class
        Assert.assertEquals(first.parent.getClass(), One.class);
        Assert.assertEquals(first.parent.text, "onetext");

        // second XML produces instance of Two class        
        Assert.assertEquals(second.parent.getClass(), Two.class);
        Assert.assertEquals(((Two)second.parent).inner, 13);
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
    public static class Root {

        @Elements({
                @Element(name = "one", targetType = One.class),
                @Element(name = "two", targetType = Two.class)
        })
        public Parent parent;
    }
}
