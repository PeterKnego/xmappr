/*
 * This software is released under the BSD license. Full license available at http://xmappr.googlecode.com
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xmappr contributors
 * All rights reserved.
 */
package org.xmappr;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.StringReader;

public class ParentClassTest {

    private static String xml = "" +
            "<base i=\"1000\" l=\"9999\" >" +
            "A text value" +
            "<iv>999</iv>" +
            "<bytv>-127</bytv>" +
            "<dbv>1.6</dbv>" +
            "</base> ";

    @Test
    public void mainTest() {
        StringReader reader = new StringReader(ParentClassTest.xml);
        Xmappr xmappr = new Xmappr(Child.class);

        Child child = (Child) xmappr.fromXML(reader);
        asserts(child);
    }

    @Test
    public void mainTestViaXML() {
        StringReader reader = new StringReader(ParentClassTest.xml);

        // Double step to make Xmappr work harder (not necessary normally - do not copy)
        // Reads Class configuration, produces XML configuration from it and then feeds it to Xmappr
        StringReader configuration = XmlConfigTester.reader(Child.class);
        Xmappr xmappr = new Xmappr(configuration);

        Child child = (Child) xmappr.fromXML(reader);
        asserts(child);
    }

    private void asserts(Child child) {
        Assert.assertEquals(child.val, "A text value");
        Assert.assertEquals(child.i, 1000);
        Assert.assertEquals(child.l, 9999);
        Assert.assertEquals(child.bytv, -127);
        Assert.assertEquals(child.dbv, 1.6);
    }

    public static class Parent {

        @Text
        public String val;

        @Attribute
        public int i;

    }

    @RootElement("base")
    public static class Child extends Parent {
        @Attribute
        public long l;

        @Element
        public byte bytv;


        @Element
        public Double getDbv() {
            return dbv;
        }

        @Element
        public void setDbv(Double dbv) {
            this.dbv = dbv;
        }

        private Double dbv;
    }


}
