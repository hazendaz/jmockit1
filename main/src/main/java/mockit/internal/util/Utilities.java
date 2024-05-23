/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.CodeSource;
import java.util.List;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Miscellaneous utility constants and methods.
 */
public final class Utilities {
    @NonNull
    public static final Object[] NO_ARGS = {};
    public static final boolean JAVA8;
    public static final boolean HOTSPOT_VM;

    static {
        float javaVersion = Float.parseFloat(System.getProperty("java.specification.version"));
        JAVA8 = javaVersion >= 1.8F;
        String vmName = System.getProperty("java.vm.name");
        HOTSPOT_VM = vmName.contains("HotSpot") || vmName.contains("OpenJDK");
    }

    private Utilities() {
    }

    public static void ensureThatMemberIsAccessible(@NonNull AccessibleObject classMember) {
        // noinspection deprecation
        if (!classMember.isAccessible()) {
            classMember.setAccessible(true);
        }
    }

    public static Method getAnnotatedMethod(Class<?> cls, Class<? extends Annotation> annotation) {
        for (Method method : cls.getMethods()) {
            if (method.getAnnotation(annotation) != null) {
                return method;
            }
        }
        return null;
    }

    public static Method getAnnotatedDeclaredMethod(Class<?> cls, Class<? extends Annotation> annotation) {
        for (Method method : cls.getDeclaredMethods()) {
            if (method.getAnnotation(annotation) != null) {
                return method;
            }
        }
        return null;
    }

    @NonNull
    public static Class<?> getClassType(@NonNull Type declaredType) {
        while (true) {
            if (declaredType instanceof Class<?>) {
                return (Class<?>) declaredType;
            }

            if (declaredType instanceof ParameterizedType) {
                return (Class<?>) ((ParameterizedType) declaredType).getRawType();
            }

            if (declaredType instanceof GenericArrayType) {
                declaredType = ((GenericArrayType) declaredType).getGenericComponentType();
                continue;
            }

            if (declaredType instanceof TypeVariable) {
                declaredType = ((TypeVariable<?>) declaredType).getBounds()[0];
                continue;
            }

            if (declaredType instanceof WildcardType) {
                declaredType = ((WildcardType) declaredType).getUpperBounds()[0];
                continue;
            }

            throw new IllegalArgumentException("Type of unexpected kind: " + declaredType);
        }
    }

    public static boolean containsReference(@NonNull List<?> references, @Nullable Object toBeFound) {
        for (Object reference : references) {
            if (reference == toBeFound) {
                return true;
            }
        }

        return false;
    }

    @NonNull
    public static String getClassFileLocationPath(@NonNull Class<?> aClass) {
        CodeSource codeSource = aClass.getProtectionDomain().getCodeSource();
        return getClassFileLocationPath(codeSource);
    }

    @NonNull
    public static String getClassFileLocationPath(@NonNull CodeSource codeSource) {
        String locationPath = codeSource.getLocation().getPath();
        return URLDecoder.decode(locationPath, StandardCharsets.UTF_8);
    }
}
