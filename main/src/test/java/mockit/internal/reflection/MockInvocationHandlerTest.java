/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.reflection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.junit.jupiter.api.Test;

final class MockInvocationHandlerTest {

    interface SampleInterface {
        String doSomething();

        int getValue();
    }

    @Test
    void instanceIsNotNull() {
        assertNotNull(MockInvocationHandler.INSTANCE);
    }

    @Test
    void newMockedInstanceCreatesProxyForInterface() {
        SampleInterface proxy = (SampleInterface) Proxy.newProxyInstance(SampleInterface.class.getClassLoader(),
                new Class<?>[] { SampleInterface.class }, MockInvocationHandler.INSTANCE);
        Class<?> proxyClass = proxy.getClass();
        Object instance = MockInvocationHandler.newMockedInstance(proxyClass);
        assertNotNull(instance);
        assertTrue(instance instanceof SampleInterface);
    }

    @Test
    void invokeEqualsWithSelf() throws Throwable {
        SampleInterface proxy = (SampleInterface) Proxy.newProxyInstance(SampleInterface.class.getClassLoader(),
                new Class<?>[] { SampleInterface.class }, MockInvocationHandler.INSTANCE);

        Method equalsMethod = Object.class.getMethod("equals", Object.class);
        Object result = MockInvocationHandler.INSTANCE.invoke(proxy, equalsMethod, new Object[] { proxy });

        assertTrue((Boolean) result);
    }

    @Test
    void invokeEqualsWithDifferentObject() throws Throwable {
        SampleInterface proxy = (SampleInterface) Proxy.newProxyInstance(SampleInterface.class.getClassLoader(),
                new Class<?>[] { SampleInterface.class }, MockInvocationHandler.INSTANCE);

        Method equalsMethod = Object.class.getMethod("equals", Object.class);
        Object result = MockInvocationHandler.INSTANCE.invoke(proxy, equalsMethod, new Object[] { "different" });

        assertFalse((Boolean) result);
    }

    @Test
    void invokeHashCode() throws Throwable {
        SampleInterface proxy = (SampleInterface) Proxy.newProxyInstance(SampleInterface.class.getClassLoader(),
                new Class<?>[] { SampleInterface.class }, MockInvocationHandler.INSTANCE);

        Method hashCodeMethod = Object.class.getMethod("hashCode");
        Object result = MockInvocationHandler.INSTANCE.invoke(proxy, hashCodeMethod, null);

        assertEquals(System.identityHashCode(proxy), result);
    }

    @Test
    void invokeToString() throws Throwable {
        SampleInterface proxy = (SampleInterface) Proxy.newProxyInstance(SampleInterface.class.getClassLoader(),
                new Class<?>[] { SampleInterface.class }, MockInvocationHandler.INSTANCE);

        Method toStringMethod = Object.class.getMethod("toString");
        Object result = MockInvocationHandler.INSTANCE.invoke(proxy, toStringMethod, null);

        assertNotNull(result);
        assertTrue(result instanceof String);
    }

    @Test
    void invokeInterfaceMethodReturnsDefaultValue() throws Throwable {
        SampleInterface proxy = (SampleInterface) Proxy.newProxyInstance(SampleInterface.class.getClassLoader(),
                new Class<?>[] { SampleInterface.class }, MockInvocationHandler.INSTANCE);

        Method doSomethingMethod = SampleInterface.class.getMethod("doSomething");
        Object result = MockInvocationHandler.INSTANCE.invoke(proxy, doSomethingMethod, null);

        assertNull(result); // default for String is null
    }

    @Test
    void invokeInterfaceMethodReturningIntReturnsZero() throws Throwable {
        SampleInterface proxy = (SampleInterface) Proxy.newProxyInstance(SampleInterface.class.getClassLoader(),
                new Class<?>[] { SampleInterface.class }, MockInvocationHandler.INSTANCE);

        Method getValueMethod = SampleInterface.class.getMethod("getValue");
        Object result = MockInvocationHandler.INSTANCE.invoke(proxy, getValueMethod, null);

        assertEquals(0, result);
    }

    @Test
    void invokeAnnotationTypeMethodReturnsAnnotationType() throws Throwable {
        // Create a proxy for Deprecated annotation (which extends Annotation)
        Annotation proxy = (Annotation) Proxy.newProxyInstance(Annotation.class.getClassLoader(),
                new Class<?>[] { Deprecated.class }, MockInvocationHandler.INSTANCE);

        Method annotationTypeMethod = Annotation.class.getMethod("annotationType");
        Object result = MockInvocationHandler.INSTANCE.invoke(proxy, annotationTypeMethod, null);

        assertNotNull(result);
        assertEquals(Deprecated.class, result);
    }
}
