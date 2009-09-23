/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
package org.xlite;

import org.xlite.converters.RootMapper;

import javax.xml.stream.*;
import javax.xml.namespace.QName;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Method;

/**
 * A facade to Xlite library, through which all serialization/deserialization calls are made.
 * <p/>
 * <b>Example usage:</b>
 * <pre>
 * Configuration conf = new AnnotationConfiguration(One.class);  // mapping configuration for class One
 * Xlite xlite = new Xlite(conf);                                // initialize Xlite
 * One one = (One) xlite.fromXml(inputXml);                      // deserialize XML to object
 * </pre>
 * Deserialization (XML-to-object) is done via fromXML() and fromXMLwithUnknown() methods.
 * <p/>
 * Serialization (object-to-XML) is done via toXML() method.
 * <h3>Thread safety</h3>
 * Xlite instance is thread-safe after it is initialized with given configuration.
 * Afterwards no changes to configuration are allowed.
 * Being thread-safe means that Xlite can be called concurrently from multiple threads, allowing objects to be
 * serialized/deserialized concurrently.
 * <h3>Preserving unmapped XML elements</h3>
 * Xlite can be configured to internally store all XML subelements that are not mapped to concrete class fields.
 * This way only a small part of a larger XML tree may be mapped to Java object, while preserving the rest of
 * XML from input to output.
 * <p/>
 * <b>Example of storing unamapped elements:</b>
 * <pre>
 * Configuration conf = new AnnotationConfiguration(One.class);  // mapping configuration for class One
 * Xlite xlite = new Xlite(conf);                                // initialize Xlite
 * Xlite.Result result = xlite.fromXMLwithUnmapped(inputXml);    // deserialize XML to object while storing unmapped XML
 * <p/>
 * One one = result.getObject();                                 // get deserialized object from Result object
 * ObjectStore store = result.getObjectStore()                   // unmapped XML is stored in binary form to ObjectStore
 * <p/>
 * xlite.toXML(one, store, outputXmlWriter)                      // on serialization, stored objects must be provided
 * </pre>
 * <em>Note: only whole XML subelements are stored and not XML attributes or text elements.
 * XML attributes and text elements must be mapped explicitly via configuration to be preserved from input to output.</em>
 * </pre>
 */
public class Xlite {

    private final Configuration configuration;

    private final XMLInputFactory xmlInputFactory;
    private final XMLOutputFactory xmlOutputFactory;

    /**
     * Creates a new Xlite instance with given Configuration.
     *
     * @param configuration
     */
    public Xlite(Configuration configuration) {
        XMLInputFactory xmlInputFactory1;
        XMLOutputFactory xmlOutputFactory1;
        this.configuration = configuration;

        try {
            Class<?> clazz = Class.forName("com.ctc.wstx.stax.WstxInputFactory");
            Method newInstanceMethod = clazz.getMethod("newInstance");
            xmlInputFactory1 = (XMLInputFactory) newInstanceMethod.invoke(null);

            Class<?> clazz2 = Class.forName("com.ctc.wstx.stax.WstxOutputFactory");
            Method newInstanceMethod2 = clazz.getMethod("newInstance");
            xmlOutputFactory1 = (XMLOutputFactory) newInstanceMethod2.invoke(null);

        } catch (Exception e) {
            xmlInputFactory1 = XMLInputFactory.newInstance();
            xmlOutputFactory1 = XMLOutputFactory.newInstance();
        }

        this.xmlInputFactory = xmlInputFactory1;
        this.xmlOutputFactory = xmlOutputFactory1;
        this.xmlOutputFactory.setProperty("javax.xml.stream.isRepairingNamespaces", true);
        this.configuration.initialize();
    }

    /**
     * Reads XML data from provided Reader and returns a deserialized object.
     *
     * @param reader
     * @return A deserialized object.
     */
    public Object fromXML(Reader reader) {

        XMLStreamReader rdr = getXmlStreamReader(reader);
        XMLSimpleReader simpleReader = new XMLSimpleReader(rdr, false);

        return getRootMapper(simpleReader).getRootObject(simpleReader);
    }

    /**
     * Reads XML data from provided Reader and returns Xlite.Result.
     *
     * @param reader
     * @return An instance of Xlite.Reader, containing deserialized object and stored unmapped XML elements.
     */
    public Result fromXMLwithUnmapped(Reader reader) {

        XMLSimpleReader simpleReader = new XMLSimpleReader(getXmlStreamReader(reader), true);

        Object object = getRootMapper(simpleReader).getRootObject(simpleReader);
        return new Result(object, simpleReader.getObjectStore());
    }

    /**
     * Serializes source object to XML and writes it to Writer.
     *
     * @param source Object to be serialized.
     * @param writer Writer from which XML data is read.
     */
    public void toXML(Object source, Writer writer) {

        XMLStreamWriter parser = getXmlStreamWriter(writer);
        XMLSimpleWriter simpleWriter = new XMLSimpleWriter(parser, new XmlStreamSettings(), configuration.isPrettyPrint());

        getRootMapper(source.getClass()).toXML(source, simpleWriter);
    }

    /**
     * Serializes source object to XML and writes it to Writer.
     * During serialization process also writes stored XML elements so that unmapped XML may be preserved.
     *
     * @param source Object to be serialized.
     * @param store  ObjectStore where unmapped XML elements are stored.
     * @param writer Writer from which XML data is read.
     */
    public void toXML(Object source, ObjectStore store, Writer writer) {

        XMLStreamWriter parser = getXmlStreamWriter(writer);
        XMLSimpleWriter simpleWriter = new XMLSimpleWriter(parser, store, new XmlStreamSettings(), configuration.isPrettyPrint());

        getRootMapper(source.getClass()).toXML(source, simpleWriter);
    }

    private RootMapper getRootMapper(XMLSimpleReader simpleReader) {
        // read the root XML element name and lookup the right RootMapper
        QName rootName = simpleReader.getRootName();
        RootMapper rootMapper = configuration.getRootElementMapper(rootName);

        // was the the right RootMapper found?
        if (rootMapper == null) {
            throw new XliteConfigurationException("Error: No class mapping found for " +
                    "root XML element <" + (rootName.getPrefix().length() == 0 ? "" : rootName.getPrefix() + ":")
                    + rootName.getLocalPart() + ">");
        }
        return rootMapper;
    }

    private RootMapper getRootMapper(Class sourceClass) {
        RootMapper rootMapper = configuration.getRootElementMapper(sourceClass);

        // was the the right RootMapper found?
        if (rootMapper == null) {
            throw new XliteConfigurationException("Error: No class mapping found for " +
                    "root class: " + sourceClass.getName());
        }
        return rootMapper;
    }

    private synchronized XMLStreamReader getXmlStreamReader(Reader reader) {
        XMLStreamReader xmlreader;
        try {
            xmlreader = xmlInputFactory.createXMLStreamReader(reader);
        } catch (XMLStreamException e) {
            throw new XliteException("Error initalizing XMLStreamReader", e);
        }
        return xmlreader;
    }


    private synchronized XMLStreamWriter getXmlStreamWriter(Writer writer) {
        try {
            return xmlOutputFactory.createXMLStreamWriter(writer);
        } catch (XMLStreamException e) {
            throw new XliteException("Error initalizing XMLStreamWriter", e);
        }
    }

    /**
     * Container class to hold deserialized Object and unmapped XML elements.
     */
    public static class Result {
        private ObjectStore store;
        private Object object;

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
