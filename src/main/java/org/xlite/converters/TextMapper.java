/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
package org.xlite.converters;

import java.lang.reflect.Field;
import java.util.Collection;

public class TextMapper {

    private FieldAccessor targetField;
    private Class targetType;
    private ValueConverter valueConverter;
    private CollectionConverting collectionConverter;
    private boolean isIntermixed;
    private String format;

    //todo finish this javadoc
    /**
     * @param targetField
     * @param targetType
     * @param valueConverter
     * @param collectionConverter
     * @param isIntermixed
     * @param format
     */
    public TextMapper(Field targetField, Class targetType, ValueConverter valueConverter,
                      CollectionConverting collectionConverter, boolean isIntermixed, String format) {
        this.targetField = new FieldAccessor(targetField);
        this.valueConverter = valueConverter;
        this.targetType = targetType;
        this.collectionConverter = collectionConverter;
        this.isIntermixed = isIntermixed;
        this.format = format;
    }


    public boolean isCollection() {
        return Collection.class.isAssignableFrom(targetField.getType());
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
     * @param container    Instance of an Object that contains the Field.
     * @param elementValue Value to be set.
     */
    public void setValue(Object container, String elementValue) {

        Object obj = valueConverter.fromValue(elementValue, format, targetType, targetField.getValue(container));

        // do nothing if ValueConverter returns null 
        if (obj == null) {
            return;
        }

        // is it a Collection?
        if (isCollection()) {
            Collection collection = (Collection) targetField.getValue(container);
            if (collection == null) {
                collection = collectionConverter.initializeCollection(targetField.getType());
                targetField.setValue(container, collection);
            }
            collection.add(obj);
        } else {
            targetField.setValue(container, obj);
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
            if (valueConverter.canConvert(Collection.class)) {
                return valueConverter.toValue(targetField.getValue(object), format);
            } else {
                return valueConverter.toValue(object, format);
            }
        } else {
            return valueConverter.toValue(targetField.getValue(object), format);
        }
    }

}
