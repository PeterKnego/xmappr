/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
package org.xlite;

import org.xlite.converters.ElementConverter;

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

    //todo Check handling of default values in all situations

    /**
     * Default value to be used when XML element is empty.
     *
     * @return
     */
    String defaultValue() default "";

    /**
     * ItemType is used to determine which converter will be used.
     * Either itemType or converter element must be defined when a target field is a Collection.
     *
     * @return
     */
    Class itemType() default Object.class;

    /**
     * Custom converter assigned to convert this XML element.
     * Either itemType or converter element must be defined when a target field is a Collection.
     *
     * @return
     */
    Class<? extends ElementConverter> converter() default ElementConverter.class;

    /**
     * Optional formatting string. Used by converter.
     *
     * @return
     */
    String format() default "";
}
