package org.xlite.converters;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: peter
 * Date: Jun 12, 2009
 * Time: 11:18:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class BigDecimalConverter  extends ValueConverter {

    public boolean canConvert(Class type) {
        return BigDecimal.class.isAssignableFrom(type);
    }

    public Object fromValue(String value, String format) {
        return new BigDecimal(value);
    }

    public String toValue(Object object, String format) {
        return ((BigDecimal) object).toString();
    }
}
