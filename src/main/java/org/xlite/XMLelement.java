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

@Target(value = {ElementType.FIELD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface XMLelement {
    String value() default "";
    String name() default "";
    String defaultValue() default "";
    Class initializeType() default Object.class;
    Class itemType() default Object.class;

    Class<? extends ElementConverter> converter() default ElementConverter.class;
}
