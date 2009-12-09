/*
 * This software is released under the BSD license. Full license available at http://xmappr.googlecode.com
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xmappr contributors
 * All rights reserved.
 */
package org.xmappr;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Used to store whole XML element subtree. It's is simply used by creating a field of {@link DomElement} type.
 * A special converter, {@link org.xmappr.converters.DomElementConverter}, is assigned to {@link DomElement},
 * which parses the whole subtree and stores it in the {@link DomElement} instance.
 */
public class DomElement {

    private QName name;
    private HashMap<QName, String> attributes = new HashMap<QName, String>();
    private List<Object> elements = new ArrayList<Object>();

    /**
     * Setter for the element name.
     *
     * @param name The {@link QName} of the element
     */
    public void setName(QName name) {
        this.name = name;
    }

    /**
     * Getter for the element name.
     *
     * @return The {@link QName} of the element
     */
    public QName getName() {
        return name;
    }

    /**
     * Getter for the attribute map.
     *
     * @return Returns the map with the element's attributes.
     */
    public HashMap<QName, String> getAttributes() {
        return attributes;
    }

    /**
     * Getter for the list of subelements.
     *
     * @return Returns the List of subelements.
     */
    public List<Object> getElements() {
        return elements;
    }

    public static boolean isText(Object obj) {
        return String.class.equals(obj.getClass());
    }

    public static boolean isElement(Object obj) {
        return DomElement.class.equals(obj.getClass());
    }

    /**
     * Adds a new XML attribute to current element.
     * @param attrQName
     * @param attrValue
     */
    public void addAttribute(QName attrQName, String attrValue) {
        attributes.put(attrQName, attrValue);
    }

    /**
     * Adds an XML text to the current element. XML text is added after existing subelements.
     *
     * @param text
     */
    public void appendText(String text) {
        elements.add(text);
    }

    /**
     * Adds an XML subelement to the current element. Subelement is added after existing subelements.
     * @param element
     */
    public void appendElement(DomElement element) {
        elements.add(element);
    }
}
