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
public class BooleanConverter extends ValueConverter {

    private final String positive;
    private final String negative;

    public BooleanConverter(final String positive, final String negative) {
        this.positive = positive;
        this.negative = negative;
    }

    public BooleanConverter() {
        this("true", "false");
    }

    public boolean canConvert(Class type) {
        return type.equals(boolean.class) || Boolean.class.isAssignableFrom(type);
    }

    public Object fromValue(String value, String format, Class targetType, Object targetObject) {
        return value.length() == 0 || positive.equalsIgnoreCase(value) ? Boolean.TRUE : Boolean.FALSE;
    }

    public String toValue(Object object, String format) {
        return ((Boolean) object ? positive : negative);
    }

}
