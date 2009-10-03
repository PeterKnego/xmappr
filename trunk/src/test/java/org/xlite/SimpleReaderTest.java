/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
package org.xlite;

import org.testng.Assert;
import org.testng.annotations.Test;

import javax.xml.namespace.QName;
import javax.xml.stream.*;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;

public class SimpleReaderTest {

    private XMLSimpleReader getReader(String xmlString) throws XMLStreamException {
        StringReader sreader = new StringReader(xmlString);
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader parser = factory.createXMLStreamReader(sreader);  // todo make this a part of SimpleReader.newInstance()
        return new XMLSimpleReader(parser);
    }

    private XMLSimpleWriter getWriter(Writer writer) throws XMLStreamException {
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        factory.setProperty("javax.xml.stream.isRepairingNamespaces", true);
        XMLStreamWriter parser = factory.createXMLStreamWriter(writer);
        return new XMLSimpleWriter(parser, new XmlStreamSettings(), true);
    }

    private static String xml1 = "<a><b><c><d attr=\"DDD\" /></c></b></a>";

    @Test
    public void emptyElementWithAttributeTest() throws XMLStreamException {
        XMLSimpleReader reader = getReader(xml1);

        reader.getRootName();
        Element rootElement = processSubElements(reader).get(0);
//        printElements(rootElement, "");
        Assert.assertEquals(rootElement.name.getLocalPart(), "a");
        Assert.assertEquals(rootElement.subelements.get(0).name.getLocalPart(), "b");
        Assert.assertEquals(rootElement.subelements.get(0).subelements.get(0).name.getLocalPart(), "c");
        Assert.assertEquals(rootElement.subelements.get(0).subelements.get(0).subelements.get(0).name.getLocalPart(), "d");


    }

    private static String xml2 = "<a><b/><c/><d></d></a>";

    @Test
    public void simpleTest() throws XMLStreamException {
        XMLSimpleReader reader = getReader(xml2);
        reader.getRootName();
        Element rootElement = processSubElements(reader).get(0);
//        printElements(rootElement, "");
        Assert.assertEquals(rootElement.name.getLocalPart(), "a");
        Assert.assertEquals(rootElement.subelements.get(0).name.getLocalPart(), "b");  // first subnode of <a>
        Assert.assertEquals(rootElement.subelements.get(1).name.getLocalPart(), "c");
        Assert.assertEquals(rootElement.subelements.get(2).name.getLocalPart(), "d");
    }

    private static String xml3 = "<a>1<b>2</b>3</a>";

    @Test
    public void textTest() throws XMLStreamException {
        XMLSimpleReader reader = getReader(xml3);
        QName a = reader.getRootName();
        reader.moveDown();
        Assert.assertEquals(reader.getName().getLocalPart(), "a");  // inside <a>
        Assert.assertEquals(reader.getText(), "1");
        reader.moveDown();
        Assert.assertEquals(reader.getName().getLocalPart(), "b"); // inside <b>
        Assert.assertEquals(reader.getText(), "2");         ;
        Assert.assertTrue(!reader.moveDown()); // there are no child nodes under <b>
        reader.moveUp();
        Assert.assertEquals(reader.getName().getLocalPart(), "a");  // back to <a>
//        Assert.assertEquals(reader.getText(), "3");
        reader.moveUp();
    }

    private static String xml4 = "<a><b><c></c><d></d></b></a>";

    @Test
    public void skippingChildElementsTest() throws XMLStreamException {
        XMLSimpleReader reader = getReader(xml4);
        reader.getRootName();
        reader.moveDown();
        Assert.assertEquals(reader.getName().getLocalPart(), "a");  // inside <a>
        reader.moveDown();  // down two times
        reader.moveDown();
        Assert.assertEquals(reader.getName().getLocalPart(), "c");  // inside child <c>
        reader.moveUp();  //moving up twice should position reader back into <a>
        reader.moveUp();
        Assert.assertEquals(reader.getName().getLocalPart(), "a");  // back inside <a>

    }

    //todo FINISH this test!!!   This test should ignore "other" nodes - CDATA, comments, DTD, Entity Reference, Processing Instruction
    static String xml5 = "";

