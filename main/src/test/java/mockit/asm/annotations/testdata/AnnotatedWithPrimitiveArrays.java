/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.asm.annotations.testdata;

import java.lang.annotation.ElementType;

@ComplexAnnotation(intValues = { 1, 2, 3 }, boolValues = { true, false }, stringValues = { "a",
        "b" }, enumValue = ElementType.METHOD, nested = @SimpleAnnotation("inner"), classValue = String.class)
public final class AnnotatedWithPrimitiveArrays {
}
