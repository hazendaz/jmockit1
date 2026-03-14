/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import java.util.Spliterator;

import org.junit.jupiter.api.Test;

final class DefaultValuesTest {

    @Test
    void getReturnTypeDescExtracts() {
        assertEquals("V", DefaultValues.getReturnTypeDesc("()V"));
        assertEquals("I", DefaultValues.getReturnTypeDesc("(I)I"));
        assertEquals("Ljava/lang/String;", DefaultValues.getReturnTypeDesc("()Ljava/lang/String;"));
    }

    @Test
    void computeForTypeVoidReturnsNull() {
        assertNull(DefaultValues.computeForType("V"));
    }

    @Test
    void computeForTypePrimitiveInt() {
        assertEquals(0, DefaultValues.computeForType("I"));
    }

    @Test
    void computeForTypeBoolean() {
        assertEquals(Boolean.FALSE, DefaultValues.computeForType("Z"));
    }

    @Test
    void computeForTypeLong() {
        assertEquals(0L, DefaultValues.computeForType("J"));
    }

    @Test
    void computeForTypeDouble() {
        assertEquals(0.0, DefaultValues.computeForType("D"));
    }

    @Test
    void computeForTypeFloat() {
        assertEquals(0.0f, DefaultValues.computeForType("F"));
    }

    @Test
    void computeForTypeObjectReturnsNull() {
        // 'L' prefix for non-mapped types -> null
        assertNull(DefaultValues.computeForType("Ljava/lang/Object;"));
    }

    @Test
    void computeForTypeIntArray() {
        Object result = DefaultValues.computeForType("[I");
        assertNotNull(result);
        assertTrue(result instanceof int[]);
        assertEquals(0, ((int[]) result).length);
    }

    @Test
    void computeForTypeStringArray() {
        Object result = DefaultValues.computeForType("[Ljava/lang/String;");
        assertNotNull(result);
        assertTrue(result instanceof String[]);
    }

    @Test
    void computeForTypeMultiDimArray() {
        Object result = DefaultValues.computeForType("[[I");
        assertNotNull(result);
    }

    @Test
    void computeForClassArray() {
        Object result = DefaultValues.computeForType(int[].class);
        assertNotNull(result);
        assertArrayEquals(new int[0], (int[]) result);
    }

    @Test
    void computeForClassPrimitive() {
        assertEquals(0, DefaultValues.computeForType(int.class));
    }

    @Test
    void computeForClassVoid() {
        assertNull(DefaultValues.computeForType(void.class));
    }

    @Test
    void computeForClassWrapper() {
        assertEquals(0, DefaultValues.computeForType(Integer.class));
    }

    @Test
    void defaultValueForPrimitiveBoolean() {
        assertEquals(Boolean.FALSE, DefaultValues.defaultValueForPrimitiveType(boolean.class));
    }

    @Test
    void defaultValueForPrimitiveLong() {
        assertEquals(0L, DefaultValues.defaultValueForPrimitiveType(long.class));
    }

    @Test
    void defaultValueForPrimitiveDouble() {
        assertEquals(0.0, DefaultValues.defaultValueForPrimitiveType(double.class));
    }

    @Test
    void defaultValueForPrimitiveFloat() {
        Object result = DefaultValues.defaultValueForPrimitiveType(float.class);
        assertEquals(0.0f, (Float) result, 0.0f);
    }

    @Test
    void defaultValueForPrimitiveChar() {
        assertEquals('\0', DefaultValues.defaultValueForPrimitiveType(char.class));
    }

    @Test
    void defaultValueForPrimitiveByte() {
        Object result = DefaultValues.defaultValueForPrimitiveType(byte.class);
        assertEquals(0, ((Byte) result).intValue());
    }

    @Test
    void defaultValueForPrimitiveShort() {
        Object result = DefaultValues.defaultValueForPrimitiveType(short.class);
        assertEquals(0, ((Short) result).intValue());
    }

