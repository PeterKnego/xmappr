/*
 * This software is released under the BSD license. Full license available at http://xmappr.googlecode.com
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xmappr contributors
 * All rights reserved.
 */
package org.xmappr.annotation;

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
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RootElement {
    /**
     * The name of the root XML element that a class maps to.
     *
     * @return
     */
    String value() default "";

    /**
     * The name of the root XML element that a class maps to.
     *
     * @return
     */
    String name() default "";

    /**
     * Assigns custom converter to be used to convert this XML element.
     *
     * @return
     */
    Class<? extends Converter> converter() default ElementConverter.class;
}
