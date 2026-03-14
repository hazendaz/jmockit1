/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.asm.classes;

import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

final class ClassVisitorTest {

    static class TestClassVisitor extends ClassVisitor {
        TestClassVisitor() {
            super();
        }
    }

    @Test
    void visitDoesNothing() {
        TestClassVisitor visitor = new TestClassVisitor();
        ClassInfo info = new ClassInfo();
        // Should not throw
        visitor.visit(52, 0, "java/lang/Object", info);
    }

    @Test
    void visitInnerClassDoesNothing() {
        TestClassVisitor visitor = new TestClassVisitor();
        // Should not throw
        visitor.visitInnerClass("java/lang/Object$1", "java/lang/Object", null, 0);
    }

    @Test
    void visitFieldReturnsNull() {
        TestClassVisitor visitor = new TestClassVisitor();
        var result = visitor.visitField(0, "myField", "I", null, null);
        assertNull(result);
    }

    @Test
    void visitMethodReturnsNull() {
        TestClassVisitor visitor = new TestClassVisitor();
        var result = visitor.visitMethod(0, "myMethod", "()V", null, null);
        assertNull(result);
    }

    @Test
    void toByteArrayReturnsNull() {
        TestClassVisitor visitor = new TestClassVisitor();
        var result = visitor.toByteArray();
        assertNull(result);
    }
}
