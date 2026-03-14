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

final class BootstrapMethodItemTest {

    @Test
    void constructorSetsPositionAndIndex() {
        BootstrapMethodItem item = new BootstrapMethodItem(5, 100, 42);
        assertEquals(5, item.index);
        assertEquals(100, item.position);
    }

    @Test
    void isEqualToWithSamePosition() {
        BootstrapMethodItem item1 = new BootstrapMethodItem(1, 100, 42);
        BootstrapMethodItem item2 = new BootstrapMethodItem(2, 100, 42);
        assertTrue(item1.isEqualTo(item2));
    }

    @Test
    void isEqualToWithDifferentPosition() {
        BootstrapMethodItem item1 = new BootstrapMethodItem(1, 100, 42);
        BootstrapMethodItem item2 = new BootstrapMethodItem(2, 200, 42);
        assertFalse(item1.isEqualTo(item2));
    }
}
