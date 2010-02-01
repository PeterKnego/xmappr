/*
 * This software is released under the BSD license. Full license available at http://xmappr.googlecode.com
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xmappr contributors
 * All rights reserved.
 */
package org.xmappr;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author peter
 */
public class FieldAccessor {

    private Field targetField;
    private Method getter;
    private Method setter;

    public FieldAccessor(Field targetField, Method getter, Method setter) {
        this.targetField = targetField;
        this.getter = getter;
        this.setter = setter;

        if (targetField == null && getter == null && setter == null) {
            throw new XmapprException("ERROR: No method available to access field!");
        }

    }

    public void setValue(Object obj, Object value) {
        if (setter == null) {
            try {
                targetField.set(obj, value);
            } catch (IllegalAccessException e) {
                throw new XmapprException("Field could not be written to! ", e);
            }
        } else {
            try {
                setter.invoke(obj, value);
            } catch (InvocationTargetException e) {
                throw new XmapprException("Setter method could not be called! ", e);
            } catch (IllegalAccessException e) {
                throw new XmapprException("Setter method could not be called! ", e);
            }
        }
    }

    public Object getValue(Object obj) {
        if (getter == null) {

            Object o = null;
            try {
                o = targetField.get(obj);
            } catch (IllegalAccessException e) {
                throw new XmapprException("Field could not be read from! ", e);
            }
            return o;

        } else {
            try {
                return getter.invoke(obj);
            } catch (Exception e) {
                throw new XmapprException("Getter method could not be called! ", e);
            }
        }
    }

    public Class getType() {
        if (targetField != null) {
            return targetField.getType();
        } else if (getter != null) {
            return getter.getReturnType();
        } else if (setter != null) {
            return setter.getParameterTypes()[0];
        }
        // this should not happen, since it's checked in constructor
        return null;
    }

    private Method findAccessorMethod(String prepend, Class... type) {
        StringBuilder getter = new StringBuilder(prepend);
        String fieldName = targetField.getName();

        // sometimes filed names are in the form "_privateField"
        if (fieldName.startsWith("_")) {
            getter.append(String.valueOf(fieldName.charAt(1)).toUpperCase()).append(fieldName, 2, fieldName.length());
        } else {
            getter.append(String.valueOf(fieldName.charAt(0)).toUpperCase()).append(fieldName, 1, fieldName.length());
        }
        try {
            return targetField.getDeclaringClass().getMethod(getter.toString(), type);
        } catch (NoSuchMethodException e) {
            throw new XmapprException("Could not find " + prepend + "ter method for private field:", e);
        }
    }

}
