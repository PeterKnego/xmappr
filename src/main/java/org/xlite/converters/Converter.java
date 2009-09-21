/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
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
