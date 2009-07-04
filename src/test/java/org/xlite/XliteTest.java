/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
package org.xlite;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xlite.Xlite;

import java.io.StringReader;

public class XliteTest {

    @Test
    public void basicTest() {

        StringReader reader = new StringReader(SampleXml.xml);
        Configuration conf = new AnnotationConfiguration(SampleXml.One.class, "one");
        Xlite xlite = new Xlite(conf);

        SampleXml.One one = (SampleXml.One) xlite.fromXML(reader);
        Assert.assertEquals(one.attr, "text1");
        Assert.assertEquals(one.attr2, 1111);
        Assert.assertEquals(one.attr3, 1.1f, 0.0f);
        Assert.assertEquals(one.text, "just a text");

        Assert.assertEquals(one.emptyNode.attrEmpty, -1.6d, 0.0d);

        Assert.assertTrue(one.two.attr4);
        Assert.assertEquals(one.two.character, 'x');
        Assert.assertEquals(one.two.text, "textTwo");

        Assert.assertEquals(one.two.three1.attr, 42);
        Assert.assertEquals(one.two.three1.textField, "textThree");

        Assert.assertEquals(one.two.four.i, 2008);
        Assert.assertTrue(one.two.four.b);
        Assert.assertEquals(one.two.four.c, 'f');
        Assert.assertEquals(one.two.four.f, -15.555f, 0.0f);


    }


}
