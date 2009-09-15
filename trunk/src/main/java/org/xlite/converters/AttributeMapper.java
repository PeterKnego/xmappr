/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
package org.xlite.converters;

import org.xlite.XliteException;

import javax.xml.namespace.QName;
import java.lang.reflect.Field;
import java.util.*;

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
            this.defaultObject = valueConverter.fromValue(defaultValue, format, targetType);
        }
        this.format = format;
        this.isMap = Map.class.isAssignableFrom(targetField.getType());
    }

    public void setValue(QName attributeName, Object object, String elementValue) {
        FieldAccessor targetField = this.targetField;
        // is it a Map?
        if (isMap) {
            Map targetMap = (Map) targetField.getValue(object);
            if (targetMap == null) {
                targetMap = initializeMap(targetField.getType());
                targetField.setValue(object, targetMap);
            }
            targetMap.put(attributeName, valueConverter.fromValue(elementValue, format, targetType));
        } else {
            setValue(object, elementValue);
        }
    }

    public String getValue(Object attributeName, Object holder) {
        // is it a Map?
        if (isMap) {
            Map target = (Map) this.targetField.getValue(holder);
            return valueConverter.toValue(target.get(attributeName), format);
        } else {
            return getValue(holder);
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
            throw new XliteException("Error: Target class " + targetType.getName() + " can not be cast to java.util.Map!");
        }
        Class<? extends Map> concreteType = getConcreteMapType(targetType);
        try {
            return concreteType.newInstance();
        } catch (Exception e) {
            throw new XliteException("Could not instantiate Map " + targetType.getName() + ". ", e);
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
     * @param holder       Instance of an Object that contains the Field.
     * @param elementValue Value to be set.
     */
    private void setValue(Object holder, String elementValue) {
        // value is empty?
        if (elementValue.length() == 0) {

            // default value is used
            if (defaultObject != null) {
                targetField.setValue(holder, defaultObject);
            }
        } else {
            Object value = valueConverter.fromValue(elementValue, format, targetType);
            targetField.setValue(holder, value);
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
        if (targetObject == null) return null;

        // use default value if defined and equal to target object
        if (defaultObject != null && defaultObject.equals(targetObject)) {
            return "";
        } else {
            return valueConverter.toValue(targetObject, format);
        }

    }


}
