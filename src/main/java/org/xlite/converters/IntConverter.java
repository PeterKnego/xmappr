package org.xlite.converters;

/**
 * @author peter
 */
public class IntConverter extends ValueConverter {

    public boolean canConvert(Class type) {
        return type.equals(int.class) || Integer.class.isAssignableFrom(type);
    }

    public Object fromValue(String value, String format) {
        return Integer.valueOf(value);
    }

    public String toValue(Object object, String format) {
        return ((Integer) object).toString();
    }
}
