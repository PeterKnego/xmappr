package org.xlite;

import org.xlite.SubTreeStore;

import javax.xml.stream.*;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.events.Characters;
import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * User: peter
 * Date: Feb 21, 2008
 * Time: 3:14:45 PM
 */
public class StaxEventReaderTest {


    public static void main(String[] args) {

        SubTreeStore rootSubTree = null;
        int count = 0;
        SubTreeStore currentSubTree = null;
        for (int i = 0; i < 5; i++) {

            long start = System.currentTimeMillis();
            count = 0;
//            currentSubTree = new SubTreeStore();
            rootSubTree = currentSubTree;
            try {
                String filename = "/home/peter/vmware/shared/Office Open XML Part 4 - Markup Language Reference/word/document.xml";
                FileReader reader = new FileReader(filename);
                XMLInputFactory factory = XMLInputFactory.newInstance();
                XMLEventReader parser = factory.createXMLEventReader(reader);

                XMLEvent event;
                while (parser.hasNext()) {

                    event = parser.nextEvent();
                    switch (event.getEventType()) {
                        case XMLStreamConstants.START_ELEMENT:
//                            SubTreeStore newSubTree = new SubTreeStore();
//                            Location location = event.getLocation();
                            StartElement sevent = event.asStartElement();
                            String lname = sevent.getName().getLocalPart();
                            String pname = sevent.getName().getPrefix();
                            String qName = pname + ":" + lname;
//                            newSubTree.name = qName.toCharArray();
//                            Iterator iter = sevent.getAttributes();
//                            while (iter.hasNext()) {
//                                Attribute attr = (Attribute) iter.next();
//                                newSubTree.attributes.add(new SubTreeStore.Attrib(attr.getName().getLocalPart().toCharArray(), attr.getValue().toCharArray()));
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
                            Characters cevent = event.asCharacters();
//                            currentSubTree.value = cevent.getData().toCharArray();
                            break;
                        case XMLStreamConstants.CDATA:

                            break;
                    } // end switch

                }
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

//        System.out.println("end "+ rootSubTree.name);
    }


}
