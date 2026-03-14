/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.reflection;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;

import mockit.Invocation;

import org.junit.jupiter.api.Test;

final class ParameterReflectionTest {

    @Test
    void getParameterTypesDescriptionWithNoParams() {
        String desc = ParameterReflection.getParameterTypesDescription(new Class<?>[0]);
        assertEquals("()", desc);
    }

    @Test
    void getParameterTypesDescriptionWithSingleParam() {
        String desc = ParameterReflection.getParameterTypesDescription(new Class<?>[] { String.class });
        assertEquals("(String)", desc);
    }

    @Test
    void getParameterTypesDescriptionWithMultipleParams() {
        String desc = ParameterReflection.getParameterTypesDescription(new Class<?>[] { int.class, String.class });
        assertEquals("(int, String)", desc);
    }

    @Test
    void getArgumentTypesFromArgumentValuesWithNoArgs() {
        Class<?>[] types = ParameterReflection.getArgumentTypesFromArgumentValues();
        assertArrayEquals(ParameterReflection.NO_PARAMETERS, types);
    }

    @Test
    void getArgumentTypesFromArgumentValuesWithClassArg() {
        // When arg is a Class, it represents the type (and the value becomes null)
        Object[] args = { String.class };
        Class<?>[] types = ParameterReflection.getArgumentTypesFromArgumentValues(args);
        assertEquals(1, types.length);
        assertEquals(String.class, types[0]);
        // arg should have been set to null
        assertArrayEquals(new Object[] { null }, args);
    }

    @Test
    void getArgumentTypesFromArgumentValuesWithObjectArg() {
        Class<?>[] types = ParameterReflection.getArgumentTypesFromArgumentValues("hello");
        assertEquals(1, types.length);
        assertEquals(String.class, types[0]);
    }

    @Test
    void getArgumentTypesFromArgumentValuesThrowsForNullArg() {
        assertThrows(IllegalArgumentException.class,
                () -> ParameterReflection.getArgumentTypesFromArgumentValues(new Object[] { null }));
    }

    @Test
    void argumentsWithExtraFirstValue() {
        Object[] result = ParameterReflection.argumentsWithExtraFirstValue(new Object[] { "b", "c" }, "a");
        assertArrayEquals(new Object[] { "a", "b", "c" }, result);
    }

    @Test
    void hasMoreSpecificTypesReturnsTrueWhenMoreSpecific() {
        // String is more specific than Object
        boolean result = ParameterReflection.hasMoreSpecificTypes(new Class<?>[] { String.class },
                new Class<?>[] { Object.class });
        assertTrue(result);
    }

    @Test
    void hasMoreSpecificTypesReturnsFalseWhenSame() {
        boolean result = ParameterReflection.hasMoreSpecificTypes(new Class<?>[] { String.class },
                new Class<?>[] { String.class });
        assertFalse(result);
    }

    @Test
    void hasMoreSpecificTypesWithPrimitiveTypes() {
        // int wrapped to Integer, same as Integer - not more specific
        boolean result = ParameterReflection.hasMoreSpecificTypes(new Class<?>[] { int.class },
                new Class<?>[] { int.class });
        assertFalse(result);
    }

    @Test
    void acceptsArgumentTypesWithExactMatch() {
        boolean result = ParameterReflection.acceptsArgumentTypes(new Class<?>[] { String.class },
                new Class<?>[] { String.class }, 0);
        assertTrue(result);
    }

    @Test
    void acceptsArgumentTypesWithAutoboxing() {
        boolean result = ParameterReflection.acceptsArgumentTypes(new Class<?>[] { int.class },
                new Class<?>[] { Integer.class }, 0);
        assertTrue(result);
    }

    @Test
    void acceptsArgumentTypesWithSubclass() {
        // StringBuilder is assignable from CharSequence
        boolean result = ParameterReflection.acceptsArgumentTypes(new Class<?>[] { CharSequence.class },
                new Class<?>[] { String.class }, 0);
        assertTrue(result);
    }

    @Test
    void acceptsArgumentTypesReturnsFalseForIncompatible() {
        boolean result = ParameterReflection.acceptsArgumentTypes(new Class<?>[] { String.class },
                new Class<?>[] { Integer.class }, 0);
        assertFalse(result);
    }

    @Test
    void isSameTypeIgnoringAutoBoxingWithSameType() {
        assertTrue(ParameterReflection.isSameTypeIgnoringAutoBoxing(String.class, String.class));
    }

    @Test
    void isSameTypeIgnoringAutoBoxingWithPrimitiveAndWrapper() {
        assertTrue(ParameterReflection.isSameTypeIgnoringAutoBoxing(int.class, Integer.class));
        assertTrue(ParameterReflection.isSameTypeIgnoringAutoBoxing(Integer.class, int.class));
    }

    @Test
    void isSameTypeIgnoringAutoBoxingWithDifferentTypes() {
        assertFalse(ParameterReflection.isSameTypeIgnoringAutoBoxing(String.class, Integer.class));
    }

    @Test
    void indexOfFirstRealParameterWithInvocationFirst() {
        int result = ParameterReflection.indexOfFirstRealParameter(new Class<?>[] { Invocation.class, String.class },
                new Class<?>[] { String.class });
        assertEquals(1, result);
    }

    @Test
    void indexOfFirstRealParameterWithNoExtra() {
        int result = ParameterReflection.indexOfFirstRealParameter(new Class<?>[] { String.class },
                new Class<?>[] { String.class });
        assertEquals(0, result);
    }

    @Test
    void indexOfFirstRealParameterWithWrongExtraType() {
        // Extra param but not Invocation -> -1
        int result = ParameterReflection.indexOfFirstRealParameter(new Class<?>[] { String.class, Integer.class },
                new Class<?>[] { Integer.class });
        assertEquals(-1, result);
    }

    @Test
    void indexOfFirstRealParameterWithTooManyExtras() {
        // 2 extra params -> -1
        int result = ParameterReflection.indexOfFirstRealParameter(
                new Class<?>[] { Invocation.class, String.class, Integer.class }, new Class<?>[] { Integer.class });
        assertEquals(-1, result);
    }

    @Test
    void matchesParameterTypesWithExactMatch() {
        assertTrue(ParameterReflection.matchesParameterTypes(new Class<?>[] { String.class },
                new Class<?>[] { String.class }, 0));
    }

    @Test
    void matchesParameterTypesReturnsFalseForMismatch() {
        assertFalse(ParameterReflection.matchesParameterTypes(new Class<?>[] { String.class },
                new Class<?>[] { Integer.class }, 0));
    }

    @Test
    void getParameterCountReturnsCorrectCount() throws NoSuchMethodException {
        Method method = String.class.getMethod("substring", int.class, int.class);
        int count = ParameterReflection.getParameterCount(method);
        assertEquals(2, count);
    }

    @Test
    void noParametersConstantIsEmpty() {
        assertNotNull(ParameterReflection.NO_PARAMETERS);
        assertEquals(0, ParameterReflection.NO_PARAMETERS.length);
    }
}
