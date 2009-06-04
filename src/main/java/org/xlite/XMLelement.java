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
    String value() default "";
    String name() default "";
    String defaultValue() default "";
    Class itemType() default Object.class;
//    boolean anyName() default false;

    Class<? extends ElementConverter> converter() default ElementConverter.class;
}
