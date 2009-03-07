package org.xlite;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.testng.Assert;
import org.xml.sax.SAXException;
import org.xlite.XMLelement;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.IOException;
import java.util.List;

import org.xlite.Xlite;
import org.xlite.XMLtext;

/**
 * @author peter
 */
public class CollectionConverterTest {

    static String xml = "" +
            "<one>\n" +
            "just some text\n" +
            "<item>\n" +
            "first item text\n" +
            "<subitem>sub12<e></e></subitem>\n" +
            "<subitem>sub11</subitem>\n" +
            "<ignored>Ignored<subignored/><subignored2/><subignored3/></ignored>\n" +
            "</item>\n" +
            "<item>\n" +
            "second item text\n" +
            "   <subitem>sub21<ignored>IIIgnored</ignored></subitem>\n" +
//                      "<ignored>Ignored<subignored/><subignored2/><subignored3/></ignored>" +
            "   <subitem>sub22</subitem>\n" +
            "   <subitem>sub23</subitem>\n" +
            "</item>\n" +
            "<ignored>Ignored<subignored/><subignored2/><subignored3/></ignored>\n" +
            "</one>\n";

//    @org.testng.annotations.Test
    public void collectionConverterTest() throws IOException, SAXException {

        StringReader reader = new StringReader(xml);
        Configuration conf = new AnnotationConfiguration(One.class, "one");
        conf.setStoringUnknownElements(true);

        Xlite xlite = new Xlite(conf);
        One one = (One) xlite.fromXML(reader);

        Assert.assertEquals(one.text, "just some text"); // should be converted to upper case
        Assert.assertEquals(one.list.size(), 2);
        Assert.assertEquals(one.list.get(0).text, "first item text");
        Assert.assertEquals(one.list.get(1).text, "second item text");
        Assert.assertEquals(one.list.get(0).subs.size(), 2);
        Assert.assertEquals(one.list.get(1).subs.size(), 3);
        Assert.assertEquals(one.list.get(1).subs.get(0).text, "sub21");
        Assert.assertEquals(one.list.get(1).subs.get(1).text, "sub22");
        Assert.assertEquals(one.list.get(1).subs.get(2).text, "sub23");

        StringWriter writer = new StringWriter();
        xlite.toXML(one, writer);

        System.out.println(xml);
        System.out.println("\n");
        System.out.println(writer.toString());

        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(xml, writer.toString());
    }

    public static class One {

        @XMLtext
        public String text;

        @XMLelement(value = "item", itemType = Item.class)
        public List<Item> list;
    }

    public static class Item {

        @XMLtext
        public String text;

        @XMLelement(value = "subitem", itemType = SubItem.class)
        public List<SubItem> subs;
    }

    public static class SubItem {

        @XMLtext
        public String text;
    }
}
