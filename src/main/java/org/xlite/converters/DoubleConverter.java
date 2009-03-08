package org.xlite.converters;

/**
 * @author peter
 */
public class DoubleConverter extends ValueConverter{

    public boolean canConvert(Class type) {
        return type.equals(double.class) || Double.class.isAssignableFrom(type);
    }

    public Object fromValue(String value) {
        return Double.valueOf(value);
    }

    public String toValue(Object object) {
        return ((Double) object).toString();
    }

}
