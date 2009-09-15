package org.xlite;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.Location;
import javax.xml.namespace.QName;
import javax.xml.namespace.NamespaceContext;

public class XMLFastStreamReader implements XMLStreamReader {

    public String getVersion() {
        return null;
    }

    public String getEncoding() {
        return null;
    }

    public int getEventType() {
        return 0;
    }

    public int next() throws XMLStreamException {
        return 0;
    }

    public int nextTag() throws XMLStreamException {
        return 0;
    }

    public boolean hasNext() throws XMLStreamException {
        return false;
    }

    public boolean isWhiteSpace() {
        return false;
    }

    public String getText() {
        return null;
    }

    public QName getName() {
        return null;
    }


    public int getAttributeCount() {
        return 0;
    }

    public QName getAttributeName(int index) {
        return null;
    }

    public String getAttributeValue(int index) {
        return null;
    }

    public int getNamespaceCount() {
        return 0;
    }

    public String getNamespaceURI(int index) {
        return null;
    }

    public String getNamespacePrefix(int index) {
        return null;
    }

    // end implement

    public String getNamespaceURI(String prefix) {
        return null;
    }

    public String getElementText() throws XMLStreamException {
        return null;
    }

    public boolean isStartElement() {
        return false;
    }

    public boolean isEndElement() {
        return false;
    }

    public boolean isCharacters() {
        return false;
    }

    public String getAttributeValue(String namespaceURI, String localName) {
        return null;
    }

    public String getAttributeNamespace(int index) {
        return null;
    }

    public String getAttributeLocalName(int index) {
        return null;
    }

    public String getAttributePrefix(int index) {
        return null;
    }

    public String getAttributeType(int index) {
        return null;
    }

    public boolean isAttributeSpecified(int index) {
        return false;
    }

    public NamespaceContext getNamespaceContext() {
        return null;
    }

    public char[] getTextCharacters() {
        return new char[0];
    }

    public int getTextCharacters(int sourceStart, char[] target, int targetStart, int length) throws XMLStreamException {
        return 0;
    }

    public int getTextStart() {
        return 0;
    }

    public int getTextLength() {
        return 0;
    }

    public boolean hasText() {
        return false;
    }

    public Location getLocation() {
        return null;
    }

    public String getLocalName() {
        return null;
    }

    public boolean hasName() {
        return false;
    }

    public String getNamespaceURI() {
        return null;
    }

    public String getPrefix() {
        return null;
    }

    public boolean isStandalone() {
        return false;
    }

    public boolean standaloneSet() {
        return false;
    }

    public String getCharacterEncodingScheme() {
        return null;
    }

    public String getPITarget() {
        return null;
    }

    public String getPIData() {
        return null;
    }

    public Object getProperty(String name) throws IllegalArgumentException {
        return null;
    }

    public void require(int type, String namespaceURI, String localName) throws XMLStreamException {

    }

    public void close() throws XMLStreamException {

    }
}
