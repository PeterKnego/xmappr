package org.xlite;

import org.xlite.XMLSimpleReader;

import javax.xml.stream.*;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;

/**
 * User: peter
 * Date: Mar 3, 2008
 * Time: 10:41:52 PM
 */
public class Test {

    public static int i;
  
    public static void main(String[] args) throws IOException, XMLStreamException, NoSuchFieldException {

        Integer i = new Integer("");

    }

    @org.testng.annotations.Test
    public void test() throws XMLStreamException {
        StringWriter fw = new StringWriter();
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        factory.setProperty("javax.xml.stream.isRepairingNamespaces", true);
        XMLStreamWriter writer = factory.createXMLStreamWriter(fw);

//        writer.writeStartElement("pre1", "a", "ns1");
//        writer.writeNamespace("pre1", "ns1");
//        writer.writeNamespace("pre2", "ns2");
//        writer.writeCharacters("\n");
//
//        writer.writeStartElement("b", "ns2");
//        writer.writeCharacters("\n");
//        writer.writeStartElement("c");
//        writer.writeEndElement();
//        writer.writeCharacters("\n");
//        writer.writeEndElement();
//        writer.writeCharacters("\n");
//        writer.writeEndElement();

        writer.writeStartElement("pre", "a", "ns1");
        writer.writeEmptyElement("", "b", "");
        writer.writeAttribute("attr", "value");
        writer.writeEndElement();

//        writer.writeEmptyElement("pre1", "a", "ns1");
//        writer.flush();

        System.out.println(fw.toString());
    }

    @org.testng.annotations.Test
    public void testXmlSimple() throws XMLStreamException, FileNotFoundException {
        for (int i = 0; i < 1; i++) {
            String filename = "/home/peter/vmware/shared/Office Open XML Part 4 - Markup Language Reference/word/document.xml";
            FileReader sreader = new FileReader(filename);
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader parser = factory.createXMLStreamReader(sreader);
            XMLSimpleReader reader = new XMLSimpleReader(parser);

            long start = System.currentTimeMillis();
            reader.findFirstElement();
            recursiveRead(reader);

            System.out.println("duration: " + (System.currentTimeMillis() - start));
            System.out.println("count " + count);
            count = 0;
        }

    }

    public static int count = 0;

    public static void recursiveRead(XMLSimpleReader reader) {
        while (reader.moveDown()) {
            String lname = reader.getName().getLocalPart();
            String pname = reader.getName().getPrefix();
            String qName = pname + ":" + lname;
//            System.out.println(qName);
            if (qName.equals("w:r")) {
                count++;
            }
            recursiveRead(reader);
            reader.moveUp();
        }
    }
}
