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

    private FieldAccessor targetField;
    private ValueConverter valueConverter;
    private String defaultValue;

    public AttributeMapper(Field targetField, ValueConverter valueConverter, String defaultValue) {
        this.targetField = new FieldAccessor(targetField);
        this.valueConverter = valueConverter;
        this.defaultValue = defaultValue;
    }

    public void setValue(QName attributeName, Object object, String elementValue) {
        // is it a Map?
        if (Map.class.isAssignableFrom(targetField.getType())) {
            Map<QName, Object> targetMap = (Map<QName, Object>) targetField.getValue(object);
            if (targetMap == null) {
                targetMap = initializeMap(targetField.getType());
                targetField.setValue(object, targetMap);
            }
            targetMap.put(attributeName, valueConverter.fromValue(elementValue));
        } else {
            targetField.setValue(object, valueConverter.fromValue(elementValue));
        }
    }

    public String getValue(QName attributeName, Object object) {
        // is it a Map?
        if (Map.class.isAssignableFrom(targetField.getType())) {
            Map<QName, String> target = (Map<QName, String>) targetField.getValue(object);
            return valueConverter.toValue(target.get(attributeName));
        } else {
            return valueConverter.toValue(targetField.getValue(object));

        }
    }

    private Map<QName, Object> initializeMap(Class targetType) {
        // is target class a Collection?
        if (!Map.class.isAssignableFrom(targetType)) {
            throw new XliteException("Error: Target class " + targetType.getName() + " can not be cast to java.util.Map!");
        }
        Class<? extends Map> concreteType = getConcreteCollectionType(targetType);
        try {
            return concreteType.newInstance();
        } catch (InstantiationException e) {
            throw new XliteException("Could not instantiate collection " + targetType.getName() + ". ", e);
        } catch (IllegalAccessException e) {
            throw new XliteException("Could not instantiate collection " + targetType.getName() + ". ", e);
        }
    }

    private Class<? extends Map> getConcreteCollectionType(Class targetType) {
        if (targetType == Map.class) {
            return LinkedHashMap.class;
        }
        return targetType;
    }
}
