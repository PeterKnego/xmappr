package org.xlite.converters;

/**
 * @author peter
 */
public class ByteConverter extends ValueConverter {

    public boolean canConvert(Class type) {
        return type.equals(byte.class) || Byte.class.isAssignableFrom(type);
    }

    public Object fromValue(String value) {
        return Byte.valueOf(value);
    }

    public String toValue(Object object) {
        return ((Byte) object).toString();
    }
}
