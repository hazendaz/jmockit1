/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.injection.constructor;

import static mockit.internal.util.Utilities.getClassType;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import mockit.internal.injection.InjectionProvider;

final class ConstructorParameter extends InjectionProvider {
    @NonNull
    private final Class<?> classOfDeclaredType;
    @NonNull
    private final Annotation[] annotations;
    @Nullable
    private final Object value;

    ConstructorParameter(@NonNull Type declaredType, @NonNull Annotation[] annotations, @NonNull String name,
            @Nullable Object value) {
        super(declaredType, name);
        classOfDeclaredType = getClassType(declaredType);
        this.annotations = annotations;
        this.value = value;
    }

    @NonNull
    @Override
    public Class<?> getClassOfDeclaredType() {
        return classOfDeclaredType;
    }

    @NonNull
    @Override
    public Annotation[] getAnnotations() {
        return annotations;
    }

    @Nullable
    @Override
    public Object getValue(@Nullable Object owner) {
        return value;
    }

    @Override
    public String toString() {
        return "parameter " + super.toString();
    }
}
