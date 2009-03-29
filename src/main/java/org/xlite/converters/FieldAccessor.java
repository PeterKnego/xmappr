package org.xlite.converters;

import org.xlite.XliteException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

/**
 * @author peter
 */
public class FieldAccessor {

    private Field targetField;
    private Method getter;
    private Method setter;

    public FieldAccessor(Field targetField) {
        this.targetField = targetField;
        // field is not public - need to access it thru accessor methods
        if (!Modifier.isPublic(targetField.getModifiers())) {
            this.getter = findAccessorMethod("get");
            this.setter = findAccessorMethod("set", targetField.getType());
        }
    }

    public void set(Object obj, Object value) {
        if (setter == null) {
            try {
                targetField.set(obj, value);
            } catch (IllegalAccessException e) {
                throw new XliteException("Field could not be written to! ", e);
            }
        } else {
            try {
                setter.invoke(obj, value);
            } catch (IllegalAccessException e) {
                throw new XliteException("Setter method could not be called! ", e);
            } catch (InvocationTargetException e) {
                throw new XliteException("Setter method could not be called! ", e);
            }
        }
    }

    public Object get(Object obj) {
        if (getter == null) {
            try {
                return targetField.get(obj);
            } catch (IllegalAccessException e) {
                throw new XliteException("Field could not be read from! ", e);
            }
        } else {
            try {
                return getter.invoke(obj);
            } catch (IllegalAccessException e) {
                throw new XliteException("Getter method could not be called! ", e);
            } catch (InvocationTargetException e) {
                throw new XliteException("Getter method could not be called! ", e);
            }
        }
    }

    public Class getType() {
        return targetField.getType();
    }

    private Method findAccessorMethod(String prepend, Class... type) {
        StringBuilder getter = new StringBuilder(prepend);
        getter.append(targetField.getName());
        getter.replace(prepend.length(), prepend.length() + 1, String.valueOf(getter.charAt(3)).toUpperCase());
        try {
            return targetField.getDeclaringClass().getMethod(getter.toString(), type);
        } catch (NoSuchMethodException e) {
            throw new XliteException("Could not find " + prepend + "ter method for private field:", e);
        }
    }

}
