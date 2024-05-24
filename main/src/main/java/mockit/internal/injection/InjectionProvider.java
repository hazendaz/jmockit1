/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.injection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Provides type, name, and value(s) for an injection point, which is either a field to be injected or a parameter in
 * the chosen constructor of a tested class.
 */
public abstract class InjectionProvider {
    public static final Object NULL = Void.class;

    @NonNull
    protected final Type declaredType;
    @NonNull
    protected final String name;
    @Nullable
    public InjectionProvider parent;

    protected InjectionProvider(@NonNull Type declaredType, @NonNull String name) {
        this.declaredType = declaredType;
        this.name = name;
    }

    @NonNull
    public final Type getDeclaredType() {
        return declaredType;
    }

    @NonNull
    public abstract Class<?> getClassOfDeclaredType();

    @NonNull
    public final String getName() {
        return name;
    }

    @NonNull
    public Annotation[] getAnnotations() {
        throw new UnsupportedOperationException("No annotations");
    }

    @Nullable
    public Object getValue(@Nullable Object owner) {
        return null;
    }

    public boolean isRequired() {
        return false;
    }

    public boolean hasAnnotation(@NonNull Class<? extends Annotation> annotationOfInterest) {
        for (Annotation annotation : getAnnotations()) {
            if (annotationOfInterest.isInstance(annotation)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        Class<?> type = getClassOfDeclaredType();
        StringBuilder description = new StringBuilder().append('"').append(type.getSimpleName()).append(' ')
                .append(name).append('"');

        if (parent != null) {
            description.append("\r\n  when initializing ").append(parent);
        }

        return description.toString();
    }
}
