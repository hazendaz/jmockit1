/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

final class ClassLoadTest {

    @Test
    void loadClassByInternalNameReturnsCorrectClass() {
        Class<String> cls = ClassLoad.loadByInternalName("java/lang/String");
        assertEquals(String.class, cls);
    }

    @Test
    void loadClassByNameReturnsCorrectClass() {
        Class<Integer> cls = ClassLoad.loadClass("java.lang.Integer");
        assertEquals(Integer.class, cls);
    }

    @Test
    void loadClassThrowsForUnknownClass() {
        assertThrows(IllegalArgumentException.class, () -> ClassLoad.loadClass("com.example.DoesNotExist"));
    }

    @Test
    void loadClassAtStartupReturnsCorrectClass() {
        Class<String> cls = ClassLoad.loadClassAtStartup("java.lang.String");
        assertEquals(String.class, cls);
    }

    @Test
    void loadClassAtStartupThrowsForUnknownClass() {
        assertThrows(IllegalArgumentException.class, () -> ClassLoad.loadClassAtStartup("com.example.DoesNotExist"));
    }

    @Test
    void loadClassWithLoaderReturnsNullForUnknownClass() {
        Class<?> cls = ClassLoad.loadClass(ClassLoadTest.class.getClassLoader(), "com.example.DoesNotExist");
        assertNull(cls);
    }

    @Test
    void loadClassWithNullLoaderReturnsNullForUnknownClass() {
        // Bootstrap classloader - can load JDK classes but not application classes
        Class<?> cls = ClassLoad.loadClass(null, "com.example.DoesNotExist");
        assertNull(cls);
    }

    @Test
    void loadFromLoaderReturnsCorrectClass() {
        Class<String> cls = ClassLoad.loadFromLoader(ClassLoadTest.class.getClassLoader(), "java.lang.String");
        assertEquals(String.class, cls);
    }

    @Test
    void loadFromLoaderThrowsForUnknownClass() {
        assertThrows(IllegalArgumentException.class,
                () -> ClassLoad.loadFromLoader(ClassLoadTest.class.getClassLoader(), "com.example.DoesNotExist"));
    }

    @Test
    void searchTypeInClasspathReturnsClassWhenFound() {
        Class<? extends String> cls = ClassLoad.searchTypeInClasspath("java.lang.String");
        assertEquals(String.class, cls);
    }

    @Test
    void searchTypeInClasspathReturnsNullForUnknownClass() {
        Class<?> cls = ClassLoad.searchTypeInClasspath("com.example.DoesNotExist");
        assertNull(cls);
    }

    @Test
    void searchTypeInClasspathWithInitializeTrue() {
        Class<?> cls = ClassLoad.searchTypeInClasspath("java.lang.String", true);
        assertNotNull(cls);
        assertEquals(String.class, cls);
    }

    @Test
    void registerLoadedClassAndRetrieve() {
        ClassLoad.registerLoadedClass(ClassLoadTest.class);
        Class<ClassLoadTest> cls = ClassLoad.loadClass(ClassLoadTest.class.getName());
        assertEquals(ClassLoadTest.class, cls);
    }

    @Test
    void getSuperClassForKnownClass() {
        String superClass = ClassLoad.getSuperClass("java/lang/String");
        assertEquals("java/lang/Object", superClass);
    }

    @Test
    void getSuperClassReturnsObjectForObjectItself() {
        String superClass = ClassLoad.getSuperClass("java/lang/Object");
        assertEquals(ClassLoad.OBJECT, superClass);
    }

    @Test
    void addAndGetSuperClass() {
        ClassLoad.addSuperClass("com/example/Child", "com/example/Parent");
        String superClass = ClassLoad.getSuperClass("com/example/Child");
        assertEquals("com/example/Parent", superClass);
    }
}
