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

    private Configuration configuration;

    private XMLInputFactory xmlInputFactory;
    private XMLOutputFactory xmlOutputFactory;

    public Xlite(Configuration configuration) {
        this.configuration = configuration;
    }

    public Object fromXML(Reader reader) {
        configuration.initialize();

        XMLSimpleReader simpleReader = new XMLSimpleReader(getXmlStreamReader(reader), false);

        return configuration.getRootElementMapper().getRootObject(simpleReader);
    }

    public Result fromXMLwithUnknown(Reader reader) {
        configuration.initialize();

        XMLSimpleReader simpleReader = new XMLSimpleReader(getXmlStreamReader(reader), true);

        Object object = configuration.getRootElementMapper().getRootObject(simpleReader);
        return new Result(object, simpleReader.getObjectStore());
    }

    private XMLStreamReader getXmlStreamReader(Reader reader) {
        XMLStreamReader xmlreader;
        try {
            xmlreader = getXmlInputFactory().createXMLStreamReader(reader);
        } catch (XMLStreamException e) {
            throw new XliteException("Error initalizing XMLStreamReader", e);
        }
        return xmlreader;
    }

    private synchronized XMLInputFactory getXmlInputFactory() {
        if (xmlInputFactory == null) {
            xmlInputFactory = XMLInputFactory.newInstance();
        }
        return xmlInputFactory;
    }

    public void toXML(Object source, Writer writer) {
        configuration.initialize();

        XMLStreamWriter parser = getXmlStreamWriter(writer);
        XMLSimpleWriter simpleWriter = new XMLSimpleWriter(parser, new XmlStreamSettings(), configuration.isPrettyPrint());

        configuration.getRootElementMapper().toXML(source, simpleWriter);
    }

    private XMLStreamWriter getXmlStreamWriter(Writer writer) {
        try {
            return getXmlOutputFactory().createXMLStreamWriter(writer);
        } catch (XMLStreamException e) {
            throw new XliteException("Error initalizing XMLStreamWriter", e);
        }
    }

    private synchronized XMLOutputFactory getXmlOutputFactory() {
        if (xmlOutputFactory == null) {
            xmlOutputFactory = XMLOutputFactory.newInstance();
            xmlOutputFactory.setProperty("javax.xml.stream.isRepairingNamespaces", true);
        }
        return xmlOutputFactory;
    }

    public void toXML(Object source, ObjectStore store, Writer writer) {
        configuration.initialize();

        XMLStreamWriter parser = getXmlStreamWriter(writer);
        XMLSimpleWriter simpleWriter = new XMLSimpleWriter(parser, store, new XmlStreamSettings(), configuration.isPrettyPrint());

        configuration.getRootElementMapper().toXML(source, simpleWriter);
    }

    public static class Result {
        ObjectStore store;
        Object object;

        public Result(Object object, ObjectStore store) {
            this.store = store;
            this.object = object;
        }

        public ObjectStore getStore() {
            return store;
        }

        public Object getObject() {
            return object;
        }

    }

}
