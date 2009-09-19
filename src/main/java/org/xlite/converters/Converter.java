package org.xlite.converters;

/**
 * Core converter interface implemented by ValueConverter and ElementConverters
 */
public interface Converter {
    /**
     * Indicates whether an implementation of Converter can convert given Class.
     *
     * @param type
     * @return
     */
    public boolean canConvert(Class type);
}
