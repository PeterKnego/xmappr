package org.xlite.converters;

import org.xlite.MappingContext;
import org.xlite.XMLSimpleReader;
import org.xlite.XliteException;
import org.xlite.XMLSimpleWriter;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.lang.reflect.Field;

/**
 * Created by IntelliJ IDEA.
 * User: peter
 * Date: Apr 20, 2009
 * Time: 6:17:13 AM
 * To change this template use File | Settings | File Templates.
 */
public class AttributesMapper {

    private FieldAccessor targetField;
    private MappingContext mappingContext;
    public ValueConverter valueConverter;

    // the following three fields are used in handling Collection mapping
    private MapConverting mapConverter;
    private Map<Class, QName> itemTypes = new HashMap<Class, QName>();
    private Map<QName, ValueConverter> converterCache = new HashMap<QName, ValueConverter>();

    // default value as set by the @XMLelement(defaultValue=??) annotation
    private String defaultValue;

    public AttributesMapper(Field targetField, MapConverting mapConverter, MappingContext mappingContext) {
        this.targetField = new FieldAccessor(targetField);
        this.mappingContext = mappingContext;
        this.mapConverter = mapConverter;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setConverter(ValueConverter fieldConverter) {
        this.valueConverter = fieldConverter;
    }

    public void addMapping(QName nodeName, Class itemType) {
        ValueConverter converter = mappingContext.lookupValueConverter(itemType);
        this.itemTypes.put(itemType, nodeName);
        this.converterCache.put(nodeName, converter);
    }

    public void readElement(QName nodeName, Object targetObject, XMLSimpleReader reader) {
        if (mapConverter == null) {
            setFieldValue(targetObject, reader);
        } else {
            collectionAddItem(nodeName, targetObject, reader);
        }
    }

    private void collectionAddItem(QName nodeName, Object targetObject, String value) {
        Map map = (Map) targetField.get(targetObject);

        // initialize map if needed
        if (map == null) {
            map = mapConverter.initializeMap(targetField.getType());
            targetField.set(targetObject, map);
        }

        // find the converter for given node name
        ValueConverter converter = converterCache.get(nodeName);
        if (converter == null) {
            throw new XliteException("Error: could not find converter for node: " + nodeName +
                    " in map " + map.getClass().getName() +
                    " in class " + map.getClass().getEnclosingClass() +
                    ". Collection contains element types that are not defined in @XMLelement annotation.");
        }

        Object objValue = converter.fromValue(value);
        mapConverter.addItem(map, nodeName, objValue);
    }

    private void setFieldValue(Object targetObject, String value) {
        Object objValue = valueConverter.fromValue(value);
        targetField.set(targetObject, objValue);
    }

    public void writeElement(Object object, QName nodeName, XMLSimpleWriter writer) {
        // it's a collection
        if (mapConverter != null) {
            Collection collection = (Collection) targetField.get(object);
            if (collection == null) {
                return;
            }
            for (Object obj : collection) {
                QName name = itemTypes.get(obj.getClass());
                ValueConverter converter = converterCache.get(name);
                converter.
                converter.toElement(obj, name, writer, mappingContext, defaultValue);
            }

        } else {   // normal field
            valueConverter.toElement(targetField.get(object), nodeName, writer, mappingContext, defaultValue);
        }
    }
}
