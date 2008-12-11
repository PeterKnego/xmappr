package org.xlite.converters;

import org.xlite.MappingContext;
import org.xlite.XMLSimpleReader;
import org.xlite.XMLSimpleWriter;
import org.xlite.XliteException;

import javax.xml.namespace.QName;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author peter
 */
public class ElementMapper {

    private Field targetField;
    private CollectionConverting collectionConverter;
    private MappingContext mappingContext;
    public ElementConverter elementConverter;
    private Map<Class, QName> itemTypes = new HashMap<Class, QName>();
    private Map<QName, ElementConverter> converterCache = new HashMap<QName, ElementConverter>();

    public ElementMapper(Field targetField, CollectionConverting collectionConverter, MappingContext mappingContext) {
        this.targetField = targetField;
        this.mappingContext = mappingContext;
        this.collectionConverter = collectionConverter;
    }

    public void setConverter(ElementConverter fieldConverter) {
        this.elementConverter = fieldConverter;
    }

    public void addMapping(QName nodeName, Class itemType) {
        ElementConverter converter = mappingContext.lookupElementConverter(itemType);
        this.itemTypes.put(itemType, nodeName);
        this.converterCache.put(nodeName, converter);
    }

    public void readElement(QName nodeName, Object targetObject, XMLSimpleReader reader) {
        if (collectionConverter == null) {
            setFieldValue(nodeName, targetObject, reader);
        } else {
            collectionAddItem(nodeName, targetObject, reader);
        }
    }

    private void collectionAddItem(QName nodeName, Object targetObject, XMLSimpleReader reader) {
        try {
            Collection collection = (Collection) targetField.get(targetObject);

            // initialize collection if needed
            if (collection == null) {
                collection = collectionConverter.initializeCollection(targetField.getType());
                targetField.set(targetObject, collection);
            }

            // find the converter for given node name
            ElementConverter converter = converterCache.get(nodeName);
            if (converter == null) {
                throw new XliteException("Error: could not find converter for node: "+ nodeName+
                        " in collection " + collection.getClass().getName() +
                        " in class " + collection.getClass().getEnclosingClass() +
                        ". Collection contains element types that are not defined in @XMLelement annotation.");
            }

            Object value = converter.fromElement(reader, mappingContext);
            collectionConverter.addItem(collection, value);
        } catch (IllegalAccessException e) {
            throw new XliteException("Field value could not be set! ", e);
        }
    }

    private void setFieldValue(QName nodeName, Object targetObject, XMLSimpleReader reader) {
        try {
            Object value = elementConverter.fromElement(reader, mappingContext);
            targetField.set(targetObject, value);
        } catch (IllegalAccessException e) {
            throw new XliteException("Field value could not be set! ", e);
        }
    }

    public void writeElement(Object object, QName nodeName, XMLSimpleWriter writer) {
        try {

            // it's a collection
            if (collectionConverter != null) {
                Collection collection = (Collection) targetField.get(object);
                if (collection == null) {
                    return;
                }
                for (Object obj : collection) {
                    QName name = itemTypes.get(obj.getClass());
                    ElementConverter converter = converterCache.get(name);
                    converter.toElement(obj, name, writer, mappingContext);
                }

            // normal field
            } else {
                elementConverter.toElement(targetField.get(object), nodeName, writer, mappingContext);
            }
        } catch (IllegalAccessException e) {
            throw new XliteException("Field value could not be read! ", e);
        }
    }

//    private ElementConverter findConverter(Class type) {
//        ElementConverter converter = null;
//
//        // search the mapper cache
//        for (Map.Entry<Class, ElementConverter> entry : converterCache.entrySet()) {
//            if (entry.getKey().equals(type)) {
//                converter = entry.getValue();
//                return converter;
//            }
//        }
//
//        // if not in cache, lookup globally
//        converter = mappingContext.lookupElementConverter(type);
//        // store in cache for future use
//        converterCache.put(type, converter);
//        return converter;
//    }
}
