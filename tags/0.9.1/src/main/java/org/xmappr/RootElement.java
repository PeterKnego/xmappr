/*
 * This software is released under the BSD license. Full license available at http://xmappr.googlecode.com
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xmappr contributors
 * All rights reserved.
 */
package org.xmappr;

import org.xmappr.converters.Converter;
import org.xmappr.converters.ElementConverter;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation used to define root class mapping.
 * Root class mapping maps a class at the top of the class hierarchy (root class) to a root XML element.
 */
@Target(value = {ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface RootElement {
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
     * Custom converter assigned to convert this XML element.
     * Either targetType or converter element must be defined when a target field is a Collection.
     *
     * @return
     */
    Class<? extends Converter> converter() default ElementConverter.class;
}
