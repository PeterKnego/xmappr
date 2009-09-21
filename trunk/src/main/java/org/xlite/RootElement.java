/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
package org.xlite;

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
}
