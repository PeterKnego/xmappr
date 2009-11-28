/*
 * This software is released under the BSD license. Full license available at http://xmappr.googlecode.com
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xmappr contributors
 * All rights reserved.
 */
package org.xmappr.converters;

/**
 * Used to get/set values of a Field. Implementations should keep a reference to a Field.
 * Values should be internally converted to/from Strings, using appropriate converters.
 *
 * @author peter
 */
public interface FieldConnector {

    /**
     * Sets a value of a Field.
     *
     * @param object       Instance of an Object that contains a Field.
     * @param elementValue Value to be set.
     */
    void setValue(Object object, String elementValue);

    /**
     * Gets a value of a Field.
     *
     * @param object Instance of an Object that contains a Field.
     * @return
     */
    String getValue(Object object);
}
