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
public class ShortConverter extends ValueConverter {

    public boolean canConvert(Class type) {
        return type.equals(short.class) || Short.class.isAssignableFrom(type);
    }

    public Object fromValue(String value, String format) {
        return Short.valueOf(value);
    }

    public String toValue(Object object, String format) {
        return ((Short) object).toString();
    }
}
