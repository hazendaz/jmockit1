/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package integration.tests;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * The Interface AnAnnotation.
 */
@Target({ FIELD, METHOD, TYPE, TYPE_USE })
@Retention(RUNTIME)
public @interface AnAnnotation {

    /**
     * Integers.
     *
     * @return the int[]
     */
    int[] integers() default {};

    /**
     * Value.
     *
     * @return the string
     */
    String value() default "test";
}
