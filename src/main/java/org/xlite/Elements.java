package org.xlite;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author peter
 */

/**
 * Annotation used to group multiple Element annotation together.
 * Used when multiple Element annotations must be defined on a field.
 */
@Target(value = {ElementType.FIELD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Elements {
    /**
     * An array of Element annotations.
     *
     * @return
     */
    Element[] value();
}
