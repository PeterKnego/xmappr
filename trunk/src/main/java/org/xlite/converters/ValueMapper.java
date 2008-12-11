/**
 * User: peter
 * Date: Feb 15, 2008
 * Time: 9:36:39 PM
 */
package org.xlite.converters;

import org.xlite.XliteException;

import java.lang.reflect.Field;

/**
 * Used to get/set values of a Field. It keeps a reference to a Field.
 * Values are internally converted to/from Strings, using assigned converters.
 *
 * @author peter
 */
public class ValueMapper {

    private Field targetField;
    private ValueConverter valueConverter;
    private String defaultValue;
    private Object defaultObject;

    public ValueMapper(Field targetField, ValueConverter valueConverter, String defaultValue) {
        this.targetField = targetField;
        this.valueConverter = valueConverter;
        this.defaultValue = defaultValue;
        if (defaultValue != null) {
            this.defaultObject = valueConverter.fromValue(defaultValue);
        }
    }

    /**
     * Assigns a value to the Field.
     *
     * @param object       Instance of an Object that contains the Field.
     * @param elementValue Value to be set.
     */
    public void setValue(Object object, String elementValue) {
        try {
            if (elementValue.length()==0) {
                return;
            }
            Object value = valueConverter.fromValue(elementValue);
            targetField.set(object, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();    //todo replace with custom exception
        }
    }

    /**
     * Reads a value from a Field.
     *
     * @param object Instance of an Object that contains the Field.
     * @return
     */
    public String getValue(Object object) {
        Object targetObject = null;
        try {
            targetObject = targetField.get(object);
            if (targetObject == null) {
                return "";
            }
            return valueConverter.toValue(targetObject);
        } catch (IllegalAccessException e) {
            throw new XliteException("Field could not be read! ", e);
        }
    }

}
