package org.xlite.converters;

import org.xlite.XliteException;

import javax.xml.namespace.QName;
import java.lang.reflect.Field;
import java.util.Map;
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

    private FieldAccessor targetField;
    private ValueConverter valueConverter;
    private Class targetType;

    public TextMapper(Field targetField, ValueConverter valueConverter, Class targetType) {
        this.targetField = new FieldAccessor(targetField);
        this.valueConverter = valueConverter;
        this.targetType = targetType;
    }


    /**
     * Assigns a value to the Field.
     *
     * @param object       Instance of an Object that contains the Field.
     * @param elementValue Value to be set.
     */
    public void setValue(Object object, String elementValue) {
        // is it a Collection?
        if (Collection.class.isAssignableFrom(targetField.getType())) {
            Collection collection = (Collection) targetField.getValue(object);
            if (collection == null) {
                collection = initializeCollection(targetField.getType());
                targetField.setValue(object, collection);
            }
            collection.add(valueConverter.fromValue(elementValue));
        } else {
            targetField.setValue(object, elementValue);

        }
    }

    /**
     * Reads a value from a Field.
     *
     * @param object Instance of an Object that contains the Field.
     * @return
     */
    public String getValue(Object object) {
            return valueConverter.toValue(object);
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
