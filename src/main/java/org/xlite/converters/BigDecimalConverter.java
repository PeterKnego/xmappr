/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */

package org.xlite.converters;

import java.math.BigDecimal;

public class BigDecimalConverter  extends ValueConverter {

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
