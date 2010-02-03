package org.xmappr;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.StringReader;
import java.lang.reflect.Method;

public class InterfaceTest {

    private static String xml = "" +
            "<products>" +
            "<apple type='red'>an apple</apple>" +
            "<orange type='orange'>an orange</orange>" +
            "</products> ";

    @Test
    public void mainTest() {

        StringReader reader = new StringReader(xml);

        Xmappr xmappr = new Xmappr(Products.class);

        Products child = (Products) xmappr.fromXML(reader);
        Assert.assertEquals(child.apple.getText(), "an apple");
        Assert.assertEquals(child.apple.getType(), "red");

        Assert.assertEquals(child.orange.getText(), "an orange");
        Assert.assertEquals(child.orange.getType(), "orange");
    }

    public static void main(String[] args) {

        for (Method method : Fruit.class.getMethods()) {
            Element element = method.getAnnotation(Element.class);
            System.out.println(element);
        }

        Orange orange = new Orange();
        for (Method method : orange.getClass().getMethods()) {
            System.out.println("method:"+method.getName());
                System.out.println("annotation:"+ConfigurationProcessor.getTextAnnotation(method).annotationType());
        }


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

        @Attribute
        public void setType(String text);

        @Attribute
        public String getType();
    }

    public static class Apple extends BaseFruit {

    }


    public static class Orange implements Fruit {
        public String hiddenText;
        public String hiddenType;

        public void setText(String text) {
            hiddenText = text;
        }

        public String getText() {
            return hiddenText;
        }

         public void setType(String text) {
            this.hiddenType = text;
        }

        public String getType() {
            return hiddenType;
        }
    }

    private static class BaseFruit implements Fruit{
        public String hiddenText;
        public String hiddenType;


        public void setText(String text) {
            hiddenText = text;
        }

        public String getText() {
            return hiddenText;
        }

        public void setType(String text) {
            this.hiddenType = text;
        }

        public String getType() {
            return hiddenType;
        }
    }
}
