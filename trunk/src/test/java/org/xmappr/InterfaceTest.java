package org.xmappr;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.StringReader;
import java.lang.reflect.Field;

public class InterfaceTest {

    private static String xml = "" +
            "<products>" +
            "<apple>an apple</apple>" +
            "<orange>an orange</orange>" +
            "</fruits> ";

    @Test
    public void mainTest() {

        Field[] fields = Apple.class.getDeclaredFields();
        for (Field field : fields) {
            System.out.println(field.getName()+":"+field.getType().getName());
        }

        StringReader reader = new StringReader(xml);

        Xmappr xmappr = new Xmappr(Products.class);

        Products child = (Products) xmappr.fromXML(reader);
        Assert.assertEquals(child.apple.hiddenText, "an apple");
        Assert.assertEquals(child.orange.hiddenText, "an orange");
    }

    @RootElement
    public static class Products {

        @Element(targetType = Apple.class)
        private Fruit apple;

        @Element(targetType = Orange.class)
        private Fruit orange;
    }


    public static interface Fruit {
        String hiddenText = null;
        String two = null;
    }

    public static class Apple extends BaseFruit implements Fruit {
        public String hiddenText;


    }



     public static class Orange implements Fruit {
        public String hiddenText;


    }

    private static class BaseFruit {
        public String one;
    }
}
