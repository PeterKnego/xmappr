/*
 * This software is released under the BSD license. Full license available at http://xmappr.googlecode.com
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xmappr contributors
 * All rights reserved.
 */
package org.xmappr.mappers;

import org.xmappr.*;
import org.xmappr.converters.CollectionConverting;
import org.xmappr.converters.ElementConverter;

import javax.xml.namespace.QName;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
    private ElementConverter wildcardConverter;

    // the following three fields are used in handling Collection mapping
    private CollectionConverting collectionConverter;
    private Map<Class, QName> targetTypes = new HashMap<Class, QName>();
    private Map<QName, Class> targetTypesByQname = new HashMap<QName, Class>();
    private Map<QName, ElementConverter> convertersByQName = new HashMap<QName, ElementConverter>();

    // default value as set by the @Element(defaultValue="..") annotation
    private String defaultValue;

    // converter formatting options as set by the @Element(format="..") annotation
    private String format;

    public ElementMapper(Field targetField, Method getter, Method setter,
                         CollectionConverting collectionConverter, MappingContext mappingContext) {
        this.targetField = new FieldAccessor(targetField, getter, setter);
        this.mappingContext = mappingContext;
        this.collectionConverter = collectionConverter;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public void setWildcardConverter(ElementConverter fieldConverter) {
        this.wildcardConverter = fieldConverter;
    }

    public void addMapping(QName nodeName, ElementConverter elementConverter, Class targetType) {

        this.targetTypes.put(targetType, nodeName);
        this.targetTypesByQname.put(nodeName, targetType);
        this.convertersByQName.put(nodeName, elementConverter);
    }

    public void readElement(QName nodeName, Object targetObject, XMLSimpleReader reader) {
        if (collectionConverter == null) {
            setFieldValue(nodeName, targetObject, reader);
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
        ElementConverter cachedConverter = convertersByQName.get(nodeName);
        ElementConverter converter = cachedConverter != null ? cachedConverter : wildcardConverter;
        Class targetType = targetTypesByQname.get(nodeName);

        if (converter == null) {
            throw new XmapprException("Error: could not find converter for node: " + nodeName +
                    " in collection " + collection.getClass().getName() +
                    " in class " + targetObject.getClass().getName() +
                    ". Collection contains element types that are not defined in @Element annotation.");
        }

        Object obj = converter.fromElement(reader, mappingContext, defaultValue, format, targetType, collection);

        // do nothing if ElementConverter returns null
        if (obj != null) {
            collectionConverter.addItem(collection, obj);
        }
    }

    private void setFieldValue(QName nodeName, Object targetObject, XMLSimpleReader reader) {
        // process XML element and create an appropriate object
        ElementConverter cachedConverter = convertersByQName.get(nodeName);
        ElementConverter converter = cachedConverter != null ? cachedConverter : wildcardConverter;
        Class targetType = targetTypesByQname.get(nodeName);
        Object obj = converter.fromElement(reader, mappingContext, defaultValue, format, targetType,
                targetField.getValue(targetObject));

        // do nothing if ElementConverter returns null
        if (obj != null) {
            // link this object to a field
            targetField.setValue(targetObject, obj);
        }
    }

    public void writeElement(Object object, QName nodeName, XMLSimpleWriter writer, TextMapper textMapper) {
        // it's a collection
        if (collectionConverter != null) {
            Collection collection = (Collection) targetField.getValue(object);
            if (collection == null) {
                return;
            }
            for (Object obj : collection) {
                if (textMapper != null && textMapper.isTargetType(obj)) {
                    writer.addText(textMapper.getValue(obj));
                } else {
                    // check if given object matches any of the target types
                    QName name = targetTypes.get(obj.getClass());
                    // if not, try finding the right converter for this object
                    ElementConverter converterFound = null;
                    if (name == null) {
                        for (ElementConverter converter : convertersByQName.values()) {
                            if (converter.canConvert(obj.getClass())) {
                                converterFound = converter;
                            }
                        }
                    } else {
                        // converter found via target type
                        converterFound = convertersByQName.get(name);
                    }
                    if (converterFound == null) {
                        throw new XmapprException("No converter found for the object type " + obj.getClass().getName() +
                                " in collection " + collection.getClass().getName() +
                                " in class " + object.getClass().getName());
                    }
                    converterFound.toElement(obj, name, writer, mappingContext, defaultValue, format);
                }
            }

        } else {   // normal field
            convertersByQName.get(nodeName).toElement(
                    targetField.getValue(object), nodeName, writer, mappingContext, defaultValue, format);
        }
    }

}
