package org.xlite.converters;

/**
 * Created by IntelliJ IDEA.
 * User: peter
 * Date: Jun 12, 2009
 * Time: 11:56:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class ByteArrayConverter extends ValueConverter {

    public boolean canConvert(Class type) {
        Class dataType = type.getComponentType();
        return type.isArray() && (byte.class.equals(dataType) || Byte.class.isAssignableFrom(dataType));
    }

    public Object fromValue(String value) {
        return Base64.decode(value);
    }

    public String toValue(Object object) {
        return String.valueOf(Base64.encode((byte[]) object));
    }
}