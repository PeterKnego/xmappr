package org.xlite;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author peter
 */
public class XMLSimpleWriter {

    private XMLStreamWriter writer;
    private XmlStreamSettings settings = new XmlStreamSettings();
    private List<Element> elementCache = new ArrayList<Element>();
    private List<Element> predefinedNamespaces = new ArrayList<Element>();
    private ObjectStore objectStore;

    public final boolean isPrettyPrinting;
    private StringBuilder tabs = new StringBuilder("\n");

    public XMLSimpleWriter(XMLStreamWriter writer, XmlStreamSettings settings, boolean prettyPrint) {
        this(writer, null, settings, prettyPrint);
    }

    public XMLSimpleWriter(XMLStreamWriter writer, ObjectStore objectStore, XmlStreamSettings settings, boolean prettyPrint) {
        this.settings = settings;
        this.objectStore = objectStore;
        this.writer = writer;
        this.isPrettyPrinting = prettyPrint;
    }

    private void prettyPrint() {
        if (isPrettyPrinting) {
            try {
                writer.writeCharacters(tabs.toString());
            } catch (XMLStreamException e) {
                throw new XliteException(e);
            }
        }
    }

    private void prettyPrintIncreaseDepth() {
        if (isPrettyPrinting) {
            tabs.insert(1, "  ");
        }
    }

    private void prettyPrintDecreaseDepth() {
        if (isPrettyPrinting) {
            tabs.delete(1, 3);
        }
    }

    public void startDocument() {
        try {
            writer.writeStartDocument(settings.encoding, settings.version);
        } catch (XMLStreamException e) {
            throw new XliteException(e);
        }
    }

    public void startNode(String prefix, String localName, String namespaceURI) {
        flushElementCache();
        prettyPrint();
        prettyPrintIncreaseDepth();
//            System.out.println("start: " + prefix + ":" + localName + "  ns=" + namespaceURI);
        elementCache.add(0, new Element(Element.START_NODE, prefix, localName, namespaceURI, ""));
    }

    public void startElement(QName qname) {
//        System.out.println("ELEMENT: "+qname.getLocalPart());
        startNode(qname.getPrefix(), qname.getLocalPart(), qname.getNamespaceURI());
    }

    public void endElement() {
        try {
            // if there are elements in cache, then this is an empty node (has no subelements or text)
            if (elementCache.size() != 0) {
                if (elementCache.get(0).command != Element.START_NODE) {
                    throw new XliteException("ERROR: first element should always be START_NODE!");
                } else {
                    // change the node type to empty node
                    elementCache.get(0).command = Element.EMPTY_NODE;
                }
                flushElementCache();
                prettyPrintDecreaseDepth();

            // this node contains internal elements (text or subelements) and should have a normal closing tag
            } else {
                flushElementCache();
                prettyPrintDecreaseDepth();
                prettyPrint();
                writer.writeEndElement();
            }
//            System.out.println("end:");
        } catch (XMLStreamException e) {
            throw new XliteException(e);
        }
    }

    public void endDocument() {
        try {
//            System.out.println("end document:");
            flushElementCache();
            writer.writeEndDocument();
            writer.flush();
        } catch (XMLStreamException e) {
            throw new XliteException(e);
        }
    }

    public void addAttribute(String prefix, String localName, String namespaceURI, String value) {
        elementCache.add(new Element(Element.ATTRIBUTE, prefix, localName, namespaceURI, value));
    }

    public void addAttribute(QName qname, String value) {
        String prefix = qname.getPrefix();
        String localName = qname.getLocalPart();
        String namespaceURI = qname.getNamespaceURI();
        addAttribute(prefix, localName, namespaceURI, value);
    }

    public void addText(String text) {
//        System.out.println("  TEXT: "+text);
        try {
            flushElementCache();
            prettyPrint();
            writer.writeCharacters(text);
        } catch (XMLStreamException e) {
            throw new XliteException(e);
        }
    }

    public void addCDATA(String data) {
        try {
            flushElementCache();
            prettyPrint();
            writer.writeCData(data);
        } catch (XMLStreamException e) {
            throw new XliteException(e);
        }
    }

    public void addComment(String text) {
        try {
            flushElementCache();
            prettyPrint();
            writer.writeComment(text);
        } catch (XMLStreamException e) {
            throw new XliteException(e);
        }
    }

    public void predefineNamespaces(NsContext namespaces) {
        for (Map.Entry<String, String> namespace : namespaces) {
            predefinedNamespaces.add(new Element(Element.NAMESPACE, namespace.getKey(), "", namespace.getValue(), ""));
        }
    }

    public void addNamespaces(NsContext namespaces) {
        for (Map.Entry<String, String> namespace : namespaces) {
            addNamespace(namespace.getKey(), namespace.getValue());
        }
    }

    private void addNamespace(String prefix, String namespaceURI) {
        elementCache.add(new Element(Element.NAMESPACE, prefix, "", namespaceURI, ""));
    }

