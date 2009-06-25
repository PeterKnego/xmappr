package org.xlite.converters;

/**
 * @author peter
 */
public class LongConverter extends ValueConverter {

    public boolean canConvert(Class type) {
        return type.equals(long.class) || Long.class.isAssignableFrom(type);
    }

    public Object fromValue(String value, String format) {
        return Long.valueOf(value);
    }

    public String toValue(Object object, String format) {
        return ((Long) object).toString();
    }
}
