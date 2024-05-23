/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.reflection;

import static java.lang.reflect.Modifier.isPrivate;
import static java.lang.reflect.Modifier.isStatic;

import static mockit.internal.reflection.ParameterReflection.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import mockit.Delegate;
import mockit.internal.util.StackTrace;
import mockit.internal.util.Utilities;

import org.checkerframework.checker.nullness.qual.NonNull;

public final class MethodReflection {
    @NonNull
    public static final Pattern JAVA_LANG = Pattern.compile("java.lang.", Pattern.LITERAL);

    private MethodReflection() {
    }

    @Nullable
    public static <T> T invoke(@NonNull Class<?> theClass, @Nullable Object targetInstance, @NonNull String methodName,
            @NonNull Class<?>[] paramTypes, @NonNull Object... methodArgs) {
        Method method = findSpecifiedMethod(theClass, methodName, paramTypes);
        T result = invoke(targetInstance, method, methodArgs);
        return result;
    }

    @NonNull
    private static Method findSpecifiedMethod(@NonNull Class<?> theClass, @NonNull String methodName,
            @NonNull Class<?>[] paramTypes) {
        while (true) {
            Method declaredMethod = findSpecifiedMethodInGivenClass(theClass, methodName, paramTypes);

            if (declaredMethod != null) {
                return declaredMethod;
            }

            Class<?> superClass = theClass.getSuperclass();

            if (superClass == null || superClass == Object.class) {
                String paramTypesDesc = getParameterTypesDescription(paramTypes);
                throw new IllegalArgumentException("Specified method not found: " + methodName + paramTypesDesc);
            }

            // noinspection AssignmentToMethodParameter
            theClass = superClass;
        }
    }

    @Nullable
    private static Method findSpecifiedMethodInGivenClass(@NonNull Class<?> theClass, @NonNull String methodName,
            @NonNull Class<?>[] paramTypes) {
        for (Method declaredMethod : theClass.getDeclaredMethods()) {
            if (declaredMethod.getName().equals(methodName)) {
                Class<?>[] declaredParameterTypes = declaredMethod.getParameterTypes();
                int firstRealParameter = indexOfFirstRealParameter(declaredParameterTypes, paramTypes);

                if (firstRealParameter >= 0
                        && matchesParameterTypes(declaredMethod.getParameterTypes(), paramTypes, firstRealParameter)) {
                    return declaredMethod;
                }
            }
        }

        return null;
    }

    @Nullable
    public static <T> T invokePublicIfAvailable(@NonNull Class<?> aClass, @Nullable Object targetInstance,
            @NonNull String methodName, @NonNull Class<?>[] parameterTypes, @NonNull Object... methodArgs) {
        Method publicMethod;
        try {
            publicMethod = aClass.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException ignore) {
            return null;
        }

        T result = invoke(targetInstance, publicMethod, methodArgs);
        return result;
    }

    @Nullable
    public static <T> T invokeWithCheckedThrows(@NonNull Class<?> theClass, @Nullable Object targetInstance,
            @NonNull String methodName, @NonNull Class<?>[] paramTypes, @NonNull Object... methodArgs)
            throws Throwable {
        Method method = findSpecifiedMethod(theClass, methodName, paramTypes);
        T result = invokeWithCheckedThrows(targetInstance, method, methodArgs);
        return result;
    }

