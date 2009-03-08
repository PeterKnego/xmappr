package org.xlite.converters;

/**
 * @author peter
 */
public class ShortConverter extends ValueConverter {

    public boolean canConvert(Class type) {
        return type.equals(short.class) || Short.class.isAssignableFrom(type);
    }

    public Object fromValue(String value) {
        return Short.valueOf(value);
    }

    public String toValue(Object object) {
        return ((Short) object).toString();
    }
}
