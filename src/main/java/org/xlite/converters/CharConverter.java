package org.xlite.converters;

/**
 * @author peter
 */
public class CharConverter extends ValueConverter{
    
    public boolean canConvert(Class type) {
        return type.equals(char.class) || Character.class.isAssignableFrom(type);
    }

    public Object fromValue(String value, String format) {
            return value.charAt(0);
    }

    public String toValue(Object object, String format) {
        return ((Character) object).toString();
    }
}
