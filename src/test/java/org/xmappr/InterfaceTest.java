package org.xmappr;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.StringReader;

public class InterfaceTest {

    private static String xml = "" +
            "<products>" +
            "<apple>an apple</apple>" +
            "<orange>an orange</orange>" +
            "</products> ";

    @Test
    public void mainTest() {

        StringReader reader = new StringReader(xml);

        Xmappr xmappr = new Xmappr(Products.class);

        Products child = (Products) xmappr.fromXML(reader);
        Assert.assertEquals(child.apple.getText(), "an apple");
        Assert.assertEquals(child.orange.getText(), "an orange");
    }

    @RootElement
    public static class Products {

        @Element(targetType = Apple.class)
        public Fruit apple;

        @Element(targetType = Orange.class)
        public Fruit orange;
    }


    public static interface Fruit {

        @Text
        public void setText(String text);

        @Text
        public String getText();
    }

    public static class Apple extends BaseFruit implements Fruit {
        public String hiddenText;


        public void setText(String text) {
            hiddenText = text;
        }

        public String getText() {
            return hiddenText;
        }
    }


    public static class Orange implements Fruit {
        public String hiddenText;

        public void setText(String text) {
            hiddenText = text;
        }

        public String getText() {
            return hiddenText;
        }
    }

    private static class BaseFruit {
        public String one;
    }
}
