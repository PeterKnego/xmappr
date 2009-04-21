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

    private ValueMapper valueMapper;
    private Class targetType;

    public AttributeMapper(Field targetField, ValueConverter valueConverter, String defaultValue) {
        this.valueMapper = new ValueMapper(targetField, valueConverter, defaultValue);
        this.targetType = targetField.getType();
    }

    public void setValue(QName attributeName, Object object, String elementValue) {
        // is it a Map?
        if (Map.class.isAssignableFrom(targetType)) {
            Map<QName, String> targetMap = (Map<QName, String>) valueMapper.getObject(object);
            if (targetMap == null) {
                targetMap = initializeMap(targetType);
                valueMapper.setObject(object, targetMap);
            }
            targetMap.put(attributeName, elementValue);
        } else {
            valueMapper.setValue(object, elementValue);
        }
    }

    public String getValue(QName attributeName, Object object) {
        // is it a Map?
        if (Map.class.isAssignableFrom(targetType)) {
            Map<QName, String> target = (Map<QName, String>) valueMapper.getObject(object);
            return target.get(attributeName);
        } else {
            return valueMapper.getValue(object);
        }
    }

    private Map<QName, String> initializeMap(Class targetType) {
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
            return HashMap.class;
        }
        return targetType;
    }
}
