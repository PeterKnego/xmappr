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
public class ShortConverter extends ValueConverter {

    public boolean canConvert(Class type) {
        return type.equals(short.class) || Short.class.isAssignableFrom(type);
    }

    public Object fromValue(String value, String format, Class targetType, Object targetObject) {
        return Short.valueOf(value);
    }

    public String toValue(Object object, String format) {
        return ((Short) object).toString();
    }
}
