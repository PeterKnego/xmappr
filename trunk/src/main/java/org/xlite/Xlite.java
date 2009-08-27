/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
package org.xlite;

import javax.xml.stream.*;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

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

        } catch (Exception e){
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

        return configuration.getRootElementMapper().getRootObject(simpleReader);
    }

    public Result fromXMLwithUnknown(Reader reader) {

        XMLSimpleReader simpleReader = new XMLSimpleReader(getXmlStreamReader(reader), true);

        Object object = configuration.getRootElementMapper().getRootObject(simpleReader);
        return new Result(object, simpleReader.getObjectStore());
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

    public void toXML(Object source, Writer writer) {

        XMLStreamWriter parser = getXmlStreamWriter(writer);
        XMLSimpleWriter simpleWriter = new XMLSimpleWriter(parser, new XmlStreamSettings(), configuration.isPrettyPrint());

        configuration.getRootElementMapper().toXML(source, simpleWriter);
    }

    private synchronized XMLStreamWriter getXmlStreamWriter(Writer writer) {
        try {
            return xmlOutputFactory.createXMLStreamWriter(writer);
        } catch (XMLStreamException e) {
            throw new XliteException("Error initalizing XMLStreamWriter", e);
        }
    }

    public void toXML(Object source, ObjectStore store, Writer writer) {

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
