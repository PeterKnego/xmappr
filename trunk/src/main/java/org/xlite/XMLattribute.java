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
    String value() default "";
    String name() default "";
    String defaultValue() default "";
    Class itemType() default String.class;
    
    Class<? extends ValueConverter> converter() default ValueConverter.class;
}
