/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.asm.constantPool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests covering copy constructors and set methods for numeric constant pool items.
 */
final class NumericItemTest {

    @Test
    void longItemCopyConstructor() {
        LongItem original = new LongItem(1);
        original.setValue(123456L);

        LongItem copy = new LongItem(2, original);
        assertEquals(2, copy.index);
        assertEquals(123456L, copy.longVal);
    }

    @Test
    void doubleItemCopyConstructor() {
        DoubleItem original = new DoubleItem(1);
        original.set(3.14);

        DoubleItem copy = new DoubleItem(2, original);
        assertEquals(2, copy.index);
        assertEquals(original.longVal, copy.longVal);
    }

    @Test
    void doubleItemSet() {
        DoubleItem item = new DoubleItem(1);
        item.set(2.718);
        assertTrue(item.longVal != 0);
    }

    @Test
    void floatItemCopyConstructor() {
        FloatItem original = new FloatItem(1);
        original.set(3.14f);

        FloatItem copy = new FloatItem(2, original);
        assertEquals(2, copy.index);
        assertEquals(original.intVal, copy.intVal);
    }

    @Test
    void floatItemSet() {
        FloatItem item = new FloatItem(1);
        item.set(1.5f);
        assertTrue(item.intVal != 0);
    }
}
