package org.xlite.converters;

/**
 * @author peter
 */
public class CharConverter implements ValueConverter{
    public boolean canConvert(Class type) {
        return type.equals(char.class) || Character.class.isAssignableFrom(type);
    }

    public Object fromValue(String value) {
         if (value.length() == 0) {
            return null;
        } else {
            return value.charAt(0);
        }
    }

    public String toValue(Object object) {
        return ((Character) object).toString();

    }
}
