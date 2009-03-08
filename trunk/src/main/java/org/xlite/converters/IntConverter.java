package org.xlite.converters;

/**
 * @author peter
 */
public class IntConverter extends ValueConverter {

    public boolean canConvert(Class type) {
        return type.equals(int.class) || Integer.class.isAssignableFrom(type);
    }

    public Object fromValue(String value) {
        return Integer.valueOf(value);
    }

    public String toValue(Object object) {
        return ((Integer) object).toString();
    }
}
