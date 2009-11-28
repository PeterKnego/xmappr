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
public class IntConverter extends ValueConverter {

    public boolean canConvert(Class type) {
        return type.equals(int.class) || Integer.class.isAssignableFrom(type);
    }

    public Object fromValue(String xmlValue, String format, Class targetType, Object targetObject) {
        return Integer.valueOf(xmlValue);
    }

    public String toValue(Object object, String format) {
        return ((Integer) object).toString();
    }
}
