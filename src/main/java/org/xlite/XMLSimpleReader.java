/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
package org.xlite;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.*;

/**
 * A wrapper around {@link javax.xml.stream.XMLStreamReader} that enables access to XML on the element level.
 * It's no longer necessary to create a loop pulling and processing events. A simple usage example:<br><br>
 * {@code XMLSimpleReader reader = new XMLSimpleReader(xmlStreamReader);}<br>
 * {@code reader.getNodeName();}<br>
 * {@code reader.getAttributes();}
 *
 * @author peter
 */
public class XMLSimpleReader {

    private XMLStreamReader reader;
    private XmlStreamSettings settings = new XmlStreamSettings();

    private Stack<Element> elementStack = new Stack<Element>();
    private boolean isEnd = false;
    private boolean isStoringUnknownElements;
    private ObjectStore eventCache;
    private ObjectStore objectStore;

    public XMLSimpleReader(XMLStreamReader reader) {
        this(reader, false);
    }

    public XMLSimpleReader(XMLStreamReader reader, boolean isStoringUnknownElements) {
        this.reader = reader;
        this.isStoringUnknownElements = isStoringUnknownElements;
        if (this.isStoringUnknownElements) {
            objectStore = new ObjectStore(200, 200);
            eventCache = new ObjectStore(200, 200);
        }
    }

    public ObjectStore getObjectStore() {
        return objectStore;
    }

    private int nextEvent() {
        try {
            return reader.next();
        } catch (XMLStreamException e) {
            throw new XliteException("Error reading XML stream.", e);
        }
    }

    /**
     * Finds next START or END of a XML element.
     * Accumulates the CHARACTER data for the current element.
     *
     * @return True if START, false if END.
     */
    private boolean nextElementBoundary() {
        return nextElementBoundary(true);
    }

    private boolean nextElementBoundary(boolean processEvents) {

        // checkAndReset the accumulated Text
        if (!elementStack.isEmpty()) {
            elementStack.peek().resetText();
        }
        // read stream settings at the beginning of the document
        if (reader.getEventType() == XMLStreamConstants.START_DOCUMENT) {
            settings.encoding = reader.getEncoding() == null ? "UTF-8" : reader.getEncoding();
            settings.version = reader.getVersion() == null ? "1.0" : reader.getVersion();
        }

        for (int event = nextEvent(); true; event = nextEvent()) {
            switch (event) {
                case XMLStreamConstants.START_ELEMENT:
                    if (isStoringUnknownElements) {
                        eventCache.trim();
                        eventCache.mark();
                        storeElement(eventCache);
                    }
                    return true;
                case XMLStreamConstants.END_DOCUMENT:
                    if (isStoringUnknownElements) {
                        storeElement(eventCache);
                    }
                    isEnd = true;
                    return false;
                case XMLStreamConstants.END_ELEMENT:
                    if (isStoringUnknownElements) {
                        eventCache.trim();
                        storeElement(eventCache);
                    }
                    return false;
                case XMLStreamConstants.CHARACTERS:
                    if (processEvents && !reader.isWhiteSpace()) {
                        String text = reader.getText();
//                        System.out.println("TEXT" + elementStack.peek().hashCode() + ":" + text + "|");
                        if (isStoringUnknownElements) {
                            storeElement(eventCache);
                        }
                        elementStack.peek().addText(text);
                    }
                    break;
            }
        }
    }

    /**
     * Checks if a next child node exists and moves into it.<br><br>
     * Postions the underlying xml stream to the opening element of the child node.
     *
     * @return True if next child node exists, otherwise false.
     */
    public boolean moveDown() {

        int event = reader.getEventType();
        if (event == XMLStreamConstants.START_ELEMENT) {
            Element element = new Element();
            element.name = reader.getName();
            int attrCount = reader.getAttributeCount();
            for (int i = 0; i < attrCount; i++) {
                element.putAttribute(reader.getAttributeName(i), reader.getAttributeValue(i));
            }
            elementStack.push(element);
        } else {
            if (event != XMLStreamConstants.END_ELEMENT && event != XMLStreamConstants.END_DOCUMENT) {
                throw new XliteException("ERROR: this should be a node END. Instead it's a event=" + event);
            }
            return false;
        }
        nextElementBoundary();
        return true;
    }

    /**
     * Moves back from a child node into the parent node.
     * Postions the underlying xml stream to the closing element of the child node.
     */
    public void moveUp() {
        if (reader.getEventType() == XMLStreamConstants.END_ELEMENT) {
            elementStack.pop();
            nextElementBoundary();
            return;
        }
        int depth = 1;
        boolean continueLooping = true;
        while (continueLooping) {
            if (nextElementBoundary()) {  // node START
                depth++;
            } else {      // node END
                if (depth-- == 0) {
                    continueLooping = false;
                    nextElementBoundary();
                }
            }
        }
        elementStack.pop();
    }

    public String getText() {
        StringBuilder text = elementStack.peek().text;
        if (text.length() == 0) return "";
        return text.toString();
    }

    public QName getName() {
        if (elementStack.isEmpty()) {
            return null;
        }
        return elementStack.peek().name;
    }


