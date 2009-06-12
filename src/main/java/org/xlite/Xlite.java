package org.xlite;

import javax.xml.stream.*;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

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

        XMLStreamReader xmlreader = getXmlStreamReader(reader);
        XMLSimpleReader simpleReader = new XMLSimpleReader(xmlreader, false);

        return annotationConfiguration.getRootElementMapper().getRootObject(simpleReader);
    }

    public Result fromXMLwithUnknown(Reader reader) {
        annotationConfiguration.initialize();

        XMLStreamReader xmlreader = getXmlStreamReader(reader);
        XMLSimpleReader simpleReader = new XMLSimpleReader(xmlreader, true);

        Object object = annotationConfiguration.getRootElementMapper().getRootObject(simpleReader);
        return new Result(object, simpleReader.getObjectStore());
    }

    private XMLStreamReader getXmlStreamReader(Reader reader) {
        XMLStreamReader xmlreader;
        XMLInputFactory factory = XMLInputFactory.newInstance();
        try {
            xmlreader = factory.createXMLStreamReader(reader);
        } catch (XMLStreamException e) {
            throw new XliteException("Error initalizing XMLStreamReader", e);
        }
        return xmlreader;
    }

    public void toXML(Object source, Writer writer) {
        annotationConfiguration.initialize();

        XMLStreamWriter parser = getXmlStreamWriter(writer);
        XMLSimpleWriter simpleWriter = new XMLSimpleWriter(parser, new XmlStreamSettings(), annotationConfiguration.isPrettyPrint());

        annotationConfiguration.getRootElementMapper().toXML(source, simpleWriter);
    }

    private XMLStreamWriter getXmlStreamWriter(Writer writer) {
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        factory.setProperty("javax.xml.stream.isRepairingNamespaces", true);
        try {
            return factory.createXMLStreamWriter(writer);
        } catch (XMLStreamException e) {
            throw new XliteException("Error initalizing XMLStreamWriter", e);
        }
    }

    public void toXML(Object source, ObjectStore store, Writer writer) {
        annotationConfiguration.initialize();

        XMLStreamWriter parser = getXmlStreamWriter(writer);
        XMLSimpleWriter simpleWriter = new XMLSimpleWriter(parser, store, new XmlStreamSettings(), annotationConfiguration.isPrettyPrint());

        annotationConfiguration.getRootElementMapper().toXML(source, simpleWriter);
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
