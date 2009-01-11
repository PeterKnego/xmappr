package org.xlite;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.*;

/**
 * A wrapper around {@link javax.xml.stream.XMLStreamReader}, that simplifies usage. It's no longer necessary to create
 * a loop pulling and processing events. A simple usage example:<br><br>
 * {@code XMLSimpleReader reader = new XMLSimpleReader(xmlStreamReader);}<br>
 * {@code reader.getNodeName();}<br>
 * {@code reader.getAttributes();}
 *
 * @author peter
 */
public class XMLSimpleReader {

    public XMLStreamReader reader;  //todo make private
    private XmlStreamSettings settings = new XmlStreamSettings();

    private Stack<Element> elementStack = new Stack<Element>();
    private boolean isEnd = false;
    private boolean isStoringUnknownElements;
    private SubTreeStore eventCache;

//    private int DP;

    public XMLSimpleReader(XMLStreamReader reader) {
        this(reader, false);
    }

    public XMLSimpleReader(XMLStreamReader reader, boolean isStoringUnknownElements) {
        this.reader = reader;
        this.isStoringUnknownElements = isStoringUnknownElements;
        if (this.isStoringUnknownElements) {
            eventCache = new SubTreeStore(200, 200);
        }
    }

    private int nextEvent() {
        try {
            int i = reader.next();
//             System.out.println("event:" + i);
            return i;
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
            StringBuilder sb = elementStack.peek().text;
            if (sb == null) {
                elementStack.peek().text = new StringBuilder();
            } else {
                sb.delete(0, sb.length());
            }
        }
        // read stream settings at the beginning of the document
        if (reader.getEventType() == XMLStreamConstants.START_DOCUMENT) {
            settings.encoding = reader.getEncoding() == null ? "UTF-8" : reader.getEncoding();
            settings.version = reader.getVersion() == null ? "1.0" : reader.getEncoding();
        }

        for (int event = nextEvent(); true; event = nextEvent()) {
            switch (event) {
                case XMLStreamConstants.START_ELEMENT:
//                    System.out.println("start: " + reader.getName());
                    if (isStoringUnknownElements) {
                        eventCache.trim();
                        eventCache.mark();
                        processElement(eventCache, "test");
                    }
                    return true;
                case XMLStreamConstants.END_DOCUMENT:
                    if (isStoringUnknownElements) {
                        processElement(eventCache, "test");
                    }
//                    System.out.println("end document ");
                    isEnd = true;
                    return false;
                case XMLStreamConstants.END_ELEMENT:
                    if (isStoringUnknownElements) {
                        eventCache.trim();
                        processElement(eventCache, "test");
                    }
//                    System.out.println("end: " + reader.getName());
                    return false;
                case XMLStreamConstants.CHARACTERS:
                    String text = reader.getText().trim();
                    if (processEvents && text.length() > 0) {
                        if (isStoringUnknownElements) {
                            processElement(eventCache, "test");
                        }
                        elementStack.peek().text.append(text);
//                        System.out.println(" text:" + text);
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
//        System.out.println("+moveDown() init");

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
//            String nm = (reader.getEventType() == 1 || reader.getEventType() == 2) ? reader.getName().getLocalPart() : "";
//            System.out.println("-moveDown() false " + elementStack.peek().name + "  (" + reader.getEventType() + ":" + nm + ")");
//            System.out.println("");
            return false;
        }
        nextElementBoundary();
//        String nm = (reader.getEventType() == 1 || reader.getEventType() == 2) ? reader.getName().getLocalPart() : "";
//        DP++;
//        System.out.println("-moveDown() " + DP + " true " + elementStack.peek().name + "  (" + reader.getEventType() + ":" + nm + ")");
//        System.out.println("");
        return true;
    }

    /**
     * Moves back from a child node into the parent node.
     * Postions the underlying xml stream to the closing element of the child node.
     */
    public void moveUp() {
//        System.out.println("+moveUP() init");
        if (reader.getEventType() == XMLStreamConstants.END_ELEMENT) {
//            System.out.println("pop:" + elementStack.peek().name.getLocalPart());
            elementStack.pop();
            nextElementBoundary();
//            DP--;
//            String nm = (reader.getEventType() == 1 || reader.getEventType() == 2) ? reader.getName().getLocalPart() : "";
//            System.out.println("-moveUp() " + DP + " " + elementStack.peek().name + "  (" + reader.getEventType() + ":" + nm + ")");
//            System.out.println("");
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
//        DP--;
//        String nm = (reader.getEventType() == 1 || reader.getEventType() == 2) ? reader.getName().getLocalPart() : "";
//        System.out.println("-moveUp() " + depth + " " + elementStack.peek().name + "  (" + reader.getEventType() + ":" + nm + ")");
//        System.out.println("");
    }

    public String getText() {
        if (elementStack.isEmpty()) {
            throw new XliteException("Error: there are no XML nodes available to be read.");
        }
        return elementStack.peek().text.toString();
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
        public StringBuilder text;
        private Map<QName, String> attributes = new HashMap<QName, String>();

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

    public void saveSubTree(SubTreeStore store, Object object) {
        if (eventCache == null) {
            throw new XliteException("Error: passed SubTreeStore is null.");
        }
//        printStore(eventCache, "TEST");
        // save a starting position of this block
        store.addStart(object);

        // calculate current depth of xml nodes in store
//        int depth = lastEventCache.getDepth() + currentEventCache.getDepth();
        int depth = eventCache.getDepth();
//        int lineNo = reader.getLocation().getLineNumber();

        //copy cached xml events from event cache
        store.copyFrom(eventCache);
        eventCache.reset();

        // check if stream is already at the end of subtree (happens when there is only one empty node in a sutree)
        int event = reader.getEventType();
        if (event == XMLStreamConstants.END_ELEMENT) {
            store.addEnd();
//            String nm = (reader.getEventType() == 1 || reader.getEventType() == 2) ? reader.getName().getLocalPart() : "";
//            printLastBlock(store, "STORE");
//            System.out.println("finished at  (" + reader.getEventType() + ":" + nm + ")");
//            System.out.println("");
            return;
        } else {
            event = nextEvent();
        }

//        QName qName;
//        String name;
        boolean loop = true;
        while (loop) {
            switch (event) {
                case XMLStreamConstants.START_ELEMENT:
                    depth++;
//                    System.out.println("START: " + reader.getName().getLocalPart() + " " + depth);
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if (--depth == 0) {
                        loop = false;
                    }
//                    depth--;
//                    System.out.println("END: " + reader.getName().getLocalPart() + " " + depth + " loop:" + loop);
                    break;
            }
            processElement(store, "treeStore");
            if (loop) {
                event = nextEvent();
            }
        }
        store.addEnd();

//        String nm = (reader.getEventType() == 1 || reader.getEventType() == 2) ? reader.getName().getLocalPart() : "";
//        printLastBlock(store, "STORE");
//        System.out.println("finished at  (" + reader.getEventType() + ":" + nm + ")");
//        System.out.println("");
    }

    private void processElement(SubTreeStore store, String desc) {
        int xmlEventType = reader.getEventType();
        QName qName;
        String name;
        switch (xmlEventType) {
            case XMLStreamConstants.START_DOCUMENT:
//                System.out.println("process "+desc+" start document");
                store.addElement(XMLStreamConstants.START_DOCUMENT);
                break;
            case XMLStreamConstants.END_DOCUMENT:
//                System.out.println("process "+desc+" end document");
                store.addElement(XMLStreamConstants.END_DOCUMENT);
                break;
            case XMLStreamConstants.START_ELEMENT:
                qName = reader.getName();
                // save namespace
                store.cacheNamespace(qName.getPrefix(), qName.getNamespaceURI(), settings.encoding);
                name = qName.getPrefix() + "=" + qName.getLocalPart();
//                System.out.println("process "+desc+" start element: " + qName.getLocalPart());
                store.addElement(XMLStreamConstants.START_ELEMENT, name, settings.encoding);
                addAtributes(store, settings.encoding);
                addNamespaces(store, settings.encoding);
                break;
            case XMLStreamConstants.END_ELEMENT:
//                System.out.println("process "+desc+" end element: " + reader.getName().getLocalPart());
                store.addElement(XMLStreamConstants.END_ELEMENT);
                break;
            case XMLStreamConstants.CHARACTERS:
                if (!reader.isWhiteSpace()) {
//                    System.out.println("process "+desc+" characters: " + reader.getText());
                    store.addElement(XMLStreamConstants.CHARACTERS, reader.getText().trim(), settings.encoding);
                }
                break;
//            case XMLStreamConstants.CDATA:
//                store.addElement(XMLStreamConstants.CDATA, reader.getText(), settings.encoding);
//                break;
            default:
//                System.out.println("other tag: " + reader.getEventType());
        }
    }

    private void addAtributes(SubTreeStore store, String encoding) {
        QName qName;
        String name;
        for (int i = 0, n = reader.getAttributeCount(); i < n; ++i) {
            qName = reader.getAttributeName(i);
//            name = qName.getPrefix().length() == 0 ? qName.getLocalPart() : (qName.getPrefix() + ":" + qName.getLocalPart());
            name = qName.getPrefix() + "=" + qName.getLocalPart(); //+ qName.getNamespaceURI();
            store.addElement(XMLStreamConstants.ATTRIBUTE, name + "=" + reader.getAttributeValue(i), encoding);
        }
    }

    private void addNamespaces(SubTreeStore store, String encoding) {
        String uri;
        String prefix;
        for (int i = 0, n = reader.getNamespaceCount(); i < n; ++i) {
            uri = reader.getNamespaceURI(i);
            prefix = reader.getNamespacePrefix(i);
            store.addElement(XMLStreamConstants.NAMESPACE, prefix + "=" + uri, encoding);
        }
    }

//    This is only used for debugging
//    public static void printStore(SubTreeStore store, String name) {
//
//        SubTreeStore.Element element = store.getNextElement(0);
//        System.out.print(name + ": ");
//        while (element != null) {
//            System.out.print(element.command + "(" + new String(element.data) + ") ");
//            element = store.getNextElement();
//        }
//        System.out.println("");
//    }


//    This is only used for debugging
//    public static void printLastBlock(SubTreeStore store, String name) {
//
//        SubTreeStore.Element element = store.getNextElement(0);
//        int loc = 0;
//        while (element != null) {
//            if (SubTreeStore.isBlockStart(element)) {
//                loc = element.location;
//            }
//            element = store.getNextElement();
//        }
//
//        element = store.getNextElement(loc);
//        System.out.print(name + "(" + element.location + "): ");
//        while (element != null) {
//            System.out.print(element.command + "(" + new String(element.data) + ") ");
//            element = store.getNextElement();
//        }
//        System.out.println("");
//    }

}
