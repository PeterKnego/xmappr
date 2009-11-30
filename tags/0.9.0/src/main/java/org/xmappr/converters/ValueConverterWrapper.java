/*
 * This software is released under the BSD license. Full license available at http://xmappr.googlecode.com
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xmappr contributors
 * All rights reserved.
 */
package org.xmappr.converters;

import org.xmappr.MappingContext;
import org.xmappr.XMLSimpleReader;
import org.xmappr.XMLSimpleWriter;
import org.xmappr.XmapprException;

import javax.xml.namespace.QName;

/**
 * Wraps ValueConverter so that it behaves like ElementConverter.
 */
public class ValueConverterWrapper implements ElementConverter {

    private ValueConverter valueConverter;

    public ValueConverterWrapper(ValueConverter valueConverter) {
        this.valueConverter = valueConverter;
    }

    public boolean canConvert(Class type) {
        return valueConverter.canConvert(type);
    }

    public Object fromElement(XMLSimpleReader reader, MappingContext mappingContext, String defaultValue,
                              String format, Class targetType, Object targetObject) {
        String text = reader.getText().trim();
        if (text == null || text.length() == 0) {
            if (defaultValue != null) {
                return valueConverter.fromValue(defaultValue, format, targetType, targetObject);
            } else if (!valueConverter.convertsEmpty()) {
                throw new XmapprException("Converter of type " + valueConverter.getClass() + " can not convert empty elements. " +
                        "Either element must not be empty or a default value must be supplied via @Element(defaultValue = \"val\")");
            }
        }
        return valueConverter.fromValue(text, format, targetType, targetObject);
    }

    public void toElement(Object object, QName nodeName, XMLSimpleWriter writer, MappingContext mappingContext,
                          String defaultValue, String format) {
        if (object != null) {
            writer.startElement(nodeName);
            String value = valueConverter.toValue(object, format);
            // if value equals default value => empty element
            if (!value.equals(defaultValue)) {
                writer.addText(value);
            }
            writer.endElement();
        }
    }
}
