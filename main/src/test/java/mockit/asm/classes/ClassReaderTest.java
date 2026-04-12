/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.asm.classes;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import mockit.internal.ClassFile;

import org.junit.jupiter.api.Test;

final class ClassReaderTest {

    @Test
    void acceptVisitorOnAnnotationWithEnumAndArrayValues() {
        byte[] code = ClassFile.readBytesFromClassFile(java.lang.annotation.Retention.class);
        ClassReader reader = new ClassReader(code);
        ClassWriter writer = new ClassWriter(reader);
        reader.accept(writer);
        byte[] result = writer.toByteArray();
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void acceptVisitorOnAnnotationWithTargetArray() {
        byte[] code = ClassFile.readBytesFromClassFile(java.lang.annotation.Target.class);
        ClassReader reader = new ClassReader(code);
        ClassWriter writer = new ClassWriter(reader);
        reader.accept(writer);
        byte[] result = writer.toByteArray();
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void acceptVisitorOnDeprecatedAnnotation() {
        byte[] code = ClassFile.readBytesFromClassFile(Deprecated.class);
        ClassReader reader = new ClassReader(code);
        ClassWriter writer = new ClassWriter(reader);
        reader.accept(writer);
        byte[] result = writer.toByteArray();
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void acceptVisitorOnClassWithMultipleAnnotations() {
        byte[] code = ClassFile.readBytesFromClassFile(java.lang.annotation.Documented.class);
        ClassReader reader = new ClassReader(code);
        ClassWriter writer = new ClassWriter(reader);
        reader.accept(writer);
        byte[] result = writer.toByteArray();
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void acceptVisitorOnStringClass() {
        byte[] code = ClassFile.readBytesFromClassFile(String.class);
        ClassReader reader = new ClassReader(code);
        ClassWriter writer = new ClassWriter(reader);
        reader.accept(writer);
        byte[] result = writer.toByteArray();
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void acceptVisitorOnInterfaceWithAnnotation() {
        byte[] code = ClassFile.readBytesFromClassFile(java.io.Serializable.class);
        ClassReader reader = new ClassReader(code);
        ClassWriter writer = new ClassWriter(reader);
        reader.accept(writer);
        byte[] result = writer.toByteArray();
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void acceptVisitorOnEnumClass() {
        byte[] code = ClassFile.readBytesFromClassFile(java.lang.annotation.RetentionPolicy.class);
        ClassReader reader = new ClassReader(code);
        ClassWriter writer = new ClassWriter(reader);
        reader.accept(writer);
        byte[] result = writer.toByteArray();
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void getVersionFromClassReader() {
        byte[] code = ClassFile.readBytesFromClassFile(String.class);
        ClassReader reader = new ClassReader(code);
        int version = reader.getVersion();
        assertTrue(version >= 52);
    }

    @Test
    void getAccessFromClassReader() {
        byte[] code = ClassFile.readBytesFromClassFile(String.class);
        ClassReader reader = new ClassReader(code);
        int access = reader.getAccess();
        assertTrue(access > 0);
    }

    @Test
    void getSuperNameFromClassReader() {
        byte[] code = ClassFile.readBytesFromClassFile(String.class);
        ClassReader reader = new ClassReader(code);
        String superName = reader.getSuperName();
        assertTrue("java/lang/Object".equals(superName) || superName != null);
    }

    @Test
    void getBytecodeFromClassReader() {
        byte[] code = ClassFile.readBytesFromClassFile(String.class);
        ClassReader reader = new ClassReader(code);
        byte[] bytecode = reader.getBytecode();
        assertNotNull(bytecode);
        assertTrue(bytecode.length > 0);
    }

    @Test
    void acceptVisitorOnClassWithPrimitiveArrayAnnotation() {
        byte[] code = ClassFile
                .readBytesFromClassFile(mockit.asm.annotations.testdata.AnnotatedWithPrimitiveArrays.class);
        ClassReader reader = new ClassReader(code);
        ClassWriter writer = new ClassWriter(reader);
        reader.accept(writer);
        byte[] result = writer.toByteArray();
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void acceptVisitorOnClassWithEmptyArrayAnnotation() {
        byte[] code = ClassFile.readBytesFromClassFile(mockit.asm.annotations.testdata.AnnotatedWithEmptyArrays.class);
        ClassReader reader = new ClassReader(code);
        ClassWriter writer = new ClassWriter(reader);
        reader.accept(writer);
        byte[] result = writer.toByteArray();
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void acceptVisitorOnComplexClassWithSwitchStatements() {
        byte[] code = ClassFile.readBytesFromClassFile(java.util.Calendar.class);
        ClassReader reader = new ClassReader(code);
        ClassWriter writer = new ClassWriter(reader);
        reader.accept(writer);
        byte[] result = writer.toByteArray();
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void acceptVisitorOnHashMapClass() {
        byte[] code = ClassFile.readBytesFromClassFile(java.util.HashMap.class);
        ClassReader reader = new ClassReader(code);
        ClassWriter writer = new ClassWriter(reader);
        reader.accept(writer);
        byte[] result = writer.toByteArray();
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void acceptVisitorOnCharacterClass() {
        byte[] code = ClassFile.readBytesFromClassFile(Character.class);
        ClassReader reader = new ClassReader(code);
        ClassWriter writer = new ClassWriter(reader);
        reader.accept(writer);
        byte[] result = writer.toByteArray();
        assertNotNull(result);
        assertTrue(result.length > 0);
    }
}
