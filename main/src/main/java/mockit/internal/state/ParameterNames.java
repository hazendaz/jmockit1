/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.state;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mockit.internal.util.TestMethod;

import org.checkerframework.checker.index.qual.NonNegative;

public final class ParameterNames {
    private static final Map<String, Map<String, String[]>> classesToMethodsToParameters = new HashMap<>();

    private ParameterNames() {
    }

    public static boolean hasNamesForClass(@Nonnull String classDesc) {
        return classesToMethodsToParameters.containsKey(classDesc);
    }

    public static void register(@Nonnull String classDesc, @Nonnull String memberName, @Nonnull String memberDesc,
            @Nonnull String[] names) {
        Map<String, String[]> methodsToParameters = classesToMethodsToParameters.get(classDesc);

        if (methodsToParameters == null) {
            methodsToParameters = new HashMap<>();
            classesToMethodsToParameters.put(classDesc, methodsToParameters);
        }

        String methodKey = memberName + memberDesc;
        methodsToParameters.put(methodKey, names);
    }

    @Nonnull
    public static String getName(@Nonnull TestMethod method, @NonNegative int index) {
        String name = getName(method.testClassDesc, method.testMethodDesc, index);
        return name == null ? "param" + index : name;
    }

    @Nullable
    public static String getName(@Nonnull String classDesc, @Nonnull String methodDesc, @NonNegative int index) {
        Map<String, String[]> methodsToParameters = classesToMethodsToParameters.get(classDesc);

        if (methodsToParameters == null) {
            return null;
        }

        String[] parameterNames = methodsToParameters.get(methodDesc);
        return parameterNames[index];
    }
}
