package org.xlite.converters;

/**
 * User: peter
 * Date: Mar 2, 2008
 * Time: 10:50:40 AM
 */
public interface ValueConverter {

    boolean canConvert(Class type);

    public Object fromValue(String value);

    public String toValue(Object object);
}
