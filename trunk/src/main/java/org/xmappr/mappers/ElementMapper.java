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
import org.xmappr.FieldAccessor;
import org.xmappr.mappers.TextMapper;

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
    private Class targetType;
    private MappingContext mappingContext;
    private ElementConverter elementConverter;

    // the following three fields are used in handling Collection mapping
    private CollectionConverting collectionConverter;
    private Map<Class, QName> targetTypes = new HashMap<Class, QName>();
    private Map<QName, ElementConverter> converterCache = new HashMap<QName, ElementConverter>();

    // default value as set by the @Element(defaultValue="..") annotation
    private String defaultValue;

    // converter formatting options as set by the @Element(format="..") annotation
    private String format;

    public ElementMapper(Field targetField, CollectionConverting collectionConverter, MappingContext mappingContext) {
        this.targetField = new FieldAccessor(targetField);
        this.mappingContext = mappingContext;
        this.collectionConverter = collectionConverter;
    }

    public void setTargetType(Class targetType) {
        this.targetType = targetType;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public void setConverter(ElementConverter fieldConverter) {
        this.elementConverter = fieldConverter;
    }

    public void addMapping(QName nodeName, ElementConverter elementConverter, Class targetType) {
//        if (elementConverter == null) {
//            elementConverter = mappingContext.lookupElementConverter(targetType);
//        }
        this.targetTypes.put(targetType, nodeName);
        this.converterCache.put(nodeName, elementConverter);
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
            throw new XmapprException("Error: could not find converter for node: " + nodeName +
                    " in collection " + collection.getClass().getName() +
                    " in class " + collection.getClass().getEnclosingClass() +
                    ". Collection contains element types that are not defined in @Element annotation.");
        }

        Object obj = converter.fromElement(reader, mappingContext, defaultValue, format, targetType, collection);

        // do nothing if ElementConverter returns null
        if (obj != null) {
            collectionConverter.addItem(collection, obj);
        }
    }

    private void setFieldValue(Object targetObject, XMLSimpleReader reader) {
        // process XML element and create an appropriate object
        Object obj = elementConverter.fromElement(reader, mappingContext, defaultValue, format, targetType,
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
//                System.out.println("---OBJ:"+obj);
                if (textMapper != null && textMapper.isTargetType(obj)) {
                    writer.addText(textMapper.getValue(obj));
                } else {
                    QName name = targetTypes.get(obj.getClass());
                    if (name == null) {
                        name = nodeName;
                    }
                    ElementConverter converter = converterCache.get(name);
                    converter.toElement(obj, name, writer, mappingContext, defaultValue, format);
                }
            }

        } else {   // normal field
            elementConverter.toElement(targetField.getValue(object), nodeName, writer, mappingContext, defaultValue, format);
        }
    }

}