    public Iterator<Map.Entry<QName, String>> getAttributeIterator() {
        return elementStack.peek().iterator();
    }

    public static class Element implements Iterable {
        public QName name;
        private StringBuilder text;
        private Map<QName, String> attributes = new HashMap<QName, String>();

        public void addText(String text) {
            this.text.append(text);
        }

        public void resetText() {
            if (text == null) {
                text = new StringBuilder();
            } else {
                text.delete(0, text.length());
            }
        }

        public void putAttribute(QName qname, String value) {
            attributes.put(qname, value);
        }

        public Iterator<Map.Entry<QName, String>> iterator() {
            return new AttributeIterator(attributes.entrySet());
        }
    }

    public static class AttributeIterator implements Iterator<Map.Entry<QName, String>> {
        private Iterator<Map.Entry<QName, String>> iterator;

        public AttributeIterator(Set<Map.Entry<QName, String>> entries) {
            this.iterator = entries.iterator();
        }

        public boolean hasNext() {
            return iterator.hasNext();
        }

        public Map.Entry<QName, String> next() {
            return iterator.next();
        }

        public void remove() {
            throw new UnsupportedOperationException("AttributeIterator does not implement method remove().");
        }
    }

    public boolean findFirstElement() {
        return findFirstElement((QName) null);
    }

    public boolean findFirstElement(String nodeName) {
        return findFirstElement(new QName(nodeName));
    }

    public boolean findFirstElement(QName qName) {
        // handle empty argument
        if (qName == null || qName.getLocalPart().equals("")) {
            return nextElementBoundary(false);
        }
        while (true) {
            if (nextElementBoundary(false)) {
                if (reader.getName().equals(qName)) {
                    moveDown();
                    return true;
                }
            } else {
                if (isEnd) {
                    return false;
                }
            }
        }
    }

    public void saveSubTree(Object reference) {

        // restore only if objectStore is setValue
        if (objectStore == null) return;

        if (eventCache == null) {
            throw new XliteException("Error: saved ObjectStore is null.");
        }
        // save a starting position of this block
        objectStore.addStart(reference);

        // calculate current depth of xml nodes in objectStore
        int depth = eventCache.getDepth();

        //copy cached xml events from event cache
        objectStore.copyFrom(eventCache);
        eventCache.reset();

        // check if stream is already at the end of subtree (happens when there is only one empty node in a sutree)
        int event = reader.getEventType();
        if (event == XMLStreamConstants.END_ELEMENT) {
            objectStore.addEnd();
            return;
        } else {
            event = nextEvent();
        }

        boolean loop = true;
        while (loop) {
            switch (event) {
                case XMLStreamConstants.START_ELEMENT:
                    depth++;
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if (--depth == 0) {
                        loop = false;
                    }
                    break;
            }
            storeElement(objectStore);
            if (loop) {
                event = nextEvent();
            }
        }
        objectStore.addEnd();
    }

    private void storeElement(ObjectStore store) {
        int xmlEventType = reader.getEventType();
        QName qName;
        String name;
        switch (xmlEventType) {
            case XMLStreamConstants.START_DOCUMENT:
                store.addElement(XMLStreamConstants.START_DOCUMENT);
                break;
            case XMLStreamConstants.END_DOCUMENT:
                store.addElement(XMLStreamConstants.END_DOCUMENT);
                break;
            case XMLStreamConstants.START_ELEMENT:
                qName = reader.getName();
                // save namespace
                store.cacheNamespace(qName.getPrefix(), qName.getNamespaceURI(), settings.encoding);
                name = qName.getPrefix() + "=" + qName.getLocalPart();
                store.addElement(XMLStreamConstants.START_ELEMENT, name, settings.encoding);
                addAtributes(store, settings.encoding);
                addNamespaces(store, settings.encoding);
                break;
            case XMLStreamConstants.END_ELEMENT:
                store.addElement(XMLStreamConstants.END_ELEMENT);
                break;
            case XMLStreamConstants.CHARACTERS:
                if (!reader.isWhiteSpace()) {
                    store.addElement(XMLStreamConstants.CHARACTERS, reader.getText().trim(), settings.encoding);
                }
                break;
        }
    }

    private void addAtributes(ObjectStore store, String encoding) {
        QName qName;
        String name;
        for (int i = 0, n = reader.getAttributeCount(); i < n; ++i) {
            qName = reader.getAttributeName(i);
            name = qName.getPrefix() + "=" + qName.getLocalPart(); //+ qName.getNamespaceURI();
            store.addElement(XMLStreamConstants.ATTRIBUTE, name + "=" + reader.getAttributeValue(i), encoding);
        }
    }

    private void addNamespaces(ObjectStore store, String encoding) {
        String uri;
        String prefix;
        for (int i = 0, n = reader.getNamespaceCount(); i < n; ++i) {
            uri = reader.getNamespaceURI(i);
            prefix = reader.getNamespacePrefix(i);
            store.addElement(XMLStreamConstants.NAMESPACE, prefix + "=" + uri, encoding);
        }
    }

}
