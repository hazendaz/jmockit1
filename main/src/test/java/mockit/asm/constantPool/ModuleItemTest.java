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

final class ModuleItemTest {

    @Test
    void constructorWithIndexTypeAndValue() {
        ModuleItem item = new ModuleItem(1, ConstantPoolTypes.MODULE, "java.base");
        assertEquals(1, item.index);
        assertEquals("java.base", item.getValue());
    }

    @Test
    void defaultConstructorHasEmptyString() {
        ModuleItem item = new ModuleItem();
        assertEquals("", item.getValue());
    }

    @Test
    void copyConstructor() {
        ModuleItem original = new ModuleItem(1, ConstantPoolTypes.MODULE, "java.base");
        ModuleItem copy = new ModuleItem(2, original);
        assertEquals(2, copy.index);
        assertEquals("java.base", copy.getValue());
    }

    @Test
    void isEqualToWithSameValue() {
        ModuleItem item1 = new ModuleItem(1, ConstantPoolTypes.MODULE, "java.base");
        ModuleItem item2 = new ModuleItem(2, ConstantPoolTypes.MODULE, "java.base");
        assertTrue(item1.isEqualTo((Item) item2));
    }

    @Test
    void isEqualToWithDifferentValue() {
        ModuleItem item1 = new ModuleItem(1, ConstantPoolTypes.MODULE, "java.base");
        ModuleItem item2 = new ModuleItem(2, ConstantPoolTypes.MODULE, "java.se");
        assertFalse(item1.isEqualTo((Item) item2));
    }

    @Test
    void setUpdatesValue() {
        ModuleItem item = new ModuleItem(1, ConstantPoolTypes.MODULE, "java.base");
        item.set(ConstantPoolTypes.MODULE, "java.sql");
        assertEquals("java.sql", item.getValue());
    }
}
