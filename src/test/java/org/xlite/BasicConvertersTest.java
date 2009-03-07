package org.xlite;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.StringReader;
import java.lang.reflect.Field;

import org.xlite.Xlite;
import org.xlite.XMLelement;
import org.xlite.XMLattribute;
import org.xlite.XMLtext;

/**
 * @author peter
 */
public class BasicConvertersTest {

    static String xml = "" +
            "<primitives i=\"1000\" l=\"9999\" bool=\"true\" byt=\"127\" db=\"-1.6\" fl=\"1.1\" ch=\"f\" >" +
            "A text value" +
            "  <iv>999</iv>" +
            "  <lv>999999</lv>" +
            "  <boolv>false</boolv>" +
            "  <bytv>-127</bytv>" +
            "  <dbv>1.6</dbv>" +
            "  <flv>-1.1</flv>" +
            "  <chv>g</chv>" +
            "  <node/>" +
            "  <int/>" +
            "</primitives> ";


    @Test
    public void mainTest() throws IllegalAccessException {
        StringReader reader = new StringReader(xml);
        Configuration conf = new AnnotationConfiguration(Primitives.class, "primitives");
        conf.setStoringUnknownElements(true);
        Xlite xf = new Xlite(conf);

        Primitives primitives = (Primitives) xf.fromXML(reader);

//        for (Field field : Primitives.class.getDeclaredFields()) {
//            System.out.println(field.getName() + "=" + field.get(primitives));
//        }

        //attributes
        Assert.assertEquals(primitives.i, 1000);
        Assert.assertEquals(primitives.l, 9999);
        Assert.assertTrue(primitives.bool);
        Assert.assertEquals(primitives.byt, 127);
        Assert.assertEquals(primitives.db, -1.6d, 0.0d);
        Assert.assertEquals(primitives.fl, 1.1f, 0.0f);
        Assert.assertEquals(primitives.ch, 'f');

        //node text
        Assert.assertEquals(primitives.value, "A text value");

        // subelements
        Assert.assertEquals(primitives.iv, 999);
        Assert.assertEquals(primitives.lv, 999999);
        Assert.assertFalse(primitives.boolv);
        Assert.assertEquals(primitives.bytv, -127);
        Assert.assertEquals(primitives.dbv, 1.6d, 0.0d);
        Assert.assertEquals(primitives.flv, -1.1f, 0.0f);
        Assert.assertEquals(primitives.chv, 'g');

        // default values used
        Assert.assertEquals(primitives.intNode, 0);
        Assert.assertEquals(primitives.stringNode, null);

    }

    public static class Primitives {
        @XMLattribute
        public int i;

        @XMLattribute
        public long l;

        @XMLattribute
        public boolean bool;

        @XMLattribute
        public byte byt;

        @XMLattribute
        public float fl;

        @XMLattribute
        public double db;

        @XMLattribute
        public char ch;

        @XMLtext
        public String value;

        @XMLelement("node")
        public String stringNode;

        @XMLelement(name = "int", defaultValue = "0")
        public int intNode;

        @XMLelement
        public int iv;

        @XMLelement
        public long lv;

        @XMLelement
        public boolean boolv;

        @XMLelement
        public byte bytv;

        @XMLelement
        public float flv;

        @XMLelement
        public double dbv;

        @XMLelement
        public char chv;

    }
}
