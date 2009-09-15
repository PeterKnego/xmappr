/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
package org.xlite.converters;

import org.xlite.MappingContext;
import org.xlite.XMLSimpleReader;
import org.xlite.XMLSimpleWriter;
import org.xlite.XliteException;

import javax.xml.namespace.QName;

/**
 * @author peter
 */
public class ValueConverterWrapper implements ElementConverter {

    private ValueConverter valueConverter;

    public ValueConverterWrapper(ValueConverter valueConverter) {
        this.valueConverter = valueConverter;
    }

    public boolean canConvert(Class type) {
        return valueConverter.canConvert(type);
    }

    public Object fromElement(XMLSimpleReader reader, MappingContext mappingContext, String defaultValue, String format, Class targetType) {
        String text = reader.getText();
        if (text == null || text.length() == 0) {
            if (defaultValue != null) {
                return valueConverter.fromValue(defaultValue, format, targetType);
            } else if (!valueConverter.convertsEmpty()) {
                throw new XliteException("Converter of type "+valueConverter.getClass()+" can not convert empty elements. " +
                        "Either element must not be empty or a default value must be supplied via @XMLelement(defaultValue = \"val\")");
            }
        }
        return valueConverter.fromValue(text, format, null);
    }

    public void toElement(Object object, QName nodeName, XMLSimpleWriter writer, MappingContext mappingContext, String defaultValue, String format) {
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
