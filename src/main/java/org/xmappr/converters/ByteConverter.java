/*
 * This software is released under the BSD license. Full license available at http://xmappr.googlecode.com
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xmappr contributors
 * All rights reserved.
 */
package org.xmappr.converters;

/**
 * @author peter
 */
public class ByteConverter extends ValueConverter {

    public boolean canConvert(Class type) {
        return type.equals(byte.class) || Byte.class.isAssignableFrom(type);
    }

    public Object fromValue(String value, String format, Class targetType, Object targetObject) {
        return Byte.valueOf(value);
    }

    public String toValue(Object object, String format) {
        return ((Byte) object).toString();
    }
}