    @Test
    void computeForWrapperBoolean() {
        assertEquals(Boolean.FALSE, DefaultValues.computeForWrapperType(Boolean.class));
    }

    @Test
    void computeForWrapperLong() {
        Object result = DefaultValues.computeForWrapperType(Long.class);
        assertEquals(0L, ((Long) result).longValue());
    }

    @Test
    void computeForWrapperDouble() {
        assertEquals(0.0, DefaultValues.computeForWrapperType(Double.class));
    }

    @Test
    void computeForWrapperFloat() {
        Object result = DefaultValues.computeForWrapperType(Float.class);
        assertEquals(0.0f, (Float) result, 0.0f);
    }

    @Test
    void computeForWrapperCharacter() {
        Object result = DefaultValues.computeForWrapperType(Character.class);
        assertEquals('\0', ((Character) result).charValue());
    }

    @Test
    void computeForWrapperByte() {
        Object result = DefaultValues.computeForWrapperType(Byte.class);
        assertEquals(0, ((Byte) result).intValue());
    }

    @Test
    void computeForWrapperShort() {
        Object result = DefaultValues.computeForWrapperType(Short.class);
        assertEquals(0, ((Short) result).intValue());
    }

    @Test
    void computeForWrapperUnknownReturnsNull() {
        assertNull(DefaultValues.computeForWrapperType(Object.class));
    }

    @Test
    void computeForReturnType() {
        assertNull(DefaultValues.computeForReturnType("doSomething()V"));
        assertEquals(0, DefaultValues.computeForReturnType("getValue()I"));
    }

    @Test
    void primitiveIteratorOfIntBehavior() {
        // Retrieve PrimitiveIterator.OfInt from the map
        Object result = DefaultValues.computeForType("Ljava/util/PrimitiveIterator$OfInt;");
        assertNotNull(result);
        assertTrue(result instanceof PrimitiveIterator.OfInt);
        PrimitiveIterator.OfInt iter = (PrimitiveIterator.OfInt) result;
        assertFalse(iter.hasNext());
        assertThrowsNoSuchElement(iter::nextInt);
        assertThrowsNoSuchElement(iter::next);
    }

    @Test
    void primitiveIteratorOfLongBehavior() {
        Object result = DefaultValues.computeForType("Ljava/util/PrimitiveIterator$OfLong;");
        assertNotNull(result);
        assertTrue(result instanceof PrimitiveIterator.OfLong);
        PrimitiveIterator.OfLong iter = (PrimitiveIterator.OfLong) result;
        assertFalse(iter.hasNext());
        assertThrowsNoSuchElement(iter::nextLong);
    }

    @Test
    void primitiveIteratorOfDoubleBehavior() {
        Object result = DefaultValues.computeForType("Ljava/util/PrimitiveIterator$OfDouble;");
        assertNotNull(result);
        assertTrue(result instanceof PrimitiveIterator.OfDouble);
        PrimitiveIterator.OfDouble iter = (PrimitiveIterator.OfDouble) result;
        assertFalse(iter.hasNext());
        assertThrowsNoSuchElement(iter::nextDouble);
    }

    @Test
    void computeForTypeOptional() {
        Object opt = DefaultValues.computeForType("Ljava/util/Optional;");
        assertNotNull(opt);
    }

    @Test
    void computeForTypeSpliterator() {
        Object spliterator = DefaultValues.computeForType("Ljava/util/Spliterator;");
        assertNotNull(spliterator);
        assertTrue(spliterator instanceof Spliterator);
    }

    @Test
    void computeForTypeStream() {
        Object stream = DefaultValues.computeForType("Ljava/util/stream/Stream;");
        assertNotNull(stream);
    }

    private static void assertThrowsNoSuchElement(Runnable action) {
        try {
            action.run();
            throw new AssertionError("Expected NoSuchElementException");
        } catch (NoSuchElementException e) {
            // expected
        }
    }
}
