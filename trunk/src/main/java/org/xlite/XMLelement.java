package org.xlite;

import org.xlite.converters.ElementConverter;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * User: peter
 * Date: Feb 17, 2008
 * Time: 4:28:09 PM
 */

//todo Write extensive javadoc!!!!

@Target(value = {ElementType.FIELD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface XMLelement {

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
     * ItemType must be used when a target field is a Collection. It determines what converter will be used.
     *
     * @return
     */
    Class itemType() default Object.class;

    /**
     * Custom converter assigned to convert this field.
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
