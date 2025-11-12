/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.expectations;

import static java.lang.reflect.Modifier.isNative;
import static java.lang.reflect.Modifier.isStatic;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import mockit.internal.state.TestRun;

public enum ExecutionMode {
    Regular {
        @Override
        boolean isNativeMethodToBeIgnored(int access) {
            return false;
        }

        @Override
        boolean isToExecuteRealImplementation(@Nullable Object instance) {
            return instance != null && !TestRun.mockFixture().isInstanceOfMockedClass(instance);
        }
    },

    Partial {
        @Override
        boolean isToExecuteRealImplementation(@Nullable Object instance) {
            return instance != null && !TestRun.mockFixture().isInstanceOfMockedClass(instance);
        }

        @Override
        boolean isWithRealImplementation(@Nullable Object instance) {
            return instance == null || !TestRun.getExecutingTest().isInjectableMock(instance);
        }

        @Override
        boolean isToExecuteRealObjectOverride(@NonNull Object instance) {
            return true;
        }
    },

    PerInstance {
        @Override
        boolean isStaticMethodToBeIgnored(int access) {
            return isStatic(access);
        }

        @Override
        boolean isToExecuteRealImplementation(@Nullable Object instance) {
            return instance == null || TestRun.getExecutingTest().isUnmockedInstance(instance);
        }

        @Override
        boolean isToExecuteRealObjectOverride(@NonNull Object instance) {
            return TestRun.getExecutingTest().isUnmockedInstance(instance);
        }
    };

    public final boolean isMethodToBeIgnored(int access) {
        return isStaticMethodToBeIgnored(access) || isNativeMethodToBeIgnored(access);
    }

    boolean isStaticMethodToBeIgnored(int access) {
        return false;
    }

    boolean isNativeMethodToBeIgnored(int access) {
        return isNative(access);
    }

    boolean isToExecuteRealImplementation(@Nullable Object instance) {
        return false;
    }

    boolean isWithRealImplementation(@Nullable Object instance) {
        return false;
    }

    boolean isToExecuteRealObjectOverride(@NonNull Object instance) {
        return false;
    }
}
