package org.xlite.converters;

import org.xlite.XliteException;

import javax.xml.namespace.QName;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: peter
 * Date: Apr 20, 2009
 * Time: 10:56:12 AM
 * To change this template use File | Settings | File Templates.
 */
public class AttributeMapper {

    private final FieldAccessor targetField;
    private final ValueConverter valueConverter;

    //todo Are default values used on the attributes?
    private final String defaultValue;
    private Object defaultObject;
    private final String format;
    private final boolean isMap;

    public AttributeMapper(Field targetField, ValueConverter valueConverter, String defaultValue, String format) {
        this.targetField = new FieldAccessor(targetField);
        this.valueConverter = valueConverter;
        this.defaultValue = defaultValue;
        if (defaultValue != null) {
            this.defaultObject = valueConverter.fromValue(defaultValue, format);
        }
        this.format = format;
        this.isMap = Map.class.isAssignableFrom(targetField.getType());
    }

    public void setValue(QName attributeName, Object object, String elementValue) {
        FieldAccessor targetField = this.targetField;
        // is it a Map?
        if (isMap) {
            Map<QName, Object> targetMap = (Map<QName, Object>) targetField.getValue(object);
            if (targetMap == null) {
                targetMap = initializeMap(targetField.getType());
                targetField.setValue(object, targetMap);
            }
            targetMap.put(attributeName, valueConverter.fromValue(elementValue, format));
        } else {
            setValue(object, elementValue);
        }
    }

    public String getValue(Object attributeName, Object object) {
        FieldAccessor targetField = this.targetField;
        // is it a Map?
        if (isMap) {
            Map<QName, String> target = (Map<QName, String>) targetField.getValue(object);
            return valueConverter.toValue(target.get(attributeName), format);
        } else {
            return getValue(object);
        }
    }

    public boolean isTargetMap() {
        return isMap;
    }

    public Object getTarget(Object parent) {
        return targetField.getValue(parent);
    }

    private Map<QName, Object> initializeMap(Class targetType) {
        // is target class a Collection?
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

    /**
     * Assigns a value to the Field.
     *
     * @param object       Instance of an Object that contains the Field.
     * @param elementValue Value to be set.
     */
    private void setValue(Object object, String elementValue) {
        // value is empty?
        if (elementValue.length() == 0) {

            // default value is used
            if (defaultValue != null && defaultValue.length() != 0) {
                targetField.setValue(object, defaultValue);
            }
        } else {
            Object value = valueConverter.fromValue(elementValue, format);
            targetField.setValue(object, value);
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
        if (targetObject == null) {
            // use default value if defined
            return defaultObject != null ? valueConverter.toValue(defaultObject, format) : null;
        } else {
            return valueConverter.toValue(targetObject, format);
        }
    }

    private Class<? extends Map> getConcreteMapType(Class targetType) {
        if (targetType == Map.class) {
            return LinkedHashMap.class;
        }
        return targetType;
    }
}
