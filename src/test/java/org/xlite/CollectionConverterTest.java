package org.xlite;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.testng.Assert;
import org.testng.annotations.ExpectedExceptions;
import org.xml.sax.SAXException;
import org.xlite.XMLelement;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.IOException;
import java.util.List;
import java.util.HashMap;
import java.util.Collection;
import java.util.ArrayList;

import org.xlite.Xlite;
import org.xlite.XMLtext;
import org.xlite.converters.CollectionConverting;
import org.xlite.converters.CollectionConverter;
import org.xlite.converters.ElementConverter;

import javax.xml.namespace.QName;
import javax.xml.stream.*;

/**
 * @author peter
 */
public class CollectionConverterTest {

    private static String xml = "" +
            "<one>" +
            "just some text" +
            "<item>" +
            "first item text" +
            "<subitem>sub12</subitem>\n" +
            "<subitem>sub11</subitem>\n" +
            "</item>\n" +
            "<item>" +
            "second item text" +
            "<subitem>sub21</subitem>\n" +
            "<subitem>sub22</subitem>\n" +
            "<subitem>sub23</subitem>\n" +
            "</item>\n" +
            "</one>\n";

    @org.testng.annotations.Test
    public void collectionConverterTest() throws IOException, SAXException {

        StringReader reader = new StringReader(xml);
        Configuration conf = new AnnotationConfiguration(One.class, "one");

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

    @org.testng.annotations.Test
    @ExpectedExceptions(XliteException.class)
    public void toElementTest() throws XMLStreamException {
        ElementConverter cc = new CollectionConverter();
        XMLOutputFactory of = XMLOutputFactory.newInstance();
        XMLStreamWriter xsw = of.createXMLStreamWriter(new StringWriter());
        XMLSimpleWriter sw = new XMLSimpleWriter(xsw, null, false);
        cc.toElement(new Object(), new QName("something"), sw, new MappingContext(null, null), "", null);
    }

    @org.testng.annotations.Test
    @ExpectedExceptions(XliteException.class)
    public void fromElementTest() throws XMLStreamException {
        ElementConverter cc = new CollectionConverter();
        XMLInputFactory of = XMLInputFactory.newInstance();
        XMLStreamReader xsr = of.createXMLStreamReader(new StringReader(""));
        XMLSimpleReader sr = new XMLSimpleReader(xsr, false);
        cc.fromElement(sr, new MappingContext(null, null), "", null);

    }

    @org.testng.annotations.Test
    @ExpectedExceptions(XliteException.class)
    public void initializeNonCollectionTest() throws XMLStreamException {
        CollectionConverting cc = new CollectionConverter();

        // HashMap is not a Collection
        cc.initializeCollection(HashMap.class);
    }

    @org.testng.annotations.Test
    @ExpectedExceptions(XliteException.class)
    public void noninitializableCollectionTest() throws XMLStreamException {
        CollectionConverting cc = new CollectionConverter();

        // CustomCollection can not be instantiated via default constructor
        cc.initializeCollection(CustomCollection.class);
    }

    // Collection that can not be initialized via default constructor
    public static class CustomCollection extends ArrayList {

        private CustomCollection() {
        }
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
