package org.xlite.converters;

/**
 * User: peter
 * Date: Mar 2, 2008
 * Time: 10:50:40 AM
 */
public abstract class ValueConverter {

    public abstract boolean canConvert(Class type);

    public abstract Object fromValue(String value, String format);

    public abstract String toValue(Object object, String format);

    public boolean convertsEmpty(){
        return false;
    }
}
