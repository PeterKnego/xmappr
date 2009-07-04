/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
package org.xlite;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

public class BasicConvertersTest {

    private static String inXml = "" +
            "<primitives i=\"1000\" l=\"9999\" bool=\"false\" byt=\"127\" db=\"-1.6\" fl=\"1.1\" ch=\"f\" >" +
            "A text value" +
            "<iv>999</iv>" +
            "<lv>999999</lv>" +
            "<boolv/>" +
            "<boolNull/>" +
            "<bytv>-127</bytv>" +
            "<dbv>1.6</dbv>" +
            "<flv>-1.1</flv>" +
            "<chv>g</chv>" +
            "<node/>" +
            "<short/>" +
            "</primitives> ";

    private static String outXml = "" +
            "<primitives i=\"1000\" l=\"9999\" byt=\"127\" db=\"-1.6\" fl=\"1.1\" ch=\"f\" >" +
            "A text value" +
            "<iv>999</iv>" +
            "<lv>999999</lv>" +
            "<boolv/>" +
            "<bytv>-127</bytv>" +
            "<dbv>1.6</dbv>" +
            "<flv>-1.1</flv>" +
            "<chv>g</chv>" +
            "<node/>" +
            "<short/>" +
            "</primitives> ";


    @Test
    public void mainTest() throws IllegalAccessException, IOException, SAXException {
        StringReader reader = new StringReader(inXml);
        Configuration conf = new AnnotationConfiguration(Primitives.class, "primitives");
        Xlite xf = new Xlite(conf);

        Primitives primitives = (Primitives) xf.fromXML(reader);

        // attributes
        Assert.assertEquals(primitives.i, 1000);
        Assert.assertEquals(primitives.l, 9999);
        Assert.assertFalse(primitives.bool);
        Assert.assertEquals(primitives.byt, 127);
        Assert.assertEquals(primitives.db, -1.6d, 0.0d);
        Assert.assertEquals(primitives.fl, 1.1f, 0.0f);
        Assert.assertEquals(primitives.ch, 'f');

        // text
        Assert.assertEquals(primitives.value, "A text value");

        // subelements
        Assert.assertEquals(primitives.iv.intValue(), 999);
        Assert.assertEquals(primitives.lv, 999999);
        Assert.assertEquals(primitives.bytv, -127);
        Assert.assertEquals(primitives.dbv, 1.6d, 0.0d);
        Assert.assertEquals(primitives.flv, -1.1f, 0.0f);
        Assert.assertEquals(primitives.chv, 'g');

        // default values used
        Assert.assertTrue(primitives.boolv);
        Assert.assertTrue(primitives.boolNull);
        Assert.assertEquals(primitives.shortNode, 0);
        Assert.assertEquals(primitives.stringNode, "");

        // change some values
        // since bool is reference type (Boolean) it will be ommited from output
        primitives.bool = null;
        primitives.boolNull = null;

        // writing back to XML
        StringWriter sw = new StringWriter();
        xf.toXML(primitives, sw);
        String ssw = sw.toString();
        System.out.println("");
        System.out.println(inXml);
        System.out.println("");
        System.out.println(ssw);

        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(outXml, ssw);

    }

    public static class Primitives {

        // tests mapping element attribute to private field
        @XMLattribute
        private int i;

        public int getI() {
            return i;
        }

        public void setI(int i) {
            this.i = i;
        }

        @XMLattribute
        public long l;

        @XMLattribute
        public Boolean bool;

        @XMLattribute
        public byte byt;

        @XMLattribute
        public float fl;

        @XMLattribute
        public double db;

        @XMLattribute
        public char ch;

        // tests mapping element value to private field
        @XMLtext
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @XMLelement("node")
        public String stringNode = "";

        @XMLelement(name = "short", defaultValue = "0")
        public short shortNode;

        public Integer getIv() {
            return iv;
        }

        public void setIv(Integer iv) {
            this.iv = iv;
        }

        // tests mapping element to private field
        @XMLelement
        private Integer iv;

        @XMLelement
        public long lv;

        @XMLelement(defaultValue = "true")
        public boolean boolv;

        @XMLelement(defaultValue = "true")
        public Boolean boolNull;

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
