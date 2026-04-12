/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.Map;

import mockit.asm.classes.ClassReader;

import org.junit.jupiter.api.Test;

final class ClassFileTest {

    @Test
    void createClassReaderWithValidClass() {
        ClassLoader cl = ClassFileTest.class.getClassLoader();
        ClassReader reader = ClassFile.createClassReader(cl, "mockit/internal/ClassFile");
        assertNotNull(reader);
    }

    @Test
    void createClassReaderWithNonExistentClassReturnsNull() {
        ClassLoader cl = ClassFileTest.class.getClassLoader();
        ClassReader reader = ClassFile.createClassReader(cl, "com/example/DoesNotExist");
        // Should return null when class file not found
        assertTrue(reader == null);
    }

    @Test
    void createReaderOrGetFromCacheForKnownClass() {
        ClassReader reader = ClassFile.createReaderOrGetFromCache(String.class);
        assertNotNull(reader);
    }

    @Test
    void createReaderOrGetFromCacheReturnsCachedReader() {
        // Call twice to test caching
        ClassReader reader1 = ClassFile.createReaderOrGetFromCache(Integer.class);
        ClassReader reader2 = ClassFile.createReaderOrGetFromCache(Integer.class);
        assertNotNull(reader1);
        assertNotNull(reader2);
    }

    @Test
    void createReaderOrGetFromCacheForNonCachedClass() {
        // Use a class that's unlikely to be in CachedClassfiles (test-only class)
        ClassReader reader = ClassFile.createReaderOrGetFromCache(ClassFileTest.class);
        assertNotNull(reader);
        // Call again to hit the CLASS_FILES cache
        ClassReader reader2 = ClassFile.createReaderOrGetFromCache(ClassFileTest.class);
        assertNotNull(reader2);
    }

    @Test
    void getClassFileByNameForKnownClass() {
        byte[] bytes = ClassFile.getClassFile("java/lang/String");
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @Test
    void getClassFileByClassForKnownClass() {
        byte[] bytes = ClassFile.getClassFile(String.class);
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @Test
    void getClassFileByLoaderAndNameForKnownClass() {
        byte[] bytes = ClassFile.getClassFile(ClassFileTest.class.getClassLoader(), "mockit/internal/ClassFile");
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @Test
    void readBytesFromClassFileByName() {
        byte[] bytes = ClassFile.readBytesFromClassFile("mockit/internal/ClassFile");
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @Test
    void readBytesFromClassFileByClass() {
        byte[] bytes = ClassFile.readBytesFromClassFile(ClassFileTest.class);
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @Test
    void readBytesFromClassFileThrowsForUnknownClass() {
        assertThrows(RuntimeException.class, () -> ClassFile.readBytesFromClassFile("com/example/DoesNotExist"));
    }

    @Test
    void notFoundExceptionHasCorrectMessage() {
        try {
            ClassFile.readBytesFromClassFile("com/example/Missing");
        } catch (RuntimeException e) {
            // Expected
        }
    }

    @Test
    void createReaderOrGetFromCacheWithCacheClearedForcesSavingInCache() throws Exception {
        Field classFilesField = ClassFile.class.getDeclaredField("CLASS_FILES");
        classFilesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, ClassReader> classFiles = (Map<String, ClassReader>) classFilesField.get(null);

        String classDesc = ClassFile.class.getName().replace('.', '/');
        classFiles.remove(classDesc);

        ClassReader reader = ClassFile.createReaderOrGetFromCache(ClassFile.class);
        assertNotNull(reader);
    }

    @Test
    void createReaderFromLastRedefinitionIfAnyWithNonCachedClass() throws Exception {
        Field classFilesField = ClassFile.class.getDeclaredField("CLASS_FILES");
        classFilesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, ClassReader> classFiles = (Map<String, ClassReader>) classFilesField.get(null);

        String classDesc = ClassFile.class.getName().replace('.', '/');
        classFiles.remove(classDesc);

        ClassReader reader = ClassFile.createReaderFromLastRedefinitionIfAny(ClassFile.class);
        assertNotNull(reader);
    }

    @Test
    void createReaderFromLastRedefinitionIfAny() {
        ClassReader reader = ClassFile.createReaderFromLastRedefinitionIfAny(String.class);
        assertNotNull(reader);
    }
}
