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
public @interface XMLtext {
    Class<? extends ValueConverter> converter() default ValueConverter.class;
}