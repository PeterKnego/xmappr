package org.xlite;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xlite.Xlite;

import java.io.StringReader;

/**
 * User: peter
 * Date: Feb 26, 2008
 * Time: 11:50:33 PM
 */
public class XliteTest {

    @Test
    public void basicTest() {

        StringReader reader = new StringReader(SampleXml.xml);
        Xlite xlite = new Xlite(SampleXml.One.class, "one");
        xlite.setStoringUnknownElements(true);

        SampleXml.One one = (SampleXml.One) xlite.fromXML(reader);
        Assert.assertEquals(one.attr, "text1");
        Assert.assertEquals(one.attr2, 1111);
        Assert.assertEquals(one.attr3, 1.1f, 0.0f);
        Assert.assertEquals(one.text, "just atext");

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
