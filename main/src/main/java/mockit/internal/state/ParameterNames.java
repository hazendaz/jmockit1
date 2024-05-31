/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.state;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import mockit.internal.util.TestMethod;

import org.checkerframework.checker.index.qual.NonNegative;

public final class ParameterNames {
    private static final Map<String, Map<String, String[]>> classesToMethodsToParameters = new HashMap<>();

    private ParameterNames() {
    }

    public static boolean hasNamesForClass(@NonNull String classDesc) {
        return classesToMethodsToParameters.containsKey(classDesc);
    }

    public static void register(@NonNull String classDesc, @NonNull String memberName, @NonNull String memberDesc,
            @NonNull String[] names) {
        Map<String, String[]> methodsToParameters = classesToMethodsToParameters.get(classDesc);

        if (methodsToParameters == null) {
            methodsToParameters = new HashMap<>();
            classesToMethodsToParameters.put(classDesc, methodsToParameters);
        }

        String methodKey = memberName + memberDesc;
        methodsToParameters.put(methodKey, names);
    }

    @NonNull
    public static String getName(@NonNull TestMethod method, @NonNegative int index) {
        String name = getName(method.testClassDesc, method.testMethodDesc, index);
        return name == null ? "param" + index : name;
    }

    @Nullable
    public static String getName(@NonNull String classDesc, @NonNull String methodDesc, @NonNegative int index) {
        Map<String, String[]> methodsToParameters = classesToMethodsToParameters.get(classDesc);

        if (methodsToParameters == null) {
            return null;
        }

        String[] parameterNames = methodsToParameters.get(methodDesc);
        return parameterNames[index];
    }
}