    @Nullable
    public static <T> T invoke(@Nullable Object targetInstance, @NonNull Method method, @NonNull Object... methodArgs) {
        Utilities.ensureThatMemberIsAccessible(method);

        try {
            // noinspection unchecked
            return (T) method.invoke(targetInstance, methodArgs);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (IllegalArgumentException e) {
            StackTrace.filterStackTrace(e);
            throw new IllegalArgumentException("Failure to invoke method: " + method, e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();

            if (cause instanceof Error) {
                throw (Error) cause;
            } else if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else {
                ThrowOfCheckedException.doThrow((Exception) cause);
                return null;
            }
        }
    }

    @Nullable
    public static <T> T invokeWithCheckedThrows(@Nullable Object targetInstance, @NonNull Method method,
            @NonNull Object... methodArgs) throws Throwable {
        Utilities.ensureThatMemberIsAccessible(method);

        try {
            // noinspection unchecked
            return (T) method.invoke(targetInstance, methodArgs);
        } catch (IllegalArgumentException e) {
            StackTrace.filterStackTrace(e);
            throw new IllegalArgumentException("Failure to invoke method: " + method, e);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    @Nullable
    public static <T> T invoke(@NonNull Class<?> theClass, @Nullable Object targetInstance, @NonNull String methodName,
            @NonNull Object... methodArgs) {
        boolean staticMethod = targetInstance == null;
        Class<?>[] argTypes = getArgumentTypesFromArgumentValues(methodArgs);
        Method method = staticMethod ? findCompatibleStaticMethod(theClass, methodName, argTypes)
                : findCompatibleMethod(theClass, methodName, argTypes);

        if (staticMethod && !isStatic(method.getModifiers())) {
            throw new IllegalArgumentException(
                    "Attempted to invoke non-static method without an instance to invoke it on");
        }

        T result = invoke(targetInstance, method, methodArgs);
        return result;
    }

    @NonNull
    private static Method findCompatibleStaticMethod(@NonNull Class<?> theClass, @NonNull String methodName,
            @NonNull Class<?>[] argTypes) {
        Method methodFound = findCompatibleMethodInClass(theClass, methodName, argTypes);

        if (methodFound != null) {
            return methodFound;
        }

        String argTypesDesc = getParameterTypesDescription(argTypes);
        throw new IllegalArgumentException("No compatible static method found: " + methodName + argTypesDesc);
    }

    @NonNull
    public static Method findCompatibleMethod(@NonNull Class<?> theClass, @NonNull String methodName,
            @NonNull Class<?>[] argTypes) {
        Method methodFound = findCompatibleMethodIfAvailable(theClass, methodName, argTypes);

        if (methodFound != null) {
            return methodFound;
        }

        String argTypesDesc = getParameterTypesDescription(argTypes);
        throw new IllegalArgumentException("No compatible method found: " + methodName + argTypesDesc);
    }

    @Nullable
    private static Method findCompatibleMethodIfAvailable(@NonNull Class<?> theClass, @NonNull String methodName,
            @NonNull Class<?>[] argTypes) {
        Method methodFound = null;

        while (true) {
            Method compatibleMethod = findCompatibleMethodInClass(theClass, methodName, argTypes);

            if (compatibleMethod != null && (methodFound == null
                    || hasMoreSpecificTypes(compatibleMethod.getParameterTypes(), methodFound.getParameterTypes()))) {
                methodFound = compatibleMethod;
            }

            Class<?> superClass = theClass.getSuperclass();

            if (superClass == null || superClass == Object.class) {
                break;
            }

            // noinspection AssignmentToMethodParameter
            theClass = superClass;
        }

        return methodFound;
    }

    @Nullable
    private static Method findCompatibleMethodInClass(@NonNull Class<?> theClass, @NonNull String methodName,
            @NonNull Class<?>[] argTypes) {
        Method found = null;
        Class<?>[] foundParamTypes = null;

        for (Method declaredMethod : theClass.getDeclaredMethods()) {
            if (declaredMethod.getName().equals(methodName)) {
                Class<?>[] declaredParamTypes = declaredMethod.getParameterTypes();
                int firstRealParameter = indexOfFirstRealParameter(declaredParamTypes, argTypes);

                if (firstRealParameter >= 0
                        && (matchesParameterTypes(declaredParamTypes, argTypes, firstRealParameter)
                                || acceptsArgumentTypes(declaredParamTypes, argTypes, firstRealParameter))
                        && (foundParamTypes == null || hasMoreSpecificTypes(declaredParamTypes, foundParamTypes))) {
                    found = declaredMethod;
                    foundParamTypes = declaredParamTypes;
                }
            }
        }

        return found;
    }

    @NonNull
    public static Method findNonPrivateHandlerMethod(@NonNull Object handler) {
        Class<?> handlerClass = handler.getClass();
        Method nonPrivateMethod;

        do {
            nonPrivateMethod = findNonPrivateHandlerMethod(handlerClass);

            if (nonPrivateMethod != null) {
                break;
            }

            handlerClass = handlerClass.getSuperclass();
        } while (handlerClass != null && handlerClass != Object.class);

        if (nonPrivateMethod == null) {
            throw new IllegalArgumentException("No non-private instance method found");
        }

        return nonPrivateMethod;
    }

    @Nullable
    private static Method findNonPrivateHandlerMethod(@NonNull Class<?> handlerClass) {
        Method[] declaredMethods = handlerClass.getDeclaredMethods();
        Method found = null;

        for (Method declaredMethod : declaredMethods) {
            int methodModifiers = declaredMethod.getModifiers();

            if (!isPrivate(methodModifiers) && !isStatic(methodModifiers)) {
                if (found != null) {
                    String methodType = Delegate.class.isAssignableFrom(handlerClass) ? "delegate"
                            : "invocation handler";
                    throw new IllegalArgumentException("More than one candidate " + methodType + " method found: "
                            + methodSignature(found) + ", " + methodSignature(declaredMethod));
                }

                found = declaredMethod;
            }
        }

        return found;
    }

    @NonNull
    private static String methodSignature(@NonNull Method method) {
        String signature = JAVA_LANG.matcher(method.toGenericString()).replaceAll("");
        int p = signature.lastIndexOf('(');
        int q = signature.lastIndexOf('.', p);

        return signature.substring(q + 1);
    }
}
