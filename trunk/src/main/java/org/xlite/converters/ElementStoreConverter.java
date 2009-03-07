package org.xlite.converters;

import javax.xml.namespace.QName;

import org.xlite.*;

/**
 * @author peter
 */
public class ElementStoreConverter implements ElementConverter {
    public boolean canConvert(Class type) {
        return ElementStore.class.isAssignableFrom(type);
    }

    public Object fromElement(XMLSimpleReader reader, MappingContext mappingContext, String defaultValue) {
        ElementStore elementStore = new ElementStore(500, 500);
        reader.saveSubTree(elementStore);
        return elementStore;
    }

    public void toElement(Object object, QName nodeName, XMLSimpleWriter writer, MappingContext mappingContext, String defaultValue) {
        ElementStore elementStore;
        if (ElementStore.class.isAssignableFrom(object.getClass())) {
            elementStore = (ElementStore) object;
        } else {
            throw new XliteException("ElementStoreConverter can only convert instances of ElementStore!");
        }
        writer.restoreSubTrees(elementStore);
    }
}
