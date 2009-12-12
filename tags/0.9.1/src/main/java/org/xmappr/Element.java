/*
 * This software is released under the BSD license. Full license available at http://xmappr.googlecode.com
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xmappr contributors
 * All rights reserved.
 */
package org.xmappr;

import org.xmappr.converters.ElementConverter;
import org.xmappr.converters.Converter;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation used to map a field to XML subelements.
 */
@Target(value = {ElementType.FIELD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Element {

    /**
     * The name of the XML element that a field maps to.
     *
     * @return
     */
    String value() default "";

    /**
     * The name of the XML element that a field maps to.
     *
     * @return
     */
    String name() default "";

    /**
     * Default value to be used when XML element is empty.
     *
     * @return
     */
    String defaultValue() default "";

    /**
     * targetType is used to determine which converter will be used.
     * Either targetType or converter element must be defined when a target field is a Collection.
     *
     * @return
     */
    Class targetType() default Object.class;

    /**
     * Custom converter assigned to convert this XML element.
     * Either targetType or converter element must be defined when a target field is a Collection.
     *
     * @return
     */
    Class<? extends Converter> converter() default ElementConverter.class;

    /**
     * Optional formatting string. Used by converter.
     *
     * @return
     */
    String format() default "";
}