    public void restoreSubTrees(Object reference) {

        // restore only if objectStore is setValue
        if(objectStore == null) return;

        List<Integer> locations = objectStore.getLocations(reference);
        if (locations != null) {
            for (Integer location : locations) {
                try {
                    restoreSubTree(objectStore, location);
                } catch (XMLStreamException e) {
                    throw new XliteException(e);
                } catch (UnsupportedEncodingException e) {
                    throw new XliteException(e);
                }
            }
        }
    }

    private void restoreSubTree(ObjectStore store, int location) throws XMLStreamException, UnsupportedEncodingException {

        flushElementCache();

//        XMLSimpleReader.printStore(objectStore, "RE-STORE");

        String prefix, localName, nsURI, value, data;
        String encoding = settings.encoding;  // default encoding
        int first, second, third;
        Map<String, String> nsCache = new HashMap<String, String>();
        boolean emptyNode = true;

        ObjectStore.Element element = store.getNextElement(location);
        if (!ObjectStore.isBlockStart(element)) {
            throw new IllegalArgumentException("Error: XMLSimpleWriter.restoreSubTree was given a wrong location " +
                    "argument: no saved data block is found on given location!");
        }

        while (!ObjectStore.isBlockEnd(element)) {
            switch (element.command) {
                case ObjectStore.NAMESPACE_CACHE:
                    data = new String(element.data);
                    first = data.indexOf('=');
                    prefix = data.substring(0, first);
                    nsURI = data.substring(first + 1, data.length());
                    if (!nsCache.containsKey(prefix)) {
                        nsCache.put(prefix, nsURI);
                    }
                    break;
                case XMLStreamConstants.START_DOCUMENT:
                    emptyNode = true;
                    String header = new String(element.data);
                    String[] headers = header.split("\n");
                    encoding = headers[0].equals("null") ? encoding : headers[0];  // default encoding is UTF-8
                    startDocument();
                    break;
                case XMLStreamConstants.END_DOCUMENT:
                    endDocument();
                    break;
                case XMLStreamConstants.START_ELEMENT:
                    data = new String(element.data);
                    first = data.indexOf('=');
                    prefix = data.substring(0, first);
                    localName = data.substring(first + 1, data.length());
                    nsURI = nsCache.get(prefix);
                    if (nsURI == null) {
                        nsURI = "";
                    }
                    startNode(prefix, localName, nsURI);
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    endElement();
                    break;
                case XMLStreamConstants.ATTRIBUTE:
                    data = new String(element.data);
                    first = data.indexOf('=');
                    second = data.indexOf('=', first + 1);
                    prefix = data.substring(0, first);
                    localName = data.substring(first + 1, second);
                    value = data.substring(second + 1, data.length());
                    nsURI = nsCache.get(prefix);
                    if (nsURI == null) {
                        nsURI = "";
                    }
                    addAttribute(prefix, localName, nsURI, value);
                    break;
                case XMLStreamConstants.NAMESPACE:
                    data = new String(element.data);
                    first = data.indexOf('=');
                    prefix = data.substring(0, first);
                    nsURI = data.substring(first + 1, data.length());
                    addNamespace(prefix, nsURI);
                    break;
                case XMLStreamConstants.CHARACTERS:
                    emptyNode = false;
                    addText(new String(element.data, encoding));
                    break;
                case XMLStreamConstants.CDATA:
                    emptyNode = false;
                    addComment(new String(element.data, encoding));
                    break;
            }
            element = store.getNextElement();
        }
    }

    private void writePredefinedNamespaces() {
        if (predefinedNamespaces.size()!=0) {
            for (Element ns : predefinedNamespaces) {
                try {
                    writer.writeNamespace(ns.prefix, ns.nsURI);
                } catch (XMLStreamException e) {
                    throw new XliteException(e);
                }
            }
            predefinedNamespaces.clear();
        }
    }

    private void flushElementCache() {

        // write out cache contents
        try {
            for (Element element : elementCache) {
                switch (element.command) {
                    case Element.START_NODE:
                        writer.writeStartElement(element.prefix, element.localName, element.nsURI);
                        writePredefinedNamespaces();
                        break;
                    case Element.EMPTY_NODE:
                        writer.writeEmptyElement(element.prefix, element.localName, element.nsURI);
                        break;
                    case Element.NAMESPACE:
                        writer.writeNamespace(element.prefix, element.nsURI);
                        break;
                    case Element.ATTRIBUTE:
                        writer.writeAttribute(element.prefix, element.nsURI, element.localName, element.value);
                        break;
                }
            }
        }
        catch (XMLStreamException e) {
            throw new XliteException(e);
        }

        // cache has been written out, so it can be cleared
        elementCache.clear();
    }

    private static class Element {
        public static final int START_NODE = 1;
        public static final int EMPTY_NODE = 2;
        public static final int NAMESPACE = 3;
        public static final int ATTRIBUTE = 4;

        public int command;
        public String prefix;
        public String localName;
        public String nsURI;
        public String value;

        private Element(int command, String prefix, String localName, String nsURI, String value) {
            this.command = command;
            this.localName = localName;
            this.nsURI = nsURI;
            this.prefix = prefix;
            this.value = value;
        }
    }

}
