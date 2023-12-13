/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.reflection;

import static mockit.internal.reflection.ParameterReflection.getParameterTypesDescription;
import static mockit.internal.reflection.ParameterReflection.indexOfFirstRealParameter;
import static mockit.internal.reflection.ParameterReflection.matchesParameterTypes;
import static mockit.internal.util.Utilities.ensureThatMemberIsAccessible;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.objenesis.instantiator.sun.SunReflectionFactoryInstantiator;

public final class ConstructorReflection {
    private ConstructorReflection() {
    }

    @Nonnull
    static <T> Constructor<T> findSpecifiedConstructor(@Nonnull Class<?> theClass, @Nonnull Class<?>[] paramTypes) {
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

    @Nonnull
    public static <T> T invokeAccessible(@Nonnull Constructor<T> constructor, @Nonnull Object... initArgs) {
        if (!constructor.isAccessible()) {
            constructor.setAccessible(true);
        }
        try {
            return constructor.newInstance(initArgs);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();

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

    public static void newInstanceUsingCompatibleConstructor(@Nonnull Class<?> aClass, @Nonnull String argument)
            throws ReflectiveOperationException {
        Constructor<?> constructor = aClass.getDeclaredConstructor(String.class);
        ensureThatMemberIsAccessible(constructor);
        constructor.newInstance(argument);
    }

    @Nonnull
    public static <T> T newInstanceUsingDefaultConstructor(@Nonnull Class<T> aClass) {
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
    public static <T> T newInstanceUsingDefaultConstructorIfAvailable(@Nonnull Class<T> aClass) {
        try {
            Constructor<T> constructor = aClass.getDeclaredConstructor();
            return constructor.newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
                | InvocationTargetException ignore) {
            return null;
        }
    }

    @Nullable
    public static <T> T newInstanceUsingPublicConstructorIfAvailable(@Nonnull Class<T> aClass,
            @Nonnull Class<?>[] parameterTypes, @Nonnull Object... initArgs) {
        Constructor<T> publicConstructor;
        try {
            publicConstructor = aClass.getConstructor(parameterTypes);
        } catch (NoSuchMethodException ignore) {
            return null;
        }

        return invokeAccessible(publicConstructor, initArgs);
    }

    @Nonnull
    public static <T> T newInstanceUsingPublicDefaultConstructor(@Nonnull Class<T> aClass) {
        Constructor<T> publicConstructor;
        try {
            publicConstructor = aClass.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        return invokeAccessible(publicConstructor);
    }

    @Nonnull
    public static <T> T newUninitializedInstance(@Nonnull Class<T> aClass) {
        SunReflectionFactoryInstantiator<T> ref = new SunReflectionFactoryInstantiator<>(aClass);
        return ref.newInstance();
    }
}
