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
public class DoubleConverter extends ValueConverter {

    public boolean canConvert(Class type) {
        return type.equals(double.class) || Double.class.isAssignableFrom(type);
    }

    public Object fromValue(String value, String format, Class targetType, Object targetObject) {
        return Double.valueOf(value);
    }

    public String toValue(Object object, String format) {
        return ((Double) object).toString();
    }
}
