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


//todo write javadoc - IMPORTANT
/**
 * @author peter
 */
public class ElementMapper {

    private FieldAccessor targetField;
    private MappingContext mappingContext;
    public ElementConverter elementConverter;

    // the following three fields are used in handling Collection mapping
    private CollectionConverting collectionConverter;
    private Map<Class, QName> itemTypes = new HashMap<Class, QName>();
    private Map<QName, ElementConverter> converterCache = new HashMap<QName, ElementConverter>();

    // default value as setValue by the @XMLelement(defaultValue=??) annotation
    private String defaultValue;

    public ElementMapper(Field targetField, CollectionConverting collectionConverter, MappingContext mappingContext) {
        this.targetField = new FieldAccessor(targetField);
        this.mappingContext = mappingContext;
        this.collectionConverter = collectionConverter;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
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
            setFieldValue(targetObject, reader);
        } else {
            collectionAddItem(nodeName, targetObject, reader);
        }
    }

    private void collectionAddItem(QName nodeName, Object targetObject, XMLSimpleReader reader) {
        Collection collection = (Collection) targetField.getValue(targetObject);

        // initialize collection if needed
        if (collection == null) {
            collection = collectionConverter.initializeCollection(targetField.getType());
            targetField.setValue(targetObject, collection);
        }

        // find the converter for given node name
        ElementConverter cachedConverter = converterCache.get(nodeName);
        ElementConverter converter = cachedConverter != null ? cachedConverter : elementConverter;

        if (converter == null) {
            throw new XliteException("Error: could not find converter for node: " + nodeName +
                    " in collection " + collection.getClass().getName() +
                    " in class " + collection.getClass().getEnclosingClass() +
                    ". Collection contains element types that are not defined in @XMLelement annotation.");
        }

        Object value = converter.fromElement(reader, mappingContext, defaultValue);
        collectionConverter.addItem(collection, value);
    }

    private void setFieldValue(Object targetObject, XMLSimpleReader reader) {
        Object value = elementConverter.fromElement(reader, mappingContext, defaultValue);
        targetField.setValue(targetObject, value);
    }

    public void writeElement(Object object, QName nodeName, XMLSimpleWriter writer, TextMapper textMapper) {
        // it's a collection
        if (collectionConverter != null) {
            Collection collection = (Collection) targetField.getValue(object);
            if (collection == null) {
                return;
            }
            for (Object obj : collection) {
//                System.out.println("---OBJ:"+obj);
                if (textMapper != null && textMapper.isTargetType(obj)) {
                    writer.addText(textMapper.getValue(obj));
                } else {
                    QName name = itemTypes.get(obj.getClass());
                    ElementConverter converter = converterCache.get(name);
                    converter.toElement(obj, name, writer, mappingContext, defaultValue);
                }
            }

        } else {   // normal field
            elementConverter.toElement(targetField.getValue(object), nodeName, writer, mappingContext, defaultValue);
        }
    }

}
