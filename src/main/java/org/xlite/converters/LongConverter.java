/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
package org.xlite.converters;

/**
 * @author peter
 */
public class LongConverter extends ValueConverter {

    public boolean canConvert(Class type) {
        return type.equals(long.class) || Long.class.isAssignableFrom(type);
    }

    public Object fromValue(String value, String format, Class targetType) {
        return Long.valueOf(value);
    }

    public String toValue(Object object, String format) {
        return ((Long) object).toString();
    }
}
