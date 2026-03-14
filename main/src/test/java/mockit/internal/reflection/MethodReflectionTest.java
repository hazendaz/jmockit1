/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.reflection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

final class MethodReflectionTest {

    static class SampleClass {
        public String publicMethod(String s) {
            return "public-" + s;
        }

        public String throwingMethod(String msg) {
            throw new RuntimeException(msg);
        }

        public String throwingCheckedException() throws Exception {
            throw new Exception("checked");
        }

        private static String privateStatic() {
            return "static-result";
        }

        public int add(int a, int b) {
            return a + b;
        }
    }

    @Test
    void invokePublicIfAvailableWhenMethodExists() {
        SampleClass obj = new SampleClass();
        String result = MethodReflection.invokePublicIfAvailable(SampleClass.class, obj, "publicMethod",
                new Class<?>[] { String.class }, "test");
        assertEquals("public-test", result);
    }

    @Test
    void invokePublicIfAvailableWhenMethodNotFound() {
        SampleClass obj = new SampleClass();
        Object result = MethodReflection.invokePublicIfAvailable(SampleClass.class, obj, "nonExistentMethod",
                new Class<?>[] { String.class }, "test");
        assertNull(result);
    }

    @Test
    void invokeWithCheckedThrowsInvokesMethod() throws Throwable {
        SampleClass obj = new SampleClass();
        String result = MethodReflection.invokeWithCheckedThrows(SampleClass.class, obj, "publicMethod",
                new Class<?>[] { String.class }, "hello");
        assertEquals("public-hello", result);
    }

    @Test
    void invokeWithCheckedThrowsRethrowsCause() {
        SampleClass obj = new SampleClass();
        assertThrows(Exception.class, () -> MethodReflection.invokeWithCheckedThrows(SampleClass.class, obj,
                "throwingCheckedException", new Class<?>[0]));
    }

    @Test
    void invokeMethodOnInstanceViaMethod() throws NoSuchMethodException {
        SampleClass obj = new SampleClass();
        Method method = SampleClass.class.getMethod("publicMethod", String.class);
        String result = MethodReflection.invoke(obj, method, "world");
        assertEquals("public-world", result);
    }

    @Test
    void invokeThrowingRuntimeExceptionRethrowsIt() throws NoSuchMethodException {
        SampleClass obj = new SampleClass();
        Method method = SampleClass.class.getMethod("throwingMethod", String.class);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> MethodReflection.invoke(obj, method, "boom"));
        assertEquals("boom", ex.getMessage());
    }

    @Test
    void findCompatibleMethodUsedByDeencapsulation() {
        SampleClass obj = new SampleClass();
        Method method = MethodReflection.findCompatibleMethod(SampleClass.class, "publicMethod",
                new Class<?>[] { String.class });
        assertEquals("publicMethod", method.getName());
    }
}
