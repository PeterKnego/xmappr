/*
 * This software is released under the BSD license. Full license available at http://xmappr.googlecode.com
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xmappr contributors
 * All rights reserved.
 */
package org.xmappr.converters;

import org.xmappr.XmapprException;

import javax.xml.namespace.QName;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

public class AttributeMapper {

    private final FieldAccessor targetField;
    private final Class targetType;
    private final ValueConverter valueConverter;

    private Object defaultObject;
    private final String format;
    private final boolean isMap;

    public AttributeMapper(Field targetField, Class targetType, ValueConverter valueConverter, String defaultValue, String format) {
        this.targetField = new FieldAccessor(targetField);
        this.targetType = targetType;
        this.valueConverter = valueConverter;
        if (defaultValue != null) {
            this.defaultObject = valueConverter.fromValue(defaultValue, format, targetType, null);
        }
        this.format = format;
        this.isMap = Map.class.isAssignableFrom(targetField.getType());
    }

    public void setValue(QName attributeName, Object container, String elementValue) {
        FieldAccessor targetField = this.targetField;
        // is it a Map?
        if (isMap) {
            Map targetMap = (Map) targetField.getValue(container);
            if (targetMap == null) {
                targetMap = initializeMap(targetField.getType());
                targetField.setValue(container, targetMap);
            }
            Object obj = valueConverter.fromValue(elementValue, format, targetType, targetMap);

            // do nothing if ValueConverter returns null
            if (obj != null) {
                targetMap.put(attributeName, obj);
            }
        } else {
            setValue(container, elementValue);
        }
    }

    public String getValue(Object attributeName, Object container) {
        // is it a Map?
        if (isMap) {
            Map target = (Map) this.targetField.getValue(container);
            return valueConverter.toValue(target.get(attributeName), format);
        } else {
            return getValue(container);
        }
    }

    public boolean isTargetMap() {
        return isMap;
    }

    public Object getTarget(Object parent) {
        return targetField.getValue(parent);
    }

    private Map<QName, Object> initializeMap(Class<? extends Map> targetType) {
        // is target class a Map?
        if (!Map.class.isAssignableFrom(targetType)) {
            throw new XmapprException("Error: Target class " + targetType.getName() + " can not be cast to java.util.Map!");
        }
        Class<? extends Map> concreteType = getConcreteMapType(targetType);
        try {
            return concreteType.newInstance();
        } catch (Exception e) {
            throw new XmapprException("Could not instantiate Map " + targetType.getName() + ". ", e);
        }
    }

    private Class<? extends Map> getConcreteMapType(Class<? extends Map> targetType) {
        if (targetType == Map.class) {
            return LinkedHashMap.class;
        }
        return targetType;
    }

    /**
     * Assigns a value to the Field.
     *
     * @param container    Instance of an Object that contains the Field.
     * @param elementValue Value to be set.
     */
    private void setValue(Object container, String elementValue) {
        // value is empty?
        if (elementValue.length() == 0) {

            // default value is used
            if (defaultObject != null) {
                targetField.setValue(container, defaultObject);
            }
        } else {
            Object tmp = valueConverter.fromValue(elementValue, format, targetType, targetField.getValue(container));

            // do nothing if ValueConverter returns null
            if (tmp != null) {
                targetField.setValue(container, tmp);
            }
        }
    }

    /**
     * Reads a value from a Field.
     *
     * @param object Instance of an Object that contains the Field.
     * @return
     */
    private String getValue(Object object) {
        Object targetObject = targetField.getValue(object);

        // null target object results in no output
        if (targetObject == null) {
            return null;
        }

        // use default value if defined and equal to target object
        if (defaultObject != null && defaultObject.equals(targetObject)) {
            return null;
        } else {
            String val = valueConverter.toValue(targetObject, format);
            return val;
        }

    }


}
