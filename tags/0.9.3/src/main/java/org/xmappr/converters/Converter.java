/*
 * This software is released under the BSD license. Full license available at http://xmappr.googlecode.com
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xmappr contributors
 * All rights reserved.
 */
package org.xmappr.converters;

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
