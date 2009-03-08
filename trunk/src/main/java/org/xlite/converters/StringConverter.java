package org.xlite.converters;

/**
 *
 * User: peter
 * Date: Mar 2, 2008
 * Time: 10:02:25 PM
 */
public class StringConverter extends ValueConverter {

    public boolean canConvert(Class type) {
        return String.class.isAssignableFrom(type);
    }

    public Object fromValue(String value) {
        return value.intern();
    }

    public String toValue(Object object) {
        return (String) object;
    }

    @Override
    public boolean convertsEmpty() {
        return true;
    }
}
