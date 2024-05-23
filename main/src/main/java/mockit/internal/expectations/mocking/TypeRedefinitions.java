/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.mocking;

import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;

import mockit.internal.state.TestRun;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class TypeRedefinitions {
    @NonNull
    private final List<Class<?>> targetClasses;
    @Nullable
    protected CaptureOfNewInstances captureOfNewInstances;

    TypeRedefinitions() {
        targetClasses = new ArrayList<>(2);
    }

    final void addTargetClass(@NonNull MockedType mockedType) {
        Class<?> targetClass = mockedType.getClassType();

        if (targetClass != TypeVariable.class) {
            targetClasses.add(targetClass);
        }
    }

    @NonNull
    public final List<Class<?>> getTargetClasses() {
        return targetClasses;
    }

    @Nullable
    public final CaptureOfNewInstances getCaptureOfNewInstances() {
        return captureOfNewInstances;
    }

    static void registerMock(@NonNull MockedType mockedType, @NonNull Object mock) {
        TestRun.getExecutingTest().registerMock(mockedType, mock);
    }

    public void cleanUp() {
        if (captureOfNewInstances != null) {
            captureOfNewInstances.cleanUp();
            captureOfNewInstances = null;
        }
    }
}
