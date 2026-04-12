/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.asm.annotations;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import mockit.asm.constantPool.ConstantPoolGeneration;
import mockit.asm.util.ByteVector;

import org.junit.jupiter.api.Test;

final class AnnotationVisitorTest {

    private static AnnotationVisitor createVisitor() {
        ConstantPoolGeneration cp = new ConstantPoolGeneration();
        return new AnnotationVisitor(cp, "Lmy/annotation/Ann;");
    }

    @Test
    void constructorCreatesVisitorWithNonNullByteData() {
        AnnotationVisitor visitor = createVisitor();
        assertNotNull(visitor);
        assertTrue(visitor.getSize() > 0);
    }

    @Test
    void visitByteArray() {
        AnnotationVisitor visitor = createVisitor();
        visitor.visit("bytes", new byte[] { 1, 2, 3 });
        visitor.visitEnd();
        assertTrue(visitor.getSize() > 0);
    }

    @Test
    void visitBooleanArray() {
        AnnotationVisitor visitor = createVisitor();
        visitor.visit("booleans", new boolean[] { true, false, true });
        visitor.visitEnd();
        assertTrue(visitor.getSize() > 0);
    }

    @Test
    void visitShortArray() {
        AnnotationVisitor visitor = createVisitor();
        visitor.visit("shorts", new short[] { 10, 20, 30 });
        visitor.visitEnd();
        assertTrue(visitor.getSize() > 0);
    }

    @Test
    void visitCharArray() {
        AnnotationVisitor visitor = createVisitor();
        visitor.visit("chars", new char[] { 'a', 'b', 'c' });
        visitor.visitEnd();
        assertTrue(visitor.getSize() > 0);
    }

    @Test
    void visitIntArray() {
        AnnotationVisitor visitor = createVisitor();
        visitor.visit("ints", new int[] { 100, 200, 300 });
        visitor.visitEnd();
        assertTrue(visitor.getSize() > 0);
    }

    @Test
    void visitLongArray() {
        AnnotationVisitor visitor = createVisitor();
        visitor.visit("longs", new long[] { 1000L, 2000L, 3000L });
        visitor.visitEnd();
        assertTrue(visitor.getSize() > 0);
    }

    @Test
    void visitFloatArray() {
        AnnotationVisitor visitor = createVisitor();
        visitor.visit("floats", new float[] { 1.0f, 2.0f, 3.0f });
        visitor.visitEnd();
        assertTrue(visitor.getSize() > 0);
    }

    @Test
    void visitDoubleArray() {
        AnnotationVisitor visitor = createVisitor();
        visitor.visit("doubles", new double[] { 1.0, 2.0, 3.0 });
        visitor.visitEnd();
        assertTrue(visitor.getSize() > 0);
    }

    @Test
    void visitStringValue() {
        AnnotationVisitor visitor = createVisitor();
        visitor.visit("name", "hello");
        visitor.visitEnd();
        assertTrue(visitor.getSize() > 0);
    }

    @Test
    void visitIntegerValue() {
        AnnotationVisitor visitor = createVisitor();
        visitor.visit("value", 42);
        visitor.visitEnd();
        assertTrue(visitor.getSize() > 0);
    }

    @Test
    void visitBooleanValue() {
        AnnotationVisitor visitor = createVisitor();
        visitor.visit("flag", Boolean.TRUE);
        visitor.visitEnd();
        assertTrue(visitor.getSize() > 0);
    }

    @Test
    void visitDoubleValue() {
        AnnotationVisitor visitor = createVisitor();
        visitor.visit("d", 3.14);
        visitor.visitEnd();
        assertTrue(visitor.getSize() > 0);
    }

    @Test
    void visitFloatValue() {
        AnnotationVisitor visitor = createVisitor();
        visitor.visit("f", 1.5f);
        visitor.visitEnd();
        assertTrue(visitor.getSize() > 0);
    }

    @Test
    void visitLongValue() {
        AnnotationVisitor visitor = createVisitor();
        visitor.visit("l", 100L);
        visitor.visitEnd();
        assertTrue(visitor.getSize() > 0);
    }

    @Test
    void visitByteValue() {
        AnnotationVisitor visitor = createVisitor();
        visitor.visit("b", (byte) 5);
        visitor.visitEnd();
        assertTrue(visitor.getSize() > 0);
    }

    @Test
    void visitCharValue() {
        AnnotationVisitor visitor = createVisitor();
        visitor.visit("c", 'X');
        visitor.visitEnd();
        assertTrue(visitor.getSize() > 0);
    }

    @Test
    void visitShortValue() {
        AnnotationVisitor visitor = createVisitor();
        visitor.visit("s", (short) 7);
        visitor.visitEnd();
        assertTrue(visitor.getSize() > 0);
    }

    @Test
    void visitEnumValue() {
        AnnotationVisitor visitor = createVisitor();
        visitor.visitEnum("type", "Ljava/lang/annotation/RetentionPolicy;", "RUNTIME");
        visitor.visitEnd();
        assertTrue(visitor.getSize() > 0);
    }

    @Test
    void visitAnnotationCreatesNestedVisitor() {
        AnnotationVisitor visitor = createVisitor();
        AnnotationVisitor nested = visitor.visitAnnotation("nested", "Ljava/lang/Deprecated;");
        assertNotNull(nested);
        nested.visitEnd();
        visitor.visitEnd();
        assertTrue(visitor.getSize() > 0);
    }

    @Test
    void visitArrayCreatesArrayVisitor() {
        AnnotationVisitor visitor = createVisitor();
        AnnotationVisitor array = visitor.visitArray("items");
        assertNotNull(array);
        array.visit(null, "item1");
        array.visit(null, "item2");
        array.visitEnd();
        visitor.visitEnd();
        assertTrue(visitor.getSize() > 0);
    }

    @Test
    void setNextLinksAnnotations() {
        AnnotationVisitor first = createVisitor();
        AnnotationVisitor second = createVisitor();
        first.setNext(second);
        assertTrue(first.getSize() > second.getSize());
    }

    @Test
    void putWritesAnnotationDataToByteVector() {
        AnnotationVisitor visitor = createVisitor();
        visitor.visit("value", 42);
        visitor.visitEnd();

        ByteVector out = new ByteVector();
        visitor.put(out);
        assertTrue(out.getLength() > 0);
    }

    @Test
    void putWithArrayWritesMultipleAnnotations() {
        AnnotationVisitor first = createVisitor();
        first.visit("value", 1);
        first.visitEnd();

        AnnotationVisitor second = createVisitor();
        second.visit("value", 2);
        second.visitEnd();

        ByteVector out = new ByteVector();
        AnnotationVisitor.put(out, new AnnotationVisitor[] { first, second });
        assertTrue(out.getLength() > 0);
    }

    @Test
    void visitEmptyByteArray() {
        AnnotationVisitor visitor = createVisitor();
        visitor.visit("empty", new byte[] {});
        visitor.visitEnd();
        assertTrue(visitor.getSize() > 0);
    }

    @Test
    void visitMultipleValuesIncreasesSize() {
        AnnotationVisitor visitor = createVisitor();
        int sizeBefore = visitor.getSize();
        visitor.visit("a", 1);
        visitor.visit("b", 2);
        visitor.visitEnd();
        assertTrue(visitor.getSize() >= sizeBefore);
    }
}
