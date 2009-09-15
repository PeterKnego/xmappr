package org.xlite.converters;

public class EnumConverter extends ValueConverter {
    
    public boolean canConvert(Class type) {
        return type.isEnum() || Enum.class.isAssignableFrom(type);
    }

    public Object fromValue(String value, String format, Class targetType) {
        return null;
    }

    public String toValue(Object object, String format) {
        return null;
    }
}
