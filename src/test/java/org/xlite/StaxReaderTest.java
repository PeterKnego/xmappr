package org.xlite;

/**
 * User: peter
 * Date: Feb 20, 2008
 * Time: 10:33:01 PM
 */

import org.xlite.SubTreeStore;

import javax.xml.stream.*;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

public class StaxReaderTest {


    public static void main(String[] args) {

        Map<String, String> elements = null;
        SubTreeStore rootSubTree = null;
        SubTreeStore currentSubTree = null;
        int count = 0;
        for (int i = 0; i < 5; i++) {

            long start = System.currentTimeMillis();
//            currentSubTree = new SubTreeStore();
            rootSubTree = currentSubTree;
            count = 0;
            elements = new HashMap<String, String>();
            try {
                String filename = "/home/peter/vmware/shared/Office Open XML Part 4 - Markup Language Reference/word/document.xml";
                FileReader reader = new FileReader(filename);
                XMLInputFactory factory = XMLInputFactory.newInstance();
                XMLStreamReader parser = factory.createXMLStreamReader(reader);

                for (int event = parser.next(); event != XMLStreamConstants.END_DOCUMENT; event = parser.next()) {
                    switch (event) {
                        case XMLStreamConstants.START_ELEMENT:
//                            SubTreeStore newSubTree = new SubTreeStore();
                            String lname = parser.getName().getLocalPart();
                            String pname = parser.getName().getPrefix();
                            String qName = pname + ":" + lname;
//                            newSubTree.name = qName.toCharArray();
//                            for (int j = 0; j < parser.getAttributeCount(); j++) {
//                                newSubTree.attributes.add(new SubTreeStore.Attrib(parser.getAttributeLocalName(j).toCharArray(),
//                                        parser.getAttributeValue(j).toCharArray()));
//                            }
//                            currentSubTree.subelements.add(newSubTree);
//                            newSubTree.parent = currentSubTree;
//                            currentSubTree = newSubTree;
                            if(qName.equals("w:r")){
                            count++;
                            }
                            break;
                        case XMLStreamConstants.END_ELEMENT:

                            break;
                        case XMLStreamConstants.CHARACTERS:
//                            currentSubTree.value = parser.getText().toCharArray();
                            break;
                        case XMLStreamConstants.CDATA:

                            break;
                    } // end switch
                } // end while
                parser.close();
            } catch (FactoryConfigurationError ex) {
                System.out.println(ex.getMessage());
            } catch (FileNotFoundException ex) {
                System.out.println(ex.getMessage());
            } catch (XMLStreamException ex) {
                System.out.println(ex.getMessage());
            }

            System.out.println("dur: " + (System.currentTimeMillis() - start));
            System.out.println("count: "+count);
        }

        System.out.println("Count: " + count);
        System.out.println("Elements: " + elements.size());
        Set<String> elementos = elements.keySet();
        ArrayList<String> list = new ArrayList<String>(elementos);
        Collections.sort(list);
        for (String s : list) {
            System.out.println(s);
        }

    }


}
