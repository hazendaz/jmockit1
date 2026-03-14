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

final class PackageItemTest {

    @Test
    void constructorWithIndexTypeAndValue() {
        PackageItem item = new PackageItem(1, ConstantPoolTypes.PACKAGE, "java/lang");
        assertEquals(1, item.index);
        assertEquals("java/lang", item.getValue());
    }

    @Test
    void defaultConstructorHasEmptyString() {
        PackageItem item = new PackageItem();
        assertEquals("", item.getValue());
    }

    @Test
    void copyConstructor() {
        PackageItem original = new PackageItem(1, ConstantPoolTypes.PACKAGE, "java/lang");
        PackageItem copy = new PackageItem(2, original);
        assertEquals(2, copy.index);
        assertEquals("java/lang", copy.getValue());
    }

    @Test
    void isEqualToWithSameValue() {
        PackageItem item1 = new PackageItem(1, ConstantPoolTypes.PACKAGE, "java/lang");
        PackageItem item2 = new PackageItem(2, ConstantPoolTypes.PACKAGE, "java/lang");
        assertTrue(item1.isEqualTo((Item) item2));
    }

    @Test
    void isEqualToWithDifferentValue() {
        PackageItem item1 = new PackageItem(1, ConstantPoolTypes.PACKAGE, "java/lang");
        PackageItem item2 = new PackageItem(2, ConstantPoolTypes.PACKAGE, "java/util");
        assertFalse(item1.isEqualTo((Item) item2));
    }

    @Test
    void setUpdatesValue() {
        PackageItem item = new PackageItem(1, ConstantPoolTypes.PACKAGE, "java/lang");
        item.set(ConstantPoolTypes.PACKAGE, "java/util");
        assertEquals("java/util", item.getValue());
    }
}
