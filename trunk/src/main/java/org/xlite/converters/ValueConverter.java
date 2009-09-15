/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
package org.xlite.converters;

/**
 * todo fill this
 */
public abstract class ValueConverter {

    public abstract boolean canConvert(Class type);

    public abstract Object fromValue(String value, String format, Class targetType);

    public abstract String toValue(Object object, String format);

    public boolean convertsEmpty(){
        return false;
    }
}
