/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.reflection;

import static mockit.internal.reflection.ParameterReflection.getParameterTypesDescription;
import static mockit.internal.reflection.ParameterReflection.indexOfFirstRealParameter;
import static mockit.internal.reflection.ParameterReflection.matchesParameterTypes;
import static mockit.internal.util.Utilities.ensureThatMemberIsAccessible;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.objenesis.instantiator.sun.SunReflectionFactoryInstantiator;

public final class ConstructorReflection {
    private ConstructorReflection() {
    }

    @NonNull
    static <T> Constructor<T> findSpecifiedConstructor(@NonNull Class<?> theClass, @NonNull Class<?>[] paramTypes) {
        for (Constructor<?> declaredConstructor : theClass.getDeclaredConstructors()) {
            Class<?>[] declaredParameterTypes = declaredConstructor.getParameterTypes();
            int firstRealParameter = indexOfFirstRealParameter(declaredParameterTypes, paramTypes);

            if (firstRealParameter >= 0
                    && matchesParameterTypes(declaredParameterTypes, paramTypes, firstRealParameter)) {
                // noinspection unchecked
                return (Constructor<T>) declaredConstructor;
            }
        }

        String paramTypesDesc = getParameterTypesDescription(paramTypes);

        throw new IllegalArgumentException(
                "Specified constructor not found: " + theClass.getSimpleName() + paramTypesDesc);
    }

    @NonNull
    public static <T> T invokeAccessible(@NonNull Constructor<T> constructor, @NonNull Object... initArgs) {
        if (!constructor.isAccessible()) {
            constructor.setAccessible(true);
        }
        try {
            return constructor.newInstance(initArgs);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            Throwable cause = e instanceof InvocationTargetException ? e.getCause() : e;

            if (cause instanceof Error) {
                throw (Error) cause;
            }
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            ThrowOfCheckedException.doThrow((Exception) cause);
            throw new IllegalStateException("Should never get here", cause);
        }
    }

    public static void newInstanceUsingCompatibleConstructor(@NonNull Class<?> aClass, @NonNull String argument)
            throws ReflectiveOperationException {
        Constructor<?> constructor = aClass.getDeclaredConstructor(String.class);
        ensureThatMemberIsAccessible(constructor);
        constructor.newInstance(argument);
    }

    @NonNull
    public static <T> T newInstanceUsingDefaultConstructor(@NonNull Class<T> aClass) {
        try {
            Constructor<T> constructor = aClass.getDeclaredConstructor();
            ensureThatMemberIsAccessible(constructor);
            return constructor.newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getTargetException());
        }
    }

    @Nullable
    public static <T> T newInstanceUsingDefaultConstructorIfAvailable(@NonNull Class<T> aClass) {
        try {
            Constructor<T> constructor = aClass.getDeclaredConstructor();
            return constructor.newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
                | InvocationTargetException ignore) {
            return null;
        }
    }

    @Nullable
    public static <T> T newInstanceUsingPublicConstructorIfAvailable(@NonNull Class<T> aClass,
            @NonNull Class<?>[] parameterTypes, @NonNull Object... initArgs) {
        Constructor<T> publicConstructor;
        try {
            publicConstructor = aClass.getConstructor(parameterTypes);
        } catch (NoSuchMethodException ignore) {
            return null;
        }

        return invokeAccessible(publicConstructor, initArgs);
    }

    @NonNull
    public static <T> T newInstanceUsingPublicDefaultConstructor(@NonNull Class<T> aClass) {
        Constructor<T> publicConstructor;
        try {
            publicConstructor = aClass.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        return invokeAccessible(publicConstructor);
    }

    @NonNull
    public static <T> T newUninitializedInstance(@NonNull Class<T> aClass) {
        SunReflectionFactoryInstantiator<T> ref = new SunReflectionFactoryInstantiator<>(aClass);
        return ref.newInstance();
    }
}
