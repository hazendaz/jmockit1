/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class ClassIdentificationTest {

    @Test
    void getLoadedClassReturnsCorrectClass() {
        ClassIdentification id = new ClassIdentification(ClassIdentificationTest.class.getClassLoader(),
                "java.lang.String");
        Class<?> loaded = id.getLoadedClass();
        assertSame(String.class, loaded);
    }

    @Test
    void getLoadedClassWithNullLoaderUsesBootstrap() {
        ClassIdentification id = new ClassIdentification(null, "java.lang.Integer");
        Class<?> loaded = id.getLoadedClass();
        assertSame(Integer.class, loaded);
    }

    @Test
    void getLoadedClassThrowsForUnknownClass() {
        ClassIdentification id = new ClassIdentification(ClassIdentificationTest.class.getClassLoader(),
                "com.example.DoesNotExist");
        assertThrows(RuntimeException.class, id::getLoadedClass);
    }

    @Test
    void equalsSameInstance() {
        ClassIdentification id = new ClassIdentification(null, "java.lang.String");
        assertEquals(id, id);
    }

    @Test
    void equalsNullReturnsFalse() {
        ClassIdentification id = new ClassIdentification(null, "java.lang.String");
        assertNotEquals(null, id);
    }

    @Test
    void equalsDifferentClassReturnsFalse() {
        ClassIdentification id = new ClassIdentification(null, "java.lang.String");
        assertFalse(id.equals("some string"));
    }

    @Test
    void equalsWithSameLoaderAndName() {
        ClassLoader cl = ClassIdentificationTest.class.getClassLoader();
        ClassIdentification id1 = new ClassIdentification(cl, "java.lang.String");
        ClassIdentification id2 = new ClassIdentification(cl, "java.lang.String");
        assertEquals(id1, id2);
    }

    @Test
    void equalsWithDifferentName() {
        ClassLoader cl = ClassIdentificationTest.class.getClassLoader();
        ClassIdentification id1 = new ClassIdentification(cl, "java.lang.String");
        ClassIdentification id2 = new ClassIdentification(cl, "java.lang.Integer");
        assertNotEquals(id1, id2);
    }

    @Test
    void equalsWithDifferentLoader() {
        // One with null loader (bootstrap) and one with application loader
        ClassLoader appCl = ClassIdentificationTest.class.getClassLoader();
        ClassIdentification id1 = new ClassIdentification(null, "java.lang.String");
        ClassIdentification id2 = new ClassIdentification(appCl, "java.lang.String");
        // Different loaders (null vs non-null) -> not equal
        assertNotEquals(id1, id2);
    }

    @Test
    void equalsWithBothNullLoaders() {
        ClassIdentification id1 = new ClassIdentification(null, "java.lang.String");
        ClassIdentification id2 = new ClassIdentification(null, "java.lang.String");
        assertEquals(id1, id2);
    }

    @Test
    void hashCodeWithNullLoader() {
        ClassIdentification id = new ClassIdentification(null, "java.lang.String");
        assertEquals("java.lang.String".hashCode(), id.hashCode());
    }

    @Test
    void hashCodeWithNonNullLoader() {
        ClassLoader cl = ClassIdentificationTest.class.getClassLoader();
        ClassIdentification id = new ClassIdentification(cl, "java.lang.String");
        int expected = 31 * cl.hashCode() + "java.lang.String".hashCode();
        assertEquals(expected, id.hashCode());
    }

    @Test
    void equalInstancesHaveSameHashCode() {
        ClassLoader cl = ClassIdentificationTest.class.getClassLoader();
        ClassIdentification id1 = new ClassIdentification(cl, "java.lang.String");
        ClassIdentification id2 = new ClassIdentification(cl, "java.lang.String");
        assertTrue(id1.equals(id2));
        assertEquals(id1.hashCode(), id2.hashCode());
    }
}
