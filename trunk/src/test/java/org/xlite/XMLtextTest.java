/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
package org.xlite;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class XMLtextTest {

    static final String xml1 = "<one>1</one>";


    @Test
    public void basicTest() {

        StringReader reader = new StringReader(xml1);
        Configuration conf = new AnnotationConfiguration(XMLTextTest.class, "one");
        Xlite xlite = new Xlite(conf);

        XMLTextTest one = (XMLTextTest) xlite.fromXML(reader);
        Assert.assertEquals((int) one.getValues().size(), 2);
        Assert.assertEquals((int) one.getValues().get(0), 0);
        Assert.assertEquals((int) one.getValues().get(1), 1);

        try {
            Writer w = new StringWriter();
            xlite.toXML(one, w);
            w.flush();
            w.close();

            System.out.println(w.toString());
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public static class XMLTextTest {

        @XMLtext(itemType = Integer.class)
        private List<Integer> values = new ArrayList<Integer>();


        public XMLTextTest() {
            values.add(0);
        }

        public List<Integer> getValues() {
            return values;
        }

        public void setValues(List<Integer> values) {
            this.values = values;
        }
    }

}