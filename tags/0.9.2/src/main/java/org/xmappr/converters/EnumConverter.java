/*
 * This software is released under the BSD license. Full license available at http://xmappr.googlecode.com
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xmappr contributors
 * All rights reserved.
 */
package org.xmappr.converters;

public class EnumConverter extends ValueConverter {

    public boolean canConvert(Class type) {
        return type.isEnum() || Enum.class.isAssignableFrom(type);
    }

    public Object fromValue(String value, String format, Class targetType, Object targetObject) {

        // Workaround for a JVM quirk:
        // Classes that extend Enum and also implement some interface appear as if they are an inner class.
        // So to get their type, parent class needs to be used.
        if (targetType.getSuperclass() != Enum.class) {
            targetType = targetType.getSuperclass();
        }
        return Enum.valueOf(targetType, value);
    }

    public String toValue(Object object, String format) {
        return ((Enum) object).name();
    }
}
