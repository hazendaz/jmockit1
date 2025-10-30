/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.reflection;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import mockit.internal.util.DefaultValues;
import mockit.internal.util.ObjectMethods;

/**
 * Handles invocations to all kinds of mock implementations created for interfaces and annotation types through any of
 * the mocking APIs.
 * <p>
 * The <code>java.lang.Object</code> methods <code>equals</code>, <code>hashCode</code>, and <code>toString</code> are
 * handled in a meaningful way, returning a value that makes sense for the proxy instance. The special
 * {@linkplain Annotation} contracts for these three methods is <em>not</em> observed, though, since it would require
 * making dynamic calls to the mocked annotation attributes.
 * <p>
 * Any other method invocation is handled by simply returning the default value according to the method's return type
 * (as defined in {@linkplain DefaultValues}).
 */
public final class MockInvocationHandler implements InvocationHandler {
    public static final InvocationHandler INSTANCE = new MockInvocationHandler();
    private static final Class<?>[] CONSTRUCTOR_PARAMETERS_FOR_PROXY_CLASS = { InvocationHandler.class };

    @NonNull
    public static Object newMockedInstance(@NonNull Class<?> proxyClass) {
        Constructor<?> publicConstructor;
        try {
            publicConstructor = proxyClass.getConstructor(CONSTRUCTOR_PARAMETERS_FOR_PROXY_CLASS);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        return ConstructorReflection.invokeAccessible(publicConstructor, INSTANCE);
    }

    @Nullable
    @Override
    public Object invoke(@NonNull Object proxy, @NonNull Method method, @Nullable Object[] args) {
        Class<?> declaringClass = method.getDeclaringClass();
        String methodName = method.getName();

        if (declaringClass == Object.class) {
            if ("equals".equals(methodName)) {
                return args != null && args.length > 0 && proxy == args[0];
            }
            if ("hashCode".equals(methodName)) {
                return System.identityHashCode(proxy);
            }
            // "toString"
            return ObjectMethods.objectIdentity(proxy);
        }

        if (declaringClass == Annotation.class) {
            return proxy.getClass().getInterfaces()[0];
        }

        Class<?> retType = method.getReturnType();
        return DefaultValues.computeForType(retType);
    }
}
