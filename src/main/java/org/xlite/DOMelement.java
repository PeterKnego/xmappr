package org.xlite;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: peter
 * Date: Apr 16, 2009
 * Time: 9:55:54 PM
 */
public class DOMelement {

    private QName name;
    private HashMap<QName, String> attributes = new HashMap<QName, String>();
    private List<Object> elements = new ArrayList<Object>();

    public void setName(QName name) {
        this.name = name;
    }

    public QName getName() {
        return name;
    }

    public HashMap<QName, String> getAttributes() {
        return attributes;
    }

    public List<Object> getElements() {
        return elements;
    }

    public static boolean isText(Object obj) {
        return String.class.equals(obj.getClass());
    }

    public static boolean isElement(Object obj) {
        return DOMelement.class.equals(obj.getClass());
    }

    public void addAttribute(QName attrQName, String attrValue) {
        attributes.put(attrQName, attrValue);
    }

    public void appendText(String text) {
        elements.add(text);
    }

    public void appendElement(DOMelement element) {
        elements.add(element);
    }
}
