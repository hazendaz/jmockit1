/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.reflection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.jupiter.api.Test;

final class AnnotationReflectionTest {

    @Retention(RetentionPolicy.RUNTIME)
    @interface SampleAnnotation {
        String value() default "test";

        String name() default "sample";
    }

    @SampleAnnotation("hello")
    static void annotatedMethod() {
    }

    @Test
    void readAnnotationAttributeReturnsValue() throws NoSuchMethodException {
        SampleAnnotation annotation = AnnotationReflectionTest.class.getDeclaredMethod("annotatedMethod")
                .getAnnotation(SampleAnnotation.class);
        String value = AnnotationReflection.readAnnotationAttribute(annotation, "value");
        assertEquals("hello", value);
    }

    @Test
    void readAnnotationAttributeThrowsForMissingAttribute() throws NoSuchMethodException {
        SampleAnnotation annotation = AnnotationReflectionTest.class.getDeclaredMethod("annotatedMethod")
                .getAnnotation(SampleAnnotation.class);
        assertThrows(RuntimeException.class,
                () -> AnnotationReflection.readAnnotationAttribute(annotation, "nonExistent"));
    }

    @Test
    void readAnnotationAttributeIfAvailableReturnsValue() throws NoSuchMethodException {
        SampleAnnotation annotation = AnnotationReflectionTest.class.getDeclaredMethod("annotatedMethod")
                .getAnnotation(SampleAnnotation.class);
        String value = AnnotationReflection.readAnnotationAttributeIfAvailable(annotation, "value");
        assertEquals("hello", value);
    }

    @Test
    void readAnnotationAttributeIfAvailableReturnsNullForMissingAttribute() throws NoSuchMethodException {
        SampleAnnotation annotation = AnnotationReflectionTest.class.getDeclaredMethod("annotatedMethod")
                .getAnnotation(SampleAnnotation.class);
        String value = AnnotationReflection.readAnnotationAttributeIfAvailable(annotation, "nonExistent");
        assertNull(value);
    }

    @Test
    void readAnnotationAttributeDefaultValue() throws NoSuchMethodException {
        SampleAnnotation annotation = AnnotationReflectionTest.class.getDeclaredMethod("annotatedMethod")
                .getAnnotation(SampleAnnotation.class);
        String name = AnnotationReflection.readAnnotationAttribute(annotation, "name");
        assertEquals("sample", name);
    }
}
