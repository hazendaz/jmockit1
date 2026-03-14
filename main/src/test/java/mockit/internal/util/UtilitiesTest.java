/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.security.CodeSource;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

final class UtilitiesTest {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface TestAnnotation {
    }

    static class AnnotatedClass {
        @TestAnnotation
        public void annotatedMethod() {
        }

        public void unannotatedMethod() {
        }

        @TestAnnotation
        private void privateAnnotatedMethod() {
        }
    }

    @Test
    void getAnnotatedMethodFindsPublicAnnotatedMethod() {
        Method method = Utilities.getAnnotatedMethod(AnnotatedClass.class, TestAnnotation.class);
        assertNotNull(method);
        assertEquals("annotatedMethod", method.getName());
    }

    @Test
    void getAnnotatedMethodReturnsNullWhenNotFound() {
        Method method = Utilities.getAnnotatedMethod(AnnotatedClass.class, Deprecated.class);
        assertNull(method);
    }

    @Test
    void getAnnotatedDeclaredMethodFindsDeclaredAnnotatedMethod() {
        Method method = Utilities.getAnnotatedDeclaredMethod(AnnotatedClass.class, TestAnnotation.class);
        assertNotNull(method);
        // Should find one of the annotated declared methods
        assertNotNull(method.getAnnotation(TestAnnotation.class));
    }

    @Test
    void getAnnotatedDeclaredMethodReturnsNullWhenNotFound() {
        Method method = Utilities.getAnnotatedDeclaredMethod(AnnotatedClass.class, Deprecated.class);
        assertNull(method);
    }

    @Test
    void getClassTypeFromClass() {
        Class<?> type = Utilities.getClassType(String.class);
        assertEquals(String.class, type);
    }

    @Test
    void getClassTypeFromParameterizedType() throws NoSuchMethodException {
        // Get a ParameterizedType from a method return type
        Method method = AnnotatedWithGeneric.class.getMethod("getList");
        Type genericReturnType = method.getGenericReturnType();
        assertTrue(genericReturnType instanceof ParameterizedType);
        Class<?> type = Utilities.getClassType(genericReturnType);
        assertEquals(List.class, type);
    }

    @Test
    void getClassTypeFromGenericArrayType() throws NoSuchMethodException {
        // Get a GenericArrayType
        Method method = AnnotatedWithGeneric.class.getMethod("getArray");
        Type genericReturnType = method.getGenericReturnType();
        assertTrue(genericReturnType instanceof GenericArrayType);
        Class<?> type = Utilities.getClassType(genericReturnType);
        assertEquals(Object.class, type);
    }

    @Test
    void getClassTypeFromTypeVariable() throws NoSuchMethodException {
        // Get a TypeVariable
        Method method = AnnotatedWithGeneric.class.getMethod("getT");
        Type genericReturnType = method.getGenericReturnType();
        assertTrue(genericReturnType instanceof TypeVariable);
        Class<?> type = Utilities.getClassType(genericReturnType);
        assertNotNull(type);
    }

    @Test
    void getClassTypeFromWildcardType() throws NoSuchMethodException {
        // Get a WildcardType from a bounded generic parameter
        Method method = AnnotatedWithGeneric.class.getMethod("getWildcard");
        Type genericReturnType = method.getGenericReturnType();
        // The return type is List<? extends Number>
        assertTrue(genericReturnType instanceof ParameterizedType);
        Type wildcard = ((ParameterizedType) genericReturnType).getActualTypeArguments()[0];
        assertTrue(wildcard instanceof WildcardType);
        Class<?> type = Utilities.getClassType(wildcard);
        assertEquals(Number.class, type);
    }

    @Test
    void containsReferenceReturnsTrueWhenFound() {
        Object obj = new Object();
        List<Object> list = Arrays.asList(new Object(), obj, new Object());
        assertTrue(Utilities.containsReference(list, obj));
    }

    @Test
    void containsReferenceReturnsFalseWhenNotFound() {
        Object obj = new Object();
        List<Object> list = Arrays.asList(new Object(), new Object());
        // containsReference uses == (identity), obj is not in list
        boolean result = Utilities.containsReference(list, obj);
        // We can't guarantee false for equal objects, but new instances should differ
        assertEquals(false, result);
    }

    @Test
    void containsReferenceReturnsTrueForNullInList() {
        List<Object> list = Arrays.asList(null, new Object());
        assertTrue(Utilities.containsReference(list, null));
    }

    @Test
    void getClassFileLocationPathFromClass() {
        // Use a class that has a code source (from a jar or classpath)
        String path = Utilities.getClassFileLocationPath(UtilitiesTest.class);
        assertNotNull(path);
        assertFalse(path.isEmpty());
    }

    @Test
    void getClassFileLocationPathFromCodeSource() throws Exception {
        CodeSource codeSource = UtilitiesTest.class.getProtectionDomain().getCodeSource();
        if (codeSource != null) {
            String path = Utilities.getClassFileLocationPath(codeSource);
            assertNotNull(path);
            assertFalse(path.isEmpty());
        }
    }

    static void assertFalse(boolean b) {
        assertEquals(false, b);
    }

    static boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    static boolean isEmpty(String[] arr) {
        return arr == null || arr.length == 0;
    }
}

class AnnotatedWithGeneric<T> {
    public List<String> getList() {
        return null;
    }

    public T[] getArray() {
        return null;
    }

    public T getT() {
        return null;
    }

    public List<? extends Number> getWildcard() {
        return null;
    }
}
