/*
 * This software is released under the BSD license. Full license available at http://xmappr.googlecode.com
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xmappr contributors
 * All rights reserved.
 */
package org.xmappr;

import org.xmappr.XmapprException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
        // field is not public - need to access it through accessor methods
        if (!Modifier.isPublic(targetField.getModifiers())) {
            this.getter = findAccessorMethod("get");
            this.setter = findAccessorMethod("set", targetField.getType());
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
            } catch (Exception e) {
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
        return targetField.getType();
    }

    private Method findAccessorMethod(String prepend, Class... type) {
        StringBuilder getter = new StringBuilder(prepend);
        getter.append(targetField.getName());
        getter.replace(prepend.length(), prepend.length() + 1, String.valueOf(getter.charAt(3)).toUpperCase());
        try {
            return targetField.getDeclaringClass().getMethod(getter.toString(), type);
        } catch (NoSuchMethodException e) {
            throw new XmapprException("Could not find " + prepend + "ter method for private field:", e);
        }
    }

}
