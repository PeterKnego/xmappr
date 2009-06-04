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

    ValueMapper valueMapper;

    public AttributeMapper(Field targetField, ValueConverter valueConverter, String defaultValue) {
        this.valueMapper = new ValueMapper(targetField, valueConverter, defaultValue);
    }

    public void setValue(QName attributeName, Object object, String elementValue) {
        FieldAccessor targetField = valueMapper.getTargetField();
        // is it a Map?
        if (Map.class.isAssignableFrom(targetField.getType())) {
            Map<QName, Object> targetMap = (Map<QName, Object>) targetField.getValue(object);
            if (targetMap == null) {
                targetMap = initializeMap(targetField.getType());
                targetField.setValue(object, targetMap);
            }
            targetMap.put(attributeName, valueMapper.getValueConverter().fromValue(elementValue));
        } else {
            valueMapper.setValue(object, elementValue);
        }
    }

    public String getValue(QName attributeName, Object object) {
        FieldAccessor targetField = valueMapper.getTargetField();
        // is it a Map?
        if (Map.class.isAssignableFrom(targetField.getType())) {
            Map<QName, String> target = (Map<QName, String>) targetField.getValue(object);
            return valueMapper.getValueConverter().toValue(target.get(attributeName));
        } else {
            return valueMapper.getValue(object);
        }
    }

    public boolean isTargetMap(){
        return Map.class.isAssignableFrom(valueMapper.getTargetField().getType());
    }
    
    public Object getTarget(Object parent){
        return valueMapper.getTargetField().getValue(parent);
    }

    private Map<QName, Object> initializeMap(Class targetType) {
        // is target class a Collection?
        if (!Map.class.isAssignableFrom(targetType)) {
            throw new XliteException("Error: Target class " + targetType.getName() + " can not be cast to java.util.Map!");
        }
        Class<? extends Map> concreteType = getConcreteMapType(targetType);
        try {
            return concreteType.newInstance();
        } catch (InstantiationException e) {
            throw new XliteException("Could not instantiate collection " + targetType.getName() + ". ", e);
        } catch (IllegalAccessException e) {
            throw new XliteException("Could not instantiate collection " + targetType.getName() + ". ", e);
        }
    }

    private Class<? extends Map> getConcreteMapType(Class targetType) {
        if (targetType == Map.class) {
            return LinkedHashMap.class;
        }
        return targetType;
    }
}
