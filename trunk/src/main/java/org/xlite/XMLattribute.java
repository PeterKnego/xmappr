package org.xlite;

import org.xlite.converters.ValueConverter;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * User: peter
 * Date: Feb 17, 2008
 * Time: 4:28:09 PM
 */

@Target(value = {ElementType.FIELD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface XMLattribute {

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
     * ItemType must be used when a target field is a Collection. It determines what converter will be used.
     *
     * @return
     */
    Class itemType() default String.class;

    /**
     * Custom converter assigned to convert this field.
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
