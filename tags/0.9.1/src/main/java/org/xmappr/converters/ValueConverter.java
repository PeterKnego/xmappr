/*
 * This software is released under the BSD license. Full license available at http://xmappr.googlecode.com
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xmappr contributors
 * All rights reserved.
 */
package org.xmappr.converters;

/**
 * todo fill this
 */
public abstract class ValueConverter implements Converter {

    public abstract Object fromValue(String value, String format, Class targetType, Object targetObject);

    public abstract String toValue(Object object, String format);

    public boolean convertsEmpty() {
        return false;
    }
}
