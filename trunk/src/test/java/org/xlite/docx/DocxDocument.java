package org.xlite.docx;

import org.xml.sax.SAXException;
import org.xlite.Xlite;
import org.xlite.XMLelement;

import java.io.FileReader;
import java.io.IOException;
import java.io.FileWriter;

/**
 * User: peter
 * Date: Dec 27, 2007
 * Time: 2:52:22 PM
 */

public class DocxDocument {

    private static String mainPartName = "word/document.xml";
    private static String settingsPartName = "word/settings.xml";

    @XMLelement("w:body")
    public Body body;

    public static void main(String[] args) throws IOException, SAXException {

        for (int i = 0; i < 1; i++) {

            DocxDocument document = null;
            Xlite xlite = new Xlite(DocxDocument.class, "document", "http://schemas.openxmlformats.org/wordprocessingml/2006/main");
            xlite.setStoringUnknownElements(true);
            xlite.addNamespace("w=http://schemas.openxmlformats.org/wordprocessingml/2006/main");

            String i4 = "/home/peter/vmware/shared/Office Open XML Part 4 - Markup Language Reference/word/document.xml";
            String i3 = "/home/peter/vmware/shared/Office Open XML Part 3 - Primer/word/document_pp.xml";
            String i2 = "/home/peter/vmware/shared/test2/word/document_pp.xml";
            String i1 = "/home/peter/vmware/shared/test1/word/document-pp.xml";

            FileReader reader = new FileReader(i1);

            long start = System.currentTimeMillis();
            document = (DocxDocument) xlite.fromXML(reader);

            System.out.println("duration read: " + (System.currentTimeMillis() - start));
            System.out.println("store size: "+xlite.getNodeStore().getStoreSize());
            start = System.currentTimeMillis();

            String tmpfile = "/home/peter/tmp/out.xml";
            FileWriter fw = new FileWriter(tmpfile);
            xlite.toXML(document, fw);
            fw.flush();
            fw.close();

            System.out.println("duration write: " + (System.currentTimeMillis() - start));

            FileReader fr1 = new FileReader(i1);
            FileReader fr2 = new FileReader(tmpfile);

//            XMLUnit.setIgnoreWhitespace(true);
//            XMLAssert.assertXMLEqual(fr1, fr2);
        }

////        FileWriter writer = new FileWriter("/home/peter/doc.html", false);
//        for (Paragraph paragraph : document.body.paragraphs) {
//            if (paragraph.runs != null) {
////                writer.append("\n");
//                System.out.println("\n");
//                for (Run run : paragraph.runs) {
//                    if (run.textnode != null) {
////                        writer.append(run.textnode.text);
//                        System.out.print(run.textnode.text);
//                    } else {
//                        System.out.print("|");
//                    }
//                }
//            }
//        }

    }

}
