/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.util;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.lang.reflect.Proxy;

public final class GeneratedClasses {
    private static final String IMPLCLASS_PREFIX = "$Impl_";
    private static final String SUBCLASS_PREFIX = "$Subclass_";

    private GeneratedClasses() {
    }

    @NonNull
    public static String getNameForGeneratedClass(@NonNull Class<?> aClass, @Nullable String suffix) {
        String prefix = aClass.isInterface() ? IMPLCLASS_PREFIX : SUBCLASS_PREFIX;
        StringBuilder name = new StringBuilder(60).append(prefix).append(aClass.getSimpleName());

        if (suffix != null) {
            name.append('_').append(suffix);
        }

        if (aClass.getClassLoader() != null) {
            Package targetPackage = aClass.getPackage();

            if (targetPackage != null && !targetPackage.isSealed() && !targetPackage.getName().isEmpty()) {
                name.insert(0, '.').insert(0, targetPackage.getName());
            }
        }

        return name.toString();
    }

    public static boolean isGeneratedImplementationClass(@NonNull Class<?> mockedType) {
        return isGeneratedImplementationClassName(mockedType.getName());
    }

    public static boolean isGeneratedImplementationClassName(@NonNull String className) {
        return className.contains(IMPLCLASS_PREFIX);
    }

    private static boolean isGeneratedSubclass(@NonNull String className) {
        return className.contains(SUBCLASS_PREFIX);
    }

    public static boolean isExternallyGeneratedSubclass(@NonNull String className) {
        int p = className.indexOf('$') + 1;

        // noinspection SimplifiableIfStatement
        if (p < 2 || p == className.length() || className.charAt(p) != '$') {
            return false;
        }

        return className.contains("_$$_javassist_") || className.contains("_$$_jvst") || className.contains("CGLIB$$");
    }

    public static boolean isGeneratedClass(@NonNull String className) {
        return isGeneratedSubclass(className) || isGeneratedImplementationClassName(className);
    }

    @NonNull
    public static Class<?> getMockedClassOrInterfaceType(@NonNull Class<?> aClass) {
        if (Proxy.isProxyClass(aClass) || isGeneratedImplementationClass(aClass)) {
            // Assumes that a proxy class implements a single interface.
            return aClass.getInterfaces()[0];
        }

        if (isGeneratedSubclass(aClass.getName())) {
            return aClass.getSuperclass();
        }

        return aClass;
    }

    @NonNull
    public static Class<?> getMockedClass(@NonNull Object mock) {
        return getMockedClassOrInterfaceType(mock.getClass());
    }
}
