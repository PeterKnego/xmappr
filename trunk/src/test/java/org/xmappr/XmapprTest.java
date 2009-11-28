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

public class XmapprTest {

    @Test
    public void basicTest() {

        StringReader reader = new StringReader(SampleXml.xml);

        // Double step to make Xmappr work harder (not necessary normally - do not copy)
        // Reads Class configuration, produces XML configuration from it and then feeds it to Xmappr
        StringReader configuration = XmlConfigTester.reader(SampleXml.One.class);
        Xmappr xmappr = new Xmappr(configuration);

        SampleXml.One one = (SampleXml.One) xmappr.fromXML(reader);
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
