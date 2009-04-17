package org.xlite;

import org.testng.annotations.Test;
import org.testng.Assert;

import java.io.StringReader;
import java.lang.reflect.Constructor;

/**
 * Created by IntelliJ IDEA.
 * User: peter
 * Date: Mar 30, 2009
 * Time: 10:24:33 AM
 */
public class ParentClassTest {

    public static String xml = "" +
            "<base i=\"1000\" l=\"9999\" >" +
            "A text value" +
            "<iv>999</iv>" +
            "  <bytv>-127</bytv>" +
            "  <dbv>1.6</dbv>" +
            "</base> ";

    @Test
    public void mainTest() {

        StringReader reader = new StringReader(ParentClassTest.xml);
        Configuration conf = new AnnotationConfiguration(Child.class, "base");
        Xlite xlite = new Xlite(conf);

        Child child = (Child) xlite.fromXML(reader);
        Assert.assertEquals(child.val, "A text value");
        Assert.assertEquals(child.i, 1000);
        Assert.assertEquals(child.l, 9999);
        Assert.assertEquals(child.bytv, -127);
        Assert.assertEquals(child.dbv, 1.6);
    }

    public static class Parent {

        @XMLtext
        public String val;

        @XMLattribute
        public int i;

    }

    public static class Child extends Parent{
        @XMLattribute
        public long l;

        @XMLelement
        public byte bytv;


        public Double getDbv() {
            return dbv;
        }

        public void setDbv(Double dbv) {
            this.dbv = dbv;
        }

        @XMLelement
        private Double dbv;
    }

}
