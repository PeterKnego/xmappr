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

public class Xlite {

    private final Configuration configuration;

    private final XMLInputFactory xmlInputFactory;
    private final XMLOutputFactory xmlOutputFactory;

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

    public Object fromXML(Reader reader) {

        XMLStreamReader rdr = getXmlStreamReader(reader);
        XMLSimpleReader simpleReader = new XMLSimpleReader(rdr, false);

        return getRootMapper(simpleReader).getRootObject(simpleReader);
    }

    public Result fromXMLwithUnknown(Reader reader) {

        XMLSimpleReader simpleReader = new XMLSimpleReader(getXmlStreamReader(reader), true);

        Object object = getRootMapper(simpleReader).getRootObject(simpleReader);
        return new Result(object, simpleReader.getObjectStore());
    }

    public void toXML(Object source, Writer writer) {

        XMLStreamWriter parser = getXmlStreamWriter(writer);
        XMLSimpleWriter simpleWriter = new XMLSimpleWriter(parser, new XmlStreamSettings(), configuration.isPrettyPrint());

        getRootMapper(source.getClass()).toXML(source, simpleWriter);
    }

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
            throw new XliteConfigurationException("Error: No class mapping found for the " +
                    "root XML element <" + rootName + ">");
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
