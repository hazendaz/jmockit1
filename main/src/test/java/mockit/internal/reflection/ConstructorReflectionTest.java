/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.reflection;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class ConstructorReflectionTest {

    static class SimpleClass {
        final int value;

        public SimpleClass() {
            value = 0;
        }

        public SimpleClass(int value) {
            this.value = value;
        }

        public SimpleClass(String str) {
            this.value = Integer.parseInt(str);
        }
    }

    static class NoDefaultConstructor {
        final int value;

        NoDefaultConstructor(int value) {
            this.value = value;
        }
    }

    static class ThrowingConstructor {
        ThrowingConstructor() {
            throw new RuntimeException("constructor failed");
        }
    }

    static class ThrowingCheckedConstructor {
        ThrowingCheckedConstructor(String s) throws Exception {
            throw new Exception("checked: " + s);
        }
    }

    @Test
    void findSpecifiedConstructorWithMatchingTypes() {
        var ctor = ConstructorReflection.findSpecifiedConstructor(SimpleClass.class, new Class<?>[] { int.class });
        assertNotNull(ctor);
    }

    @Test
    void findSpecifiedConstructorWithStringType() {
        var ctor = ConstructorReflection.findSpecifiedConstructor(SimpleClass.class, new Class<?>[] { String.class });
        assertNotNull(ctor);
    }

    @Test
    void findSpecifiedConstructorWithCheckedException() {
        var ctor = ConstructorReflection.findSpecifiedConstructor(
            ThrowingCheckedConstructor.class, new Class<?>[] { String.class }
        );
        assertNotNull(ctor);
    }

    @Test
    void findSpecifiedConstructorNotFound() {
        assertThrows(IllegalArgumentException.class, () -> ConstructorReflection
                .findSpecifiedConstructor(SimpleClass.class, new Class<?>[] { double.class }));
    }

    @Test
    void newInstanceUsingDefaultConstructorCreatesInstance() {
        SimpleClass instance = ConstructorReflection.newInstanceUsingDefaultConstructor(SimpleClass.class);
        assertNotNull(instance);
    }

    @Test
    void newInstanceUsingDefaultConstructorThrowsForMissingConstructor() {
        assertThrows(RuntimeException.class,
                () -> ConstructorReflection.newInstanceUsingDefaultConstructor(NoDefaultConstructor.class));
    }

    @Test
    void newInstanceUsingDefaultConstructorThrowsForThrowingConstructor() {
        assertThrows(RuntimeException.class,
                () -> ConstructorReflection.newInstanceUsingDefaultConstructor(ThrowingConstructor.class));
    }

    @Test
    void newInstanceUsingDefaultConstructorIfAvailableReturnsInstanceWhenExists() {
        SimpleClass instance = ConstructorReflection.newInstanceUsingDefaultConstructorIfAvailable(SimpleClass.class);
        assertNotNull(instance);
    }

    @Test
    void newInstanceUsingDefaultConstructorIfAvailableReturnsNullWhenNoDefault() {
        NoDefaultConstructor instance = ConstructorReflection
                .newInstanceUsingDefaultConstructorIfAvailable(NoDefaultConstructor.class);
        assertNull(instance);
    }

    @Test
    void newInstanceUsingPublicConstructorIfAvailableReturnsInstanceWhenExists() {
        SimpleClass instance = ConstructorReflection.newInstanceUsingPublicConstructorIfAvailable(SimpleClass.class,
                new Class<?>[] { int.class }, 42);
        assertNotNull(instance);
    }

    @Test
    void newInstanceUsingPublicConstructorIfAvailableReturnsNullWhenNotFound() {
        SimpleClass instance = ConstructorReflection.newInstanceUsingPublicConstructorIfAvailable(SimpleClass.class,
                new Class<?>[] { double.class }, 3.14);
        assertNull(instance);
    }

    @Test
    void newInstanceUsingPublicDefaultConstructorCreatesInstance() {
        SimpleClass instance = ConstructorReflection.newInstanceUsingPublicDefaultConstructor(SimpleClass.class);
        assertNotNull(instance);
    }

    @Test
    void newInstanceUsingPublicDefaultConstructorThrowsWhenNoDefault() {
        assertThrows(RuntimeException.class,
                () -> ConstructorReflection.newInstanceUsingPublicDefaultConstructor(NoDefaultConstructor.class));
    }

    @Test
    void newUninitializedInstanceCreatesInstance() {
        NoDefaultConstructor instance = ConstructorReflection.newUninitializedInstance(NoDefaultConstructor.class);
        assertNotNull(instance);
    }

    @Test
    void newInstanceUsingCompatibleConstructorCreatesInstance() throws ReflectiveOperationException {
        ConstructorReflection.newInstanceUsingCompatibleConstructor(SimpleClass.class, "42");
        // No exception means success
        assertTrue(true);
    }

    @Test
    void newInstanceUsingCompatibleConstructorThrowsForMissingStringCtor() {
        assertThrows(ReflectiveOperationException.class,
                () -> ConstructorReflection.newInstanceUsingCompatibleConstructor(NoDefaultConstructor.class, "x"));
    }
}
