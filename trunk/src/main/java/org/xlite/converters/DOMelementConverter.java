/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
package org.xlite.converters;

import org.xlite.XMLSimpleReader;
import org.xlite.XMLSimpleWriter;
import org.xlite.MappingContext;
import org.xlite.DOMelement;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;

public class DOMelementConverter implements ElementConverter {

    public boolean canConvert(Class type) {
        return DOMelement.class.isAssignableFrom(type);
    }

    public Object fromElement(XMLSimpleReader reader, MappingContext mappingContext, String defaultValue, String format, Class targetType, Object targetObject) {
        DOMelement element = new DOMelement();
        element.setName(reader.getName());

        // XML element attributes
        Iterator<Map.Entry<QName, String>> attributeSet = reader.getAttributeIterator();
        while (attributeSet.hasNext()) {
            Map.Entry<QName, String> entry = attributeSet.next();
            QName attrQName = entry.getKey();
            String attrValue = entry.getValue();

            if (attrValue.length() != 0) {
                element.addAttribute(attrQName, attrValue);
            }
        }

        String text = reader.getText();
        if (text.length() != 0) element.appendText(text);

        QName qname;
        while (reader.moveDown()) {

            // recursivelly call DOMelementConverter
            element.appendElement((DOMelement) this.fromElement(reader, mappingContext, null, null, null, null));

            reader.moveUp();

            text = reader.getText();
            if (text.length() != 0) element.appendText(text);
        }
        return element;
    }

    public void toElement(Object object, QName elementName, XMLSimpleWriter writer, MappingContext mappingContext, String defaultValue, String format) {
        DOMelement element = (DOMelement) object;

        // write a start tag
        writer.startElement(element.getName());


        //write out attributes
        HashMap<QName, String> attrs = element.getAttributes();
        for (Map.Entry<QName, String> attr : attrs.entrySet()) {
            writer.addAttribute(attr.getKey(), attr.getValue());
        }

        // write subelements
        for (Object o : element.getElements()) {
             if(DOMelement.isElement(o)){
                 DOMelement subElement = (DOMelement) o;
                 this.toElement(subElement, null, writer, mappingContext, null, null);
             } else if(DOMelement.isText(o)){
                 writer.addText((String) o);
             }
        }

        // write end tag
        writer.endElement();
    }
}
