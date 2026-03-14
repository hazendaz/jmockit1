/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.asm.constantPool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import mockit.asm.jvmConstants.ConstantPoolTypes;

import org.junit.jupiter.api.Test;

final class DynamicItemTest {

    @Test
    void constructorWithIndex() {
        DynamicItem item = new DynamicItem(1);
        assertEquals(1, item.index);
    }

    @Test
    void copyConstructor() {
        DynamicItem original = new DynamicItem(1);
        original.set(ConstantPoolTypes.INVOKE_DYNAMIC, "myMethod", "(I)V", 3);

        DynamicItem copy = new DynamicItem(2, original);
        assertEquals(2, copy.index);
        assertEquals(3, copy.bsmIndex);
        assertEquals("myMethod", copy.name);
        assertEquals("(I)V", copy.desc);
    }

    @Test
    void setUpdatesAllFields() {
        DynamicItem item = new DynamicItem(1);
        item.set(ConstantPoolTypes.INVOKE_DYNAMIC, "method", "(Ljava/lang/String;)I", 5);

        assertEquals("method", item.name);
        assertEquals("(Ljava/lang/String;)I", item.desc);
        assertEquals(5, item.bsmIndex);
    }

    @Test
    void isEqualToWithSameValues() {
        DynamicItem item1 = new DynamicItem(1);
        item1.set(ConstantPoolTypes.INVOKE_DYNAMIC, "method", "(I)V", 2);

        DynamicItem item2 = new DynamicItem(3);
        item2.set(ConstantPoolTypes.INVOKE_DYNAMIC, "method", "(I)V", 2);

        // Cast to Item to invoke DynamicItem.isEqualTo(Item)
        assertTrue(item1.isEqualTo((Item) item2));
    }

    @Test
    void isEqualToWithDifferentBsmIndex() {
        DynamicItem item1 = new DynamicItem(1);
        item1.set(ConstantPoolTypes.INVOKE_DYNAMIC, "method", "(I)V", 2);

        DynamicItem item2 = new DynamicItem(3);
        item2.set(ConstantPoolTypes.INVOKE_DYNAMIC, "method", "(I)V", 5);

        // Cast to Item to invoke DynamicItem.isEqualTo(Item) which checks bsmIndex
        assertFalse(item1.isEqualTo((Item) item2));
    }

    @Test
    void isEqualToWithDifferentName() {
        DynamicItem item1 = new DynamicItem(1);
        item1.set(ConstantPoolTypes.INVOKE_DYNAMIC, "method1", "(I)V", 2);

        DynamicItem item2 = new DynamicItem(3);
        item2.set(ConstantPoolTypes.INVOKE_DYNAMIC, "method2", "(I)V", 2);

        assertFalse(item1.isEqualTo((Item) item2));
    }
}
