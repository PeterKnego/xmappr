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

    public ValueConverter valueConverter;

    public ValueConverterWrapper(ValueConverter valueConverter) {
        this.valueConverter = valueConverter;
    }

    public boolean canConvert(Class type) {
        return valueConverter.canConvert(type);
    }

    public Object fromElement(XMLSimpleReader reader, MappingContext mappingContext, String defaultValue) {
        String text = reader.getText();
        if (text == null || text.length() == 0) {
            if (defaultValue != null) {
                return valueConverter.fromValue(defaultValue);
            } else if (!valueConverter.convertsEmpty()) {
                throw new XliteException("Converter of type "+valueConverter.getClass()+" can not convert empty elements. " +
                        "Either element must not be empty or you a default value must be cupploied via @XMLelement(defaultValue = \"val\")");
            }
        }
        return valueConverter.fromValue(text);
    }

    public void toElement(Object object, QName nodeName, XMLSimpleWriter writer, MappingContext mappingContext, String defaultValue) {
        if (object != null) {
            writer.startElement(nodeName);
            String value = valueConverter.toValue(object);
            // if value equals default value => empty elemnt
            if (!value.equals(defaultValue)) {
                writer.addText(value);
            }
            writer.endElement();
        }
    }
}
