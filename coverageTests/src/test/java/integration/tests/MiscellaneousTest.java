/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package integration.tests;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.beans.ConstructorProperties;
import java.lang.annotation.Retention;
import java.lang.reflect.Constructor;

import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;

import org.junit.jupiter.api.Test;

class MiscellaneousTest {
    @Test
    void methodWithIINCWideInstruction() {
        int i = 0;
        i += 1000; // compiled to opcode iinc_w
        assert i == 1000;
    }

    @Retention(RUNTIME)
    public @interface Dummy {
        Class<?> value();
    }

    @Dummy(String.class)
    static class AnnotatedClass {
    }

    @Test
    void havingAnnotationWithClassValue(@Injectable AnnotatedClass dummy) {
        assertNotNull(dummy);
    }

    @Test
    void verifyAnnotationsArePreserved() throws Exception {
        Constructor<ClassWithAnnotations> constructor = ClassWithAnnotations.class.getDeclaredConstructor();

        assertTrue(constructor.isAnnotationPresent(ConstructorProperties.class));
    }

    @Test
    void mockingAnAnnotation(@Tested @Mocked AnAnnotation mockedAnnotation) {
        assertNull(mockedAnnotation.value());
    }
}
