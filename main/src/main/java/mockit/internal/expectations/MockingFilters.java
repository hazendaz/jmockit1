/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations;

import javax.annotation.Nonnull;

import mockit.asm.types.JavaType;

public final class MockingFilters {
    private MockingFilters() {
    }

    public static void validateAsMockable(@Nonnull Class<?> type) {
        String typeDesc = JavaType.getInternalName(type);
        validateAsMockable(typeDesc);
    }

    public static void validateAsMockable(@Nonnull String typeDesc) {
        boolean unmockable = ("java/lang/String java/lang/StringBuffer java/lang/StringBuilder java/lang/AbstractStringBuilder "
                + "java/lang/Throwable java/lang/Object java/lang/Enum java/lang/System java/lang/ThreadLocal "
                + "java/lang/ClassLoader java/lang/Math java/lang/StrictMath java/time/Duration").contains(typeDesc)
                || "java/nio/file/Paths".equals(typeDesc) || typeDesc.startsWith("java/util/jar/");

        if (unmockable) {
            throw new IllegalArgumentException(typeDesc.replace('/', '.') + " is not mockable");
        }
    }

    public static boolean isSubclassOfUnmockable(@Nonnull Class<?> aClass) {
        return Throwable.class.isAssignableFrom(aClass) || ClassLoader.class.isAssignableFrom(aClass)
                || ThreadLocal.class.isAssignableFrom(aClass) || Number.class.isAssignableFrom(aClass);
    }
}
