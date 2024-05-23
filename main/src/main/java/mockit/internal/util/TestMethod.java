/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mockit.asm.types.JavaType;

import org.checkerframework.checker.index.qual.NonNegative;

public final class TestMethod {
    @Nonnull
    public final Class<?> testClass;
    @Nonnull
    public final String testClassDesc;
    @Nonnull
    public final String testMethodDesc;
    @Nonnull
    private final Type[] parameterTypes;
    @Nonnull
    private final Class<?>[] parameterClasses;
    @Nonnull
    private final Annotation[][] parameterAnnotations;
    @Nonnull
    private final Object[] parameterValues;

    public TestMethod(@Nonnull Method testMethod, @Nonnull Object[] parameterValues) {
        testClass = testMethod.getDeclaringClass();
        testClassDesc = JavaType.getInternalName(testClass);
        testMethodDesc = testMethod.getName() + JavaType.getMethodDescriptor(testMethod);
        parameterTypes = testMethod.getGenericParameterTypes();
        parameterClasses = testMethod.getParameterTypes();
        parameterAnnotations = testMethod.getParameterAnnotations();
        this.parameterValues = parameterValues;
    }

    @NonNegative
    public int getParameterCount() {
        return parameterTypes.length;
    }

    @Nonnull
    public Type getParameterType(@NonNegative int index) {
        return parameterTypes[index];
    }

    @Nonnull
    public Class<?> getParameterClass(@NonNegative int index) {
        return parameterClasses[index];
    }

    @Nonnull
    public Annotation[] getParameterAnnotations(@NonNegative int index) {
        return parameterAnnotations[index];
    }

    @Nullable
    public Object getParameterValue(@NonNegative int index) {
        return parameterValues[index];
    }

    public void setParameterValue(@NonNegative int index, @Nullable Object value) {
        if (value != null) {
            parameterValues[index] = value;
        }
    }
}
