package org.xlite.converters;

/**
 * @author peter
 */
public class FloatConverter implements ValueConverter {

    public boolean canConvert(Class type) {
        return type.equals(float.class) || Float.class.isAssignableFrom(type);
    }

    public Object fromValue(String value) {
        return Float.valueOf(value);
    }

    public String toValue(Object object) {
        return ((Float) object).toString();
    }

}
