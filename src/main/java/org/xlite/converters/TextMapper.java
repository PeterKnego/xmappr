package org.xlite.converters;

import java.lang.reflect.Field;
import java.util.Collection;

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
    private CollectionConverting collectionConverter;
    private Class targetType;
    private boolean isIntermixed;
    private String format;

    public TextMapper(Field accessor, ValueConverter valueConverter, Class targetType,
                      CollectionConverting collectionConverter, boolean isIntermixed, String format) {
        this.accessor = new FieldAccessor(accessor);
        this.valueConverter = valueConverter;
        this.targetType = targetType;
        this.collectionConverter = collectionConverter;
        this.isIntermixed = isIntermixed;
        this.format = format;
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
                collection = collectionConverter.initializeCollection(accessor.getType());
                accessor.setValue(object, collection);
            }
            collection.add(valueConverter.fromValue(elementValue, format));
        } else {
            accessor.setValue(object, valueConverter.fromValue(elementValue, format));
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
            return valueConverter.toValue(accessor.getValue(object), format);
        }
    }

}
