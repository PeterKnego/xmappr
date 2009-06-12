package org.xlite.converters;

import java.math.BigInteger;

/**
 * Created by IntelliJ IDEA.
 * User: peter
 * Date: Jun 12, 2009
 * Time: 11:45:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class BigIntegerConverter extends ValueConverter {

    public boolean canConvert(Class type) {
        return BigInteger.class.isAssignableFrom(type);
    }

    public Object fromValue(String value) {
        return new BigInteger(value);
    }

    public String toValue(Object object) {
        return ((BigInteger) object).toString();
    }
}