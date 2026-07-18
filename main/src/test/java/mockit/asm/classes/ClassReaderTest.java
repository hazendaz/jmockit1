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

    /**
     * java.net.InetAddress is sealed (JDK 17+), so its classfile carries a PermittedSubclasses attribute.
     * Round-tripping it through ClassReader -> ClassWriter must preserve that attribute; otherwise the JVM rejects a
     * redefinition of the class with "attempted to change the class ... PermittedSubclasses attribute". This asserts
     * the attribute survives in the emitted class-level attribute table, not merely that some bytes came out. (Checking
     * the constant pool would be meaningless: the pool is copied wholesale, so the "PermittedSubclasses" UTF8 entry
     * survives regardless of whether the attribute itself is written.)
     */
    @Test
    void preservesPermittedSubclassesAttributeOnSealedClass() {
        byte[] code = ClassFile.readBytesFromClassFile(java.net.InetAddress.class);
        assertTrue(hasClassAttribute(code, "PermittedSubclasses"),
                "test premise: InetAddress should be sealed (carry PermittedSubclasses) on this JDK");

        ClassReader reader = new ClassReader(code);
        ClassWriter writer = new ClassWriter(reader);
        reader.accept(writer);
        byte[] result = writer.toByteArray();

        assertNotNull(result);
        assertTrue(hasClassAttribute(result, "PermittedSubclasses"),
                "PermittedSubclasses attribute must be preserved in the rewritten sealed class");
    }

    /**
     * Structurally parses a class file down to its class-level attribute table and reports whether an attribute with
     * the given name is present there. Unlike a constant-pool scan, this reflects what the writer actually emitted, so
     * it distinguishes a preserved attribute from a merely-copied name entry.
     */
    private static boolean hasClassAttribute(byte[] cf, String attributeName) {
        String[] utf8 = new String[readUnsignedShort(cf, 8)];
        int index = 10;

        // Constant pool: record UTF8 values (to resolve attribute name indices) and skip everything by tag size.
        for (int i = 1; i < utf8.length; i++) {
            int tag = cf[index] & 0xFF;
            index++;

            switch (tag) {
                case 1: // Utf8
                    int length = readUnsignedShort(cf, index);
                    utf8[i] = new String(cf, index + 2, length, java.nio.charset.StandardCharsets.UTF_8);
                    index += 2 + length;
                    break;
                case 5: // Long
                case 6: // Double
                    index += 8;
                    i++; // occupies two pool slots
                    break;
                case 7: // Class
                case 8: // String
                case 16: // MethodType
                case 19: // Module
                case 20: // Package
                    index += 2;
                    break;
                case 15: // MethodHandle
                    index += 3;
                    break;
                default: // Integer/Float/Fieldref/Methodref/InterfaceMethodref/NameAndType/Dynamic/InvokeDynamic
                    index += 4;
                    break;
            }
        }

        index += 6; // access_flags, this_class, super_class
        index += 2 + 2 * readUnsignedShort(cf, index); // interfaces_count + interfaces[]
        index = skipMembers(cf, index); // fields[]
        index = skipMembers(cf, index); // methods[]

        // Class-level attributes table: attributes_count (u2) then attribute_info[].
        int attrCount = readUnsignedShort(cf, index);
        index += 2;

        for (int a = 0; a < attrCount; a++) {
            int nameIndex = readUnsignedShort(cf, index);
            int attrLength = readInt(cf, index + 2);
            index += 6;

            if (attributeName.equals(utf8[nameIndex])) {
                return true;
            }

            index += attrLength;
        }

        return false;
    }

    /**
     * Skips a fields[] or methods[] table (both have identical member_info layout), returning the offset just past it.
     */
    private static int skipMembers(byte[] cf, int index) {
        int count = readUnsignedShort(cf, index);
        index += 2;

        for (int m = 0; m < count; m++) {
            index += 6; // access_flags, name_index, descriptor_index

            int attrCount = readUnsignedShort(cf, index);
            index += 2;

            for (int a = 0; a < attrCount; a++) {
                int attrLength = readInt(cf, index + 2); // skip name_index (u2), read length (u4)
                index += 6 + attrLength;
            }
        }

        return index;
    }

    private static int readUnsignedShort(byte[] b, int i) {
        return (b[i] & 0xFF) << 8 | b[i + 1] & 0xFF;
    }

    private static int readInt(byte[] b, int i) {
        return (b[i] & 0xFF) << 24 | (b[i + 1] & 0xFF) << 16 | (b[i + 2] & 0xFF) << 8 | b[i + 3] & 0xFF;
    }
}
