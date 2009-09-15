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
public class IntConverter extends ValueConverter {

    public boolean canConvert(Class type) {
        return type.equals(int.class) || Integer.class.isAssignableFrom(type);
    }

    public Object fromValue(String xmlValue, String format, Class targetType) {
        return Integer.valueOf(xmlValue);
    }

    public String toValue(Object object, String format) {
        return ((Integer) object).toString();
    }
}
