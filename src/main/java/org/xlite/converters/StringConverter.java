/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
package org.xlite.converters;

/**
 *
 * User: peter
 * Date: Mar 2, 2008
 * Time: 10:02:25 PM
 */
public class StringConverter extends ValueConverter {

    public boolean canConvert(Class type) {
        return String.class.isAssignableFrom(type);
    }

    public Object fromValue(String value, String format, Class targetType) {
        return value.intern();
    }

    public String toValue(Object object, String format) {
        return (String) object;
    }

    @Override
    public boolean convertsEmpty() {
        return true;
    }
}
