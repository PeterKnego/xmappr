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
public class CharConverter extends ValueConverter{
    
    public boolean canConvert(Class type) {
        return type.equals(char.class) || Character.class.isAssignableFrom(type);
    }

    public Object fromValue(String value, String format, Class targetType, Object targetObject) {
            return value.charAt(0);
    }

    public String toValue(Object object, String format) {
        return ((Character) object).toString();
    }
}
