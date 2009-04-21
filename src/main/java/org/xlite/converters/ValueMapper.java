/**
 * User: peter
 * Date: Feb 15, 2008
 * Time: 9:36:39 PM
 */
package org.xlite.converters;

import java.lang.reflect.Field;

/**
 * Used to get/set values of a Field. Internally keeps a reference to a Field.
 * Values are converted to/from Strings, using assigned converters.
 *
 * @author peter
 */
public class ValueMapper {

    private FieldAccessor targetField;
    private ValueConverter valueConverter;
    private String defaultValue;
    private Object defaultObject;

    public ValueMapper(Field targetField, ValueConverter valueConverter, String defaultValue) {
        this.targetField = new FieldAccessor(targetField);
        this.valueConverter = valueConverter;
        this.defaultValue = defaultValue;
        if (defaultValue != null) {
            this.defaultObject = valueConverter.fromValue(defaultValue);
        }
    }

    public Object getObject(Object parent) {
        return targetField.getValue(parent);
    }

    public void setObject(Object parent, Object value){
        targetField.setValue(parent, value);
    }

    /**
     * Assigns a value to the Field.
     *
     * @param object       Instance of an Object that contains the Field.
     * @param elementValue Value to be set.
     */
    public void setValue(Object object, String elementValue) {
        // value is empty?
        if (elementValue.length() == 0) {

            // default value is used
            if (defaultValue != null && defaultValue.length() != 0) {
                targetField.setValue(object, defaultValue);
            }
        } else {
            Object value = valueConverter.fromValue(elementValue);
            targetField.setValue(object, value);
        }
    }

    /**
     * Reads a value from a Field.
     *
     * @param object Instance of an Object that contains the Field.
     * @return
     */
    public String getValue(Object object) {
        Object targetObject;
        targetObject = targetField.getValue(object);
        if (targetObject == null) {
            // use default value if defined
            return defaultObject != null ? valueConverter.toValue(defaultObject) : "";
        } else {
            return valueConverter.toValue(targetObject);
        }
    }

}
