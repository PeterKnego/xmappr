/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
package org.xlite.converters;

public class ByteArrayConverter extends ValueConverter {

    public boolean canConvert(Class type) {
        Class dataType = type.getComponentType();
        return type.isArray() && (byte.class.equals(dataType) || Byte.class.isAssignableFrom(dataType));
    }

    public Object fromValue(String value, String format, Class targetType, Object targetObject) {
        return Base64.decode(value);
    }

    public String toValue(Object object, String format) {
        return String.valueOf(Base64.encode((byte[]) object));
    }
}