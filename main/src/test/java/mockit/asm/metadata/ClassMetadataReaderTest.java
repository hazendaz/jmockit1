/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.asm.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.List;

import mockit.internal.ClassFile;

import org.junit.jupiter.api.Test;

final class ClassMetadataReaderTest {

    private static byte[] getBytesFor(Class<?> clazz) {
        return ClassFile.readBytesFromClassFile(clazz);
    }

    @Test
    void readVersionOfStringClass() {
        byte[] code = getBytesFor(String.class);
        int version = ClassMetadataReader.readVersion(code);
        assertTrue(version >= 52, "Expected at least Java 8 bytecode version");
    }

    @Test
    void constructorWithCodeOnly() {
        byte[] code = getBytesFor(String.class);
        ClassMetadataReader reader = new ClassMetadataReader(code);
        assertNotNull(reader);
    }

    @Test
    void constructorWithAttributes() {
        byte[] code = getBytesFor(String.class);
        ClassMetadataReader reader = new ClassMetadataReader(code,
                EnumSet.of(ClassMetadataReader.Attribute.Annotations));
        assertNotNull(reader);
    }

    @Test
    void getVersionForStringClass() {
        byte[] code = getBytesFor(String.class);
        ClassMetadataReader reader = new ClassMetadataReader(code);
        int version = reader.getVersion();
        assertTrue(version >= 52);
    }

    @Test
    void getAccessFlagsForStringClass() {
        byte[] code = getBytesFor(String.class);
        ClassMetadataReader reader = new ClassMetadataReader(code);
        int flags = reader.getAccessFlags();
        assertTrue(flags > 0);
    }

    @Test
    void getThisClassForStringClass() {
        byte[] code = getBytesFor(String.class);
        ClassMetadataReader reader = new ClassMetadataReader(code);
        String thisClass = reader.getThisClass();
        assertEquals("java/lang/String", thisClass);
    }

    @Test
    void getSuperClassForStringClass() {
        byte[] code = getBytesFor(String.class);
        ClassMetadataReader reader = new ClassMetadataReader(code);
        String superClass = reader.getSuperClass();
        assertEquals("java/lang/Object", superClass);
    }

    @Test
    void getInterfacesForStringClass() {
        byte[] code = getBytesFor(String.class);
        ClassMetadataReader reader = new ClassMetadataReader(code);
        String[] interfaces = reader.getInterfaces();
        assertNotNull(interfaces);
        assertTrue(interfaces.length > 0);
    }

    @Test
    void getFieldsForStringClass() {
        byte[] code = getBytesFor(String.class);
        ClassMetadataReader reader = new ClassMetadataReader(code);
        List<ClassMetadataReader.FieldInfo> fields = reader.getFields();
        assertNotNull(fields);
        assertFalse(fields.isEmpty());
    }

    @Test
    void getMethodsForStringClass() {
        byte[] code = getBytesFor(String.class);
        ClassMetadataReader reader = new ClassMetadataReader(code);
        List<ClassMetadataReader.MethodInfo> methods = reader.getMethods();
        assertNotNull(methods);
        assertFalse(methods.isEmpty());
    }

    @Test
    void getAnnotationsForDeprecatedClass() {
        byte[] code = getBytesFor(Deprecated.class);
        ClassMetadataReader reader = new ClassMetadataReader(code,
                EnumSet.of(ClassMetadataReader.Attribute.Annotations));
        List<ClassMetadataReader.AnnotationInfo> annotations = reader.getAnnotations();
        assertNotNull(annotations);
    }

    @Test
    void fieldInfoHasNameAndDesc() {
        byte[] code = getBytesFor(String.class);
        ClassMetadataReader reader = new ClassMetadataReader(code);
        List<ClassMetadataReader.FieldInfo> fields = reader.getFields();
        assertNotNull(fields);
        for (ClassMetadataReader.FieldInfo field : fields) {
            assertNotNull(field.name);
            assertNotNull(field.desc);
        }
    }

    @Test
    void methodInfoHasNameAndDesc() {
        byte[] code = getBytesFor(String.class);
        ClassMetadataReader reader = new ClassMetadataReader(code);
        List<ClassMetadataReader.MethodInfo> methods = reader.getMethods();
        assertNotNull(methods);
        boolean hasConstructor = false;
        boolean hasMethod = false;
        for (ClassMetadataReader.MethodInfo method : methods) {
            assertNotNull(method.name);
            assertNotNull(method.desc);
            if (method.isConstructor()) {
                hasConstructor = true;
            }
            if (method.isMethod()) {
                hasMethod = true;
            }
        }
        assertTrue(hasConstructor);
        assertTrue(hasMethod);
    }

    @Test
    void readClassWithSignatureAttribute() {
        byte[] code = getBytesFor(java.util.ArrayList.class);
        ClassMetadataReader reader = new ClassMetadataReader(code, EnumSet.of(ClassMetadataReader.Attribute.Signature));
        assertNotNull(reader.getThisClass());
    }

    @Test
    void getMethodsWithParameterNames() {
        byte[] code = getBytesFor(String.class);
        ClassMetadataReader reader = new ClassMetadataReader(code,
                EnumSet.of(ClassMetadataReader.Attribute.Parameters));
        List<ClassMetadataReader.MethodInfo> methods = reader.getMethods();
        assertNotNull(methods);
        assertFalse(methods.isEmpty());
    }

    @Test
    void getSuperClassOfObjectIsNull() {
        byte[] code = getBytesFor(Object.class);
        ClassMetadataReader reader = new ClassMetadataReader(code);
        String superClass = reader.getSuperClass();
        assertTrue(superClass == null || superClass.isEmpty());
    }
}