    @Test
    public void ignoringOtherElementsTest() throws XMLStreamException {
        XMLSimpleReader reader = getReader(xml4);
        reader.getRootName();
        reader.moveDown();

    }

    private static String xml6 = "<a><b1>B1</b1><c><d><e/></d></c><b2>B2</b2></a>";

    @Test
    public void skippedElementsTest() throws XMLStreamException {
        XMLSimpleReader reader = getReader(xml6);

        // inside a
        reader.getRootName();
        reader.moveDown();
        Assert.assertEquals(reader.getName().getLocalPart(), "a");

        // inside b1
        Assert.assertTrue(reader.moveDown());
        Assert.assertEquals(reader.getName().getLocalPart(), "b1");
        Assert.assertEquals(reader.getText(), "B1");
        reader.moveUp();

        // inside c
        Assert.assertTrue(reader.moveDown());
        Assert.assertEquals(reader.getName().getLocalPart(), "c");
        // skip d and e entirelly
        reader.moveUp();

        // inside b2
        Assert.assertTrue(reader.moveDown());
        Assert.assertEquals(reader.getName().getLocalPart(), "b2");
        Assert.assertEquals(reader.getText(), "B2");
        reader.moveUp();

        // back to a
        Assert.assertEquals(reader.getName().getLocalPart(), "a");
        reader.moveUp();


    }

    @Test
    public void writerTest() throws XMLStreamException {
        StringWriter sw = new StringWriter();
        XMLSimpleWriter writer = getWriter(sw);
        writer.startElement(new QName("root"));
        writer.addAttribute(new QName("a"), "abc");
        writer.addAttribute(new QName("b"), "123");
        writer.addAttribute(new QName("c"), "mama");
//        writer.addText("text");
        writer.endElement();
        writer.endDocument();

        System.out.println("writerTest: " + sw.toString());
    }

    @Test
    public void writer() throws XMLStreamException {
        StringWriter sw = new StringWriter();
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        factory.setProperty("javax.xml.stream.isRepairingNamespaces", true);
        XMLStreamWriter writer = factory.createXMLStreamWriter(sw);

        writer.writeEmptyElement("root");
        writer.writeAttribute("a", "123");
        writer.writeAttribute("b", "456");
        writer.writeEndDocument();
        writer.flush();
        writer.close();
        System.out.println(sw.toString());
    }


    private static List<Element> processSubElements(XMLSimpleReader reader) {
        List<Element> elements = new ArrayList<Element>();
        while (reader.moveDown()) {
            Element element = new Element();
            elements.add(element);
            element.name = reader.getName();
            Iterator<Map.Entry<QName, String>> attrIterator = reader.getAttributeIterator();
            while (attrIterator.hasNext()) {
                Map.Entry<QName, String> entry = attrIterator.next();
                element.attributes.put(entry.getKey(), entry.getValue());
            }
//            System.out.println("NODE-"+element.name.getLocalPart());
//            System.out.println("text1: "+reader.getText());
            List<Element> subElements = processSubElements(reader);
            if (!subElements.isEmpty()) {
                element.subelements.addAll(subElements);
            }
            element.value = reader.getText();
//            System.out.println("text2: "+reader.getText());
            reader.moveUp();
        }
        return elements;
    }

    public static class Element {
        public QName name;
        public Map<QName, String> attributes = new HashMap<QName, String>();
        public String value;
        List<Element> subelements = new ArrayList<Element>();
    }

    private static void printElements(Element element, String prefix) {
        System.out.print(prefix + "<" + element.name.getLocalPart());
        for (QName qName : element.attributes.keySet()) {
            System.out.print(" " + qName + "=\"" + element.attributes.get(qName) + "\"");
        }
        System.out.println(">");
        if (element.value != null && element.value.length() != 0) {
            System.out.println(prefix + element.value);
        }
        for (Element subnode : element.subelements) {
            printElements(subnode, prefix + "  ");
        }
        System.out.println(prefix + "</" + element.name.getLocalPart() + ">");

    }

}
