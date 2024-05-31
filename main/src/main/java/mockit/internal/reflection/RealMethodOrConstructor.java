/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.reflection;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import mockit.internal.util.ClassLoad;
import mockit.internal.util.TypeDescriptor;

public final class RealMethodOrConstructor {
    @NonNull
    public final Member member;

    public RealMethodOrConstructor(@NonNull String className, @NonNull String memberNameAndDesc)
            throws NoSuchMethodException {
        this(ClassLoad.loadFromLoader(RealMethodOrConstructor.class.getClassLoader(), className), memberNameAndDesc);
    }

    public RealMethodOrConstructor(@NonNull Class<?> realClass, @NonNull String memberNameAndDesc)
            throws NoSuchMethodException {
        int p = memberNameAndDesc.indexOf('(');
        String memberDesc = memberNameAndDesc.substring(p);

        if (memberNameAndDesc.charAt(0) == '<') {
            member = findConstructor(realClass, memberDesc);
        } else {
            String methodName = memberNameAndDesc.substring(0, p);
            member = findMethod(realClass, methodName, memberDesc);
        }
    }

    public RealMethodOrConstructor(@NonNull Class<?> realClass, @NonNull String memberName, @NonNull String memberDesc)
            throws NoSuchMethodException {
        if (memberName.charAt(0) == '<') {
            member = findConstructor(realClass, memberDesc);
        } else {
            member = findMethod(realClass, memberName, memberDesc);
        }
    }

    @NonNull
    private static Constructor<?> findConstructor(@NonNull Class<?> realClass, @NonNull String constructorDesc) {
        Class<?>[] parameterTypes = TypeDescriptor.getParameterTypes(constructorDesc);
        return ConstructorReflection.findSpecifiedConstructor(realClass, parameterTypes);
    }

    @NonNull
    private static Method findMethod(@NonNull Class<?> realClass, @NonNull String methodName,
            @NonNull String methodDesc) throws NoSuchMethodException {
        Class<?>[] parameterTypes = TypeDescriptor.getParameterTypes(methodDesc);
        Class<?> ownerClass = realClass;

        while (true) {
            try {
                Method method = ownerClass.getDeclaredMethod(methodName, parameterTypes);

                if (method.isBridge()) {
                    ownerClass = ownerClass.getSuperclass();
                    continue;
                }

                return method;
            } catch (NoSuchMethodException e) {
                Method interfaceMethod = findInterfaceMethod(ownerClass, methodName, parameterTypes);

                if (interfaceMethod != null) {
                    return interfaceMethod;
                }

                ownerClass = ownerClass.getSuperclass();

                if (ownerClass == null || ownerClass == Object.class) {
                    throw e;
                }
            }
        }
    }

    @Nullable
    private static Method findInterfaceMethod(@NonNull Class<?> aType, @NonNull String methodName,
            @NonNull Class<?>[] parameterTypes) {
        for (Class<?> anInterface : aType.getInterfaces()) {
            try {
                return anInterface.getMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException ignore) {
            }
        }

        return null;
    }

    @NonNull
    public <M extends Member> M getMember() {
        // noinspection unchecked
        return (M) member;
    }
}
