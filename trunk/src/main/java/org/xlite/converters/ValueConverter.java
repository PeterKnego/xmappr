package org.xlite.converters;

/**
 * User: peter
 * Date: Mar 2, 2008
 * Time: 10:50:40 AM
 */
public abstract class ValueConverter {

    public abstract boolean canConvert(Class type);

    public abstract Object fromValue(String value);

    public abstract String toValue(Object object);

    public boolean convertsEmpty(){
        return false;
    }
}
