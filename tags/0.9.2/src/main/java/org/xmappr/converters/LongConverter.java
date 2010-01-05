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
public class LongConverter extends ValueConverter {

    public boolean canConvert(Class type) {
        return type.equals(long.class) || Long.class.isAssignableFrom(type);
    }

    public Object fromValue(String value, String format, Class targetType, Object targetObject) {
        return Long.valueOf(value);
    }

    public String toValue(Object object, String format) {
        return ((Long) object).toString();
    }
}
