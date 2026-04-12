/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.asm.constantPool;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import mockit.asm.types.JavaType;
import mockit.asm.types.MethodType;
import mockit.asm.types.ObjectType;
import mockit.asm.types.PrimitiveType;
import mockit.asm.util.MethodHandle;

import org.junit.jupiter.api.Test;

final class ConstantPoolGenerationTest {

    @Test
    void newUTF8AddsStringToPool() {
        ConstantPoolGeneration cp = new ConstantPoolGeneration();
        int index = cp.newUTF8("hello");
        assertTrue(index > 0);
    }

    @Test
    void newUTF8ReturnsSameIndexForDuplicate() {
        ConstantPoolGeneration cp = new ConstantPoolGeneration();
        int index1 = cp.newUTF8("hello");
        int index2 = cp.newUTF8("hello");
        assertTrue(index1 == index2);
    }

    @Test
    void newClassAddsClassToPool() {
        ConstantPoolGeneration cp = new ConstantPoolGeneration();
        int index = cp.newClass("java/lang/String");
        assertTrue(index > 0);
    }

    @Test
    void newClassItemReturnsStringItem() {
        ConstantPoolGeneration cp = new ConstantPoolGeneration();
        StringItem item = cp.newClassItem("java/lang/Object");
        assertNotNull(item);
        assertTrue(item.index > 0);
    }

    @Test
    void newIntegerAddsIntToPool() {
        ConstantPoolGeneration cp = new ConstantPoolGeneration();
        IntItem item = cp.newInteger(42);
        assertNotNull(item);
        assertTrue(item.index > 0);
    }

    @Test
    void newFloatAddsFloatToPool() {
        ConstantPoolGeneration cp = new ConstantPoolGeneration();
        FloatItem item = cp.newFloat(3.14f);
        assertNotNull(item);
        assertTrue(item.index > 0);
    }

    @Test
    void newLongAddsLongToPool() {
        ConstantPoolGeneration cp = new ConstantPoolGeneration();
        LongItem item = cp.newLong(100L);
        assertNotNull(item);
        assertTrue(item.index > 0);
    }

    @Test
    void newDoubleAddsDoubleToPool() {
        ConstantPoolGeneration cp = new ConstantPoolGeneration();
        DoubleItem item = cp.newDouble(2.718);
        assertNotNull(item);
        assertTrue(item.index > 0);
    }

    @Test
    void newFieldItemAddsFieldRefToPool() {
        ConstantPoolGeneration cp = new ConstantPoolGeneration();
        ClassMemberItem item = cp.newFieldItem("java/lang/System", "out", "Ljava/io/PrintStream;");
        assertNotNull(item);
        assertTrue(item.index > 0);
    }

    @Test
    void newMethodItemAddsMethodRefToPool() {
        ConstantPoolGeneration cp = new ConstantPoolGeneration();
        ClassMemberItem item = cp.newMethodItem("java/lang/String", "length", "()I", false);
        assertNotNull(item);
        assertTrue(item.index > 0);
    }

    @Test
    void newMethodItemForInterfaceAddsIMethodRef() {
        ConstantPoolGeneration cp = new ConstantPoolGeneration();
        ClassMemberItem item = cp.newMethodItem("java/util/List", "size", "()I", true);
        assertNotNull(item);
        assertTrue(item.index > 0);
    }

    @Test
    void newMethodHandleItemAddsToPool() {
        ConstantPoolGeneration cp = new ConstantPoolGeneration();
        MethodHandle mh = new MethodHandle(MethodHandle.Tag.TAG_INVOKEVIRTUAL, "java/lang/String", "length", "()I");
        MethodHandleItem item = cp.newMethodHandleItem(mh);
        assertNotNull(item);
        assertTrue(item.index > 0);
    }

    @Test
    void newMethodHandleItemForInterfaceAddsIMethodRef() {
        ConstantPoolGeneration cp = new ConstantPoolGeneration();
        MethodHandle mh = new MethodHandle(MethodHandle.Tag.TAG_INVOKEINTERFACE, "java/util/List", "size", "()I");
        MethodHandleItem item = cp.newMethodHandleItem(mh);
        assertNotNull(item);
        assertTrue(item.index > 0);
    }

    @Test
    void newConstItemWithString() {
        ConstantPoolGeneration cp = new ConstantPoolGeneration();
        Item item = cp.newConstItem("hello");
        assertNotNull(item);
    }

    @Test
    void newConstItemWithInteger() {
        ConstantPoolGeneration cp = new ConstantPoolGeneration();
        Item item = cp.newConstItem(Integer.valueOf(42));
        assertNotNull(item);
    }

    @Test
    void newConstItemWithCharacter() {
        ConstantPoolGeneration cp = new ConstantPoolGeneration();
        Item item = cp.newConstItem('A');
        assertNotNull(item);
    }

    @Test
    void newConstItemWithBoolean() {
        ConstantPoolGeneration cp = new ConstantPoolGeneration();
        Item item = cp.newConstItem(Boolean.TRUE);
        assertNotNull(item);
    }

    @Test
    void newConstItemWithObjectType() {
        ConstantPoolGeneration cp = new ConstantPoolGeneration();
        ObjectType type = ObjectType.create("java/lang/String");
        Item item = cp.newConstItem(type);
        assertNotNull(item);
    }

    @Test
    void newConstItemWithMethodType() {
        ConstantPoolGeneration cp = new ConstantPoolGeneration();
        MethodType type = (MethodType) JavaType.getType("(I)V");
        Item item = cp.newConstItem(type);
        assertNotNull(item);
    }

    @Test
    void newConstItemWithPrimitiveType() {
        ConstantPoolGeneration cp = new ConstantPoolGeneration();
        PrimitiveType type = PrimitiveType.getPrimitiveType('I');
        assertNotNull(type);
        Item item = cp.newConstItem(type);
        assertNotNull(item);
    }

    @Test
    void newConstItemWithMethodHandle() {
        ConstantPoolGeneration cp = new ConstantPoolGeneration();
        MethodHandle mh = new MethodHandle(MethodHandle.Tag.TAG_INVOKESTATIC, "java/lang/Math", "abs", "(I)I");
        Item item = cp.newConstItem(mh);
        assertNotNull(item);
    }

    @Test
    void getSizeReturnsPositiveValue() {
        ConstantPoolGeneration cp = new ConstantPoolGeneration();
        cp.newUTF8("test");
        int size = cp.getSize();
        assertTrue(size > 0);
    }

    @Test
    void checkConstantPoolMaxSizeDoesNotThrowForSmallPool() {
        ConstantPoolGeneration cp = new ConstantPoolGeneration();
        cp.newUTF8("test");
        cp.checkConstantPoolMaxSize();
    }
}
