package org.xlite;

import javax.xml.stream.*;
import java.io.Reader;
import java.io.Writer;


/**
 * User: peter
 * Date: Feb 25, 2008
 * Time: 11:48:02 AM
 */
public class Xlite {

    private Configuration annotationConfiguration;

    public Xlite(Configuration annotationConfiguration) {
        this.annotationConfiguration = annotationConfiguration;
    }


    public Object fromXML(Reader reader) {
        annotationConfiguration.initialize();

        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader xmlreader;
        try {
            xmlreader = factory.createXMLStreamReader(reader);
        } catch (XMLStreamException e) {
            throw new XliteException("Error initalizing XMLStreamReader", e);
        }
        XMLSimpleReader simpleReader = new XMLSimpleReader(xmlreader, annotationConfiguration.isStoringUnknownElements());

        return annotationConfiguration.getRootElementMapper().getRootObject(simpleReader);
    }

    public void toXML(Object source, Writer writer) {
        annotationConfiguration.initialize();

        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        factory.setProperty("javax.xml.stream.isRepairingNamespaces", true);
        XMLStreamWriter parser;
        try {
            parser = factory.createXMLStreamWriter(writer);
        } catch (XMLStreamException e) {
            throw new XliteException("Error initalizing XMLStreamWriter", e);
        }
        XMLSimpleWriter simpleWriter = new XMLSimpleWriter(parser, new XmlStreamSettings(), annotationConfiguration.isPrettyPrint());

        annotationConfiguration.getRootElementMapper().toXML(source, simpleWriter);
    }

    public SubTreeStore getNodeStore() {
        return annotationConfiguration.getNodeStore();
    }
}
