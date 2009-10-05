package org.xlite.converters;

/**
 * ValueConverter for converting to/from Strings, similar to StringConverter. The difference is
 * that EmptyStringConverter does not convert to/from empty values. E.g. on input empty value it will produce null
 * instead of empty string (""). On output if field value is "" it will produce no XML element or attribute.
 */
public class EmptyStringConverter extends ValueConverter {

    public boolean canConvert(Class type) {
        return String.class.isAssignableFrom(type);
    }

    public Object fromValue(String value, String format, Class targetType, Object targetObject) {
        return (value == null || value.length() == 0) ? null : value.intern();
    }

    public String toValue(Object object, String format) {

        return (object == null || ((String) object).length() == 0) ? null : (String) object;
    }

    @Override
    public boolean convertsEmpty() {
        return false;
    }
}
