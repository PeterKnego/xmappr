package org.xlite.converters;

import org.xlite.XliteException;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: peter
 * Date: May 6, 2009
 * Time: 12:03:14 AM
 * To change this template use File | Settings | File Templates.
 */
public class TextMapper {

    private FieldAccessor accessor;
    private ValueConverter valueConverter;
    private Class targetType;
    private boolean isIntermixed;

    public TextMapper(Field accessor, ValueConverter valueConverter, Class targetType, boolean isIntermixed) {
        this.accessor = new FieldAccessor(accessor);
        this.valueConverter = valueConverter;
        this.targetType = targetType;
        this.isIntermixed = isIntermixed;
    }


    public boolean isCollection() {
        return Collection.class.isAssignableFrom(accessor.getType());
    }

    public boolean isIntermixed() {
        return isIntermixed;
    }

    public boolean isTargetType(Object object) {
        return targetType.equals(object.getClass());
    }

    /**
     * Assigns a value to the Field.
     *
     * @param object       Instance of an Object that contains the Field.
     * @param elementValue Value to be set.
     */
    public void setValue(Object object, String elementValue) {
        // is it a Collection?
        if (isCollection()) {
            Collection collection = (Collection) accessor.getValue(object);
            if (collection == null) {
                collection = initializeCollection(accessor.getType());
                accessor.setValue(object, collection);
            }
            collection.add(valueConverter.fromValue(elementValue));
        } else {
            accessor.setValue(object, elementValue);
        }
    }

    /**
     * Reads a value from a Field.
     *
     * @param object Instance of an Object that contains the Field.
     * @return
     */
    public String getValue(Object object) {
        // is it a Collection?
        if (isCollection()) {
            return valueConverter.toValue(object);
        } else {
            return valueConverter.toValue(accessor.getValue(object));
        }
    }

    public Collection initializeCollection(Class targetType) {
        // is target class a Collection?
        if (!Collection.class.isAssignableFrom(targetType)) {
            throw new XliteException("Error: Target class " + targetType.getName() + " can not be cast to java.util.Collection!");
        }
        Class<? extends Collection> concreteType = getConcreteCollectionType(targetType);
        try {
            return concreteType.newInstance();
        } catch (InstantiationException e) {
            throw new XliteException("Could not instantiate collection " + targetType.getName() + ". ", e);
        } catch (IllegalAccessException e) {
            throw new XliteException("Could not instantiate collection " + targetType.getName() + ". ", e);
        }
    }

    private Class<? extends Collection> getConcreteCollectionType(Class<? extends Collection> targetType) {
        if (targetType == List.class || targetType == Collection.class) {
            return ArrayList.class;
        }
        return targetType;
    }

}
