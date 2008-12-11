package org.xlite.converters;

import javax.xml.namespace.QName;

import org.xlite.*;

/**
 * @author peter
 */
public class ElementHolderConverter implements ElementConverter {
    public boolean canConvert(Class type) {
        return ElementHolder.class.isAssignableFrom(type);
    }

    public Object fromElement(XMLSimpleReader reader, MappingContext mappingContext) {
        ElementHolder elementHolder = new ElementHolder(500, 500);
        reader.saveSubTree(elementHolder.getStore(), elementHolder);
        return elementHolder;
    }

    public void toElement(Object object, QName nodeName, XMLSimpleWriter writer, MappingContext mappingContext) {
        ElementHolder elementHolder;
        if (ElementHolder.class.isAssignableFrom(object.getClass())) {
            elementHolder = (ElementHolder) object;
        } else {
            throw new XliteException("ElementHolderConverter can only convert instances of ElementHolder!");
        }
        writer.restoreSubTrees(elementHolder.getStore(), elementHolder);
    }
}
