/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.reflection;

import static mockit.internal.reflection.ParameterReflection.NO_PARAMETERS;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class AnnotationReflection {
    private AnnotationReflection() {
    }

    @NonNull
    public static String readAnnotationAttribute(@NonNull Object annotationInstance, @NonNull String attributeName) {
        try {
            return readAttribute(annotationInstance, attributeName);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public static String readAnnotationAttributeIfAvailable(@NonNull Object annotationInstance,
            @NonNull String attributeName) {
        try {
            return readAttribute(annotationInstance, attributeName);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    @NonNull
    private static String readAttribute(@NonNull Object annotationInstance, @NonNull String attributeName)
            throws NoSuchMethodException {
        try {
            Method publicMethod = annotationInstance.getClass().getMethod(attributeName, NO_PARAMETERS);
            return (String) publicMethod.invoke(annotationInstance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        }
    }
}
