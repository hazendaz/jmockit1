/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.asm.constantPool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class LongValueItemTest {

    @Test
    void constructorWithIndex() {
        LongValueItem item = new LongValueItem(1);
        assertEquals(1, item.index);
        assertEquals(0L, item.longVal);
    }

    @Test
    void copyConstructor() {
        LongValueItem original = new LongValueItem(1);
        original.setValue(12345L);

        LongValueItem copy = new LongValueItem(2, original);
        assertEquals(2, copy.index);
        assertEquals(12345L, copy.longVal);
    }

    @Test
    void setValueUpdatesLongVal() {
        LongValueItem item = new LongValueItem(1);
        item.setValue(9876543210L);
        assertEquals(9876543210L, item.longVal);
    }

    @Test
    void isEqualToWithSameValue() {
        LongValueItem item1 = new LongValueItem(1);
        item1.setValue(42L);

        LongValueItem item2 = new LongValueItem(2);
        item2.setValue(42L);

        assertTrue(item1.isEqualTo(item2));
    }

    @Test
    void isEqualToWithDifferentValue() {
        LongValueItem item1 = new LongValueItem(1);
        item1.setValue(42L);

        LongValueItem item2 = new LongValueItem(2);
        item2.setValue(99L);

        assertFalse(item1.isEqualTo(item2));
    }
}
