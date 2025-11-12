/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.reflection;

import static mockit.internal.reflection.MethodReflection.JAVA_LANG;
import static mockit.internal.util.Utilities.JAVA8;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.lang.reflect.Method;

import mockit.Invocation;
import mockit.internal.util.AutoBoxing;
import mockit.internal.util.GeneratedClasses;

import org.checkerframework.checker.index.qual.NonNegative;

public final class ParameterReflection {
    @NonNull
    public static final Class<?>[] NO_PARAMETERS = {};

    private ParameterReflection() {
    }

    @NonNull
    static String getParameterTypesDescription(@NonNull Class<?>[] paramTypes) {
        StringBuilder paramTypesDesc = new StringBuilder(200);
        paramTypesDesc.append('(');

        String sep = "";

        for (Class<?> paramType : paramTypes) {
            String typeName = JAVA_LANG.matcher(paramType.getCanonicalName()).replaceAll("");
            paramTypesDesc.append(sep).append(typeName);
            sep = ", ";
        }

        paramTypesDesc.append(')');
        return paramTypesDesc.toString();
    }

    @NonNull
    public static Class<?>[] getArgumentTypesFromArgumentValues(@NonNull Object... args) {
        if (args.length == 0) {
            return NO_PARAMETERS;
        }

        Class<?>[] argTypes = new Class<?>[args.length];

        for (int i = 0; i < args.length; i++) {
            argTypes[i] = getArgumentTypeFromArgumentValue(i, args);
        }

        return argTypes;
    }

    @NonNull
    private static Class<?> getArgumentTypeFromArgumentValue(int i, @NonNull Object[] args) {
        Object arg = args[i];

        if (arg == null) {
            throw new IllegalArgumentException("Invalid null value passed as argument " + i);
        }

        Class<?> argType;

        if (arg instanceof Class<?>) {
            argType = (Class<?>) arg;
            args[i] = null;
        } else {
            argType = GeneratedClasses.getMockedClass(arg);
        }

        return argType;
    }

    @NonNull
    public static Object[] argumentsWithExtraFirstValue(@NonNull Object[] args, @NonNull Object firstValue) {
        Object[] args2 = new Object[1 + args.length];
        args2[0] = firstValue;
        System.arraycopy(args, 0, args2, 1, args.length);
        return args2;
    }

    static boolean hasMoreSpecificTypes(@NonNull Class<?>[] currentTypes, @NonNull Class<?>[] previousTypes) {
        for (int i = 0; i < currentTypes.length; i++) {
            Class<?> current = wrappedIfPrimitive(currentTypes[i]);
            Class<?> previous = wrappedIfPrimitive(previousTypes[i]);

            if (current != previous && previous.isAssignableFrom(current)) {
                return true;
            }
        }

        return false;
    }

    @NonNull
    private static Class<?> wrappedIfPrimitive(@NonNull Class<?> parameterType) {
        if (parameterType.isPrimitive()) {
            Class<?> wrapperType = AutoBoxing.getWrapperType(parameterType);
            assert wrapperType != null;
            return wrapperType;
        }

        return parameterType;
    }

    static boolean acceptsArgumentTypes(@NonNull Class<?>[] paramTypes, @NonNull Class<?>[] argTypes,
            int firstParameter) {
        for (int i = firstParameter; i < paramTypes.length; i++) {
            Class<?> parType = paramTypes[i];
            Class<?> argType = argTypes[i - firstParameter];

            if (!isSameTypeIgnoringAutoBoxing(parType, argType) && !parType.isAssignableFrom(argType)) {
                return false;
            }
        }

        return true;
    }

    static boolean isSameTypeIgnoringAutoBoxing(@NonNull Class<?> firstType, @NonNull Class<?> secondType) {
        return firstType == secondType || firstType.isPrimitive() && isWrapperOfPrimitiveType(firstType, secondType)
                || secondType.isPrimitive() && isWrapperOfPrimitiveType(secondType, firstType);
    }

    private static boolean isWrapperOfPrimitiveType(@NonNull Class<?> primitiveType, @NonNull Class<?> otherType) {
        return primitiveType == AutoBoxing.getPrimitiveType(otherType);
    }

    static int indexOfFirstRealParameter(@NonNull Class<?>[] mockParameterTypes,
            @NonNull Class<?>[] realParameterTypes) {
        int extraParameters = mockParameterTypes.length - realParameterTypes.length;

        if (extraParameters == 1) {
            return mockParameterTypes[0] == Invocation.class ? 1 : -1;
        }

        if (extraParameters != 0) {
            return -1;
        }

        return 0;
    }

    static boolean matchesParameterTypes(@NonNull Class<?>[] declaredTypes, @NonNull Class<?>[] specifiedTypes,
            int firstParameter) {
        for (int i = firstParameter; i < declaredTypes.length; i++) {
            Class<?> declaredType = declaredTypes[i];
            Class<?> specifiedType = specifiedTypes[i - firstParameter];

            if (!isSameTypeIgnoringAutoBoxing(declaredType, specifiedType)) {
                return false;
            }
        }

        return true;
    }

    @NonNegative
    public static int getParameterCount(@NonNull Method method) {
        // noinspection Since15
        return JAVA8 ? method.getParameterCount() : method.getParameterTypes().length;
    }
}
