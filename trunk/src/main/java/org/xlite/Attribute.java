/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
package org.xlite;

import org.xlite.converters.ValueConverter;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation used to map a field to XML attribute.
 */
@Target(value = {ElementType.FIELD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Attribute {

    /**
     * The name of the XML attribute that a field maps to.
     *
     * @return
     */
    String value() default "";

    /**
     * The name of the XML attribute that a field maps to.
     *
     * @return
     */
    String name() default "";

    //todo Check if default values are used on Attributes?

    /**
     * Default value to be used when XML attribute is empty.
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
    Class itemType() default String.class;

    /**
     * Custom converter assigned to convert this XML attribute.
     * Either itemType or converter element must be defined when a target field is a Collection.
     *
     * @return
     */
    Class<? extends ValueConverter> converter() default ValueConverter.class;

    /**
     * Optional formatting string. Used by converter.
     *
     * @return
     */
    String format() default "";
}
