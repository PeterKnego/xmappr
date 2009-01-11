package org.xlite.converters;

import org.xlite.MappingContext;
import org.xlite.XMLSimpleReader;
import org.xlite.XMLSimpleWriter;

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
            } else {
                return null;
            }
        }
        return valueConverter.fromValue(text);
    }

    public void toElement(Object object, QName nodeName, XMLSimpleWriter writer, MappingContext mappingContext) {
        if (object != null) {
            writer.startElement(nodeName);
            writer.addText(valueConverter.toValue(object));
            writer.endElement();
        }
    }
}
