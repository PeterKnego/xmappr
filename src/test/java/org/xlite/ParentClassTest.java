/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
package org.xlite;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.StringReader;

public class ParentClassTest {

    private static String xml = "" +
            "<base i=\"1000\" l=\"9999\" >" +
            "A text value" +
            "<iv>999</iv>" +
            "  <bytv>-127</bytv>" +
            "  <dbv>1.6</dbv>" +
            "</base> ";

    @Test
    public void mainTest() {

        StringReader reader = new StringReader(ParentClassTest.xml);
        Xlite xlite = new Xlite(Child.class);

        Child child = (Child) xlite.fromXML(reader);
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


        public Double getDbv() {
            return dbv;
        }

        public void setDbv(Double dbv) {
            this.dbv = dbv;
        }

        @Element
        private Double dbv;
    }

}
