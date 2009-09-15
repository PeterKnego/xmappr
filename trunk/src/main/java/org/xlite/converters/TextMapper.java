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
     * 
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
     * @param object       Instance of an Object that contains the Field.
     * @param elementValue Value to be set.
     */
    public void setValue(Object object, String elementValue) {
        // is it a Collection?
        if (isCollection()) {
            Collection collection = (Collection) targetField.getValue(object);
            if (collection == null) {
                collection = collectionConverter.initializeCollection(targetField.getType());
                targetField.setValue(object, collection);
            }
            collection.add(valueConverter.fromValue(elementValue, format, targetType));
        } else {
            targetField.setValue(object, valueConverter.fromValue(elementValue, format, targetType));
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
            return valueConverter.toValue(object, format);
        } else {
            return valueConverter.toValue(targetField.getValue(object), format);
        }
    }

}
