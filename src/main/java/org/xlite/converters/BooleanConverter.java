package org.xlite.converters;

/**
 * @author peter
 */
public class BooleanConverter implements ValueConverter {

    private final String positive;
    private final String negative;
    private final boolean caseSensitive;

    public BooleanConverter(final String positive, final String negative, final boolean caseSensitive) {
        this.positive = positive;
        this.negative = negative;
        this.caseSensitive = caseSensitive;
    }

    public BooleanConverter() {
        this("true", "false", false);
    }

    public boolean canConvert(Class type) {
        return type.equals(boolean.class) || Boolean.class.isAssignableFrom(type);
    }

    public Object fromValue(String value) {
        if (caseSensitive) {
            return positive.equals(value) ? Boolean.TRUE : Boolean.FALSE;
        } else {
            return positive.equalsIgnoreCase(value) ? Boolean.TRUE : Boolean.FALSE;
        }
    }

    public String toValue(Object object) {
        return object == null ? null : ((Boolean) object ? positive : negative);
    }
}
