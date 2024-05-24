/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import mockit.asm.types.JavaType;

import org.checkerframework.checker.index.qual.NonNegative;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public final class TestMethod {
    @NonNull
    public final Class<?> testClass;
    @NonNull
    public final String testClassDesc;
    @NonNull
    public final String testMethodDesc;
    @NonNull
    private final Type[] parameterTypes;
    @NonNull
    private final Class<?>[] parameterClasses;
    @NonNull
    private final Annotation[][] parameterAnnotations;
    @NonNull
    private final Object[] parameterValues;

    public TestMethod(@NonNull Method testMethod, @NonNull Object[] parameterValues) {
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

    @NonNull
    public Type getParameterType(@NonNegative int index) {
        return parameterTypes[index];
    }

    @NonNull
    public Class<?> getParameterClass(@NonNegative int index) {
        return parameterClasses[index];
    }

    @NonNull
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
