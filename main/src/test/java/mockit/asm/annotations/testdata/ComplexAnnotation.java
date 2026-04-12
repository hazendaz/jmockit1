/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.asm.annotations.testdata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ComplexAnnotation {

    int[] intValues() default {};

    boolean[] boolValues() default {};

    String[] stringValues() default {};

    ElementType enumValue() default ElementType.TYPE;

    SimpleAnnotation nested() default @SimpleAnnotation("default");

    Class<?> classValue() default Object.class;
}
