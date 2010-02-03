/*
 * This software is released under the BSD license. Full license available at http://xmappr.googlecode.com
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xmappr contributors
 * All rights reserved.
 */
package org.xmappr.converters;

import org.xmappr.DomElement;
import org.xmappr.MappingContext;
import org.xmappr.XMLSimpleReader;
import org.xmappr.XMLSimpleWriter;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DomElementConverter implements ElementConverter {

    public boolean canConvert(Class type) {
        return DomElement.class.isAssignableFrom(type);
    }

    public Object fromElement(XMLSimpleReader reader, MappingContext mappingContext, String defaultValue, String format, Class targetType, Object targetObject) {
        DomElement element = new DomElement();
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

            // recursivelly call DomElementConverter
            element.appendElement((DomElement) this.fromElement(reader, mappingContext, null, null, null, null));

            reader.moveUp();

            text = reader.getText();
            if (text.length() != 0) element.appendText(text);
        }
        return element;
    }

    public void toElement(Object object, QName elementName, XMLSimpleWriter writer, MappingContext mappingContext, String defaultValue, String format) {
        DomElement element = (DomElement) object;

        // write a start tag
        writer.startElement(element.getName());


        //write out attributes
        HashMap<QName, String> attrs = element.getAttributes();
        for (Map.Entry<QName, String> attr : attrs.entrySet()) {
            writer.addAttribute(attr.getKey(), attr.getValue());
        }

        // write subelements
        for (Object o : element.getElements()) {
            if (DomElement.isElement(o)) {
                DomElement subElement = (DomElement) o;
                this.toElement(subElement, null, writer, mappingContext, null, null);
            } else if (DomElement.isText(o)) {
                writer.addText((String) o);
            }
        }

        // write end tag
        writer.endElement();
    }
}
