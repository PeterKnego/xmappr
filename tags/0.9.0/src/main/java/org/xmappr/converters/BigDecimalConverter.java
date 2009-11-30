/*
 * This software is released under the BSD license. Full license available at http://xmappr.googlecode.com
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xmappr contributors
 * All rights reserved.
 */

package org.xmappr.converters;

import java.math.BigDecimal;

public class BigDecimalConverter extends ValueConverter {

    public boolean canConvert(Class type) {
        return BigDecimal.class.isAssignableFrom(type);
    }

    public Object fromValue(String value, String format, Class targetType, Object targetObject) {
        return new BigDecimal(value);
    }

    public String toValue(Object object, String format) {
        return ((BigDecimal) object).toString();
    }
}
