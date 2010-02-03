package org.xmappr;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

public class ExistingObjectTest {

    private static String inXml = "" +
            "<primitives l=\"9999\" bool=\"false\" byt=\"127\" db=\"-1.6\" fl=\"1.1\" ch=\"f\" >" +
            "<iv>999</iv>" +
            "<lv>999999</lv>" +
            "<boolv/>" +
            "<boolNull/>" +
            "<bytv>-127</bytv>" +
            "<dbv>1.6</dbv>" +
            "<chv>g</chv>" +
            "<node/>" +
            "<short/>" +
            "</primitives> ";

    private static String outXml = "" +
            "<primitives in=\"1000\" l=\"9999\" byt=\"127\" db=\"-1.6\" fl=\"1.1\" ch=\"f\" >" +
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
        Xmappr xmappr = new Xmappr(Primitives.class);
        xmappr.setPrettyPrint(true);

        // create an existing object with some data
        Primitives primitives = new Primitives();
        primitives.in = 1000;
        primitives.value = "A text value";
        primitives.flv = -1.1f;

        xmappr.fromXML(reader, primitives);
        asserts(xmappr, primitives);
    }

    @Test
    public void mainTestViaXML() throws IllegalAccessException, IOException, SAXException {
        StringReader reader = new StringReader(inXml);

        // Double step to make Xmappr work harder (not necessary normally - do not copy)
        // Reads Class configuration, produces XML configuration from it and then feeds it to Xmappr
        StringReader configuration = XmlConfigTester.reader(Primitives.class);
        Xmappr xmappr = new Xmappr(configuration);
        xmappr.setPrettyPrint(true);

        // create an existing object with some data
        Primitives primitives = new Primitives();
        primitives.in = 1000;
        primitives.value = "A text value";
        primitives.flv = -1.1f;

        xmappr.fromXML(reader, primitives);
        asserts(xmappr, primitives);
    }

    private void asserts(Xmappr xmappr, Primitives primitives) throws SAXException, IOException {
        // attributes
        Assert.assertEquals(primitives.in, 1000);
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
        xmappr.toXML(primitives, sw);

        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(outXml, sw.toString());
    }

    @RootElement("primitives")
    public static class Primitives {

        private int in;

        @Attribute
        public int getIn() {
            return in;
        }

        @Attribute
        public void setIn(int in) {
            this.in = in;
        }

        @Attribute
        public long l;

        @Attribute
        public Boolean bool;

        @Attribute
        public byte byt;

        @Attribute
        public float fl;

        @Attribute
        public double db;

        @Attribute
        public char ch;

        private String value;

        @Text
        public String getValue() {
            return value;
        }

        @Text
        public void setValue(String value) {
            this.value = value;
        }

        @Element("node")
        public String stringNode = "";

        @Element(name = "short", defaultValue = "0")
        public short shortNode;

        @Element
        public Integer getIv() {
            return iv;
        }

        @Element
        public void setIv(Integer iv) {
            this.iv = iv;
        }

        private Integer iv;

        @Element
        public long lv;

        @Element(defaultValue = "true")
        public boolean boolv;

        @Element(defaultValue = "true")
        public Boolean boolNull;

        @Element
        public byte bytv;

        @Element
        public float flv;

        @Element
        public double dbv;

        @Element
        public char chv;

    }
}
