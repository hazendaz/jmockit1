/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.injection.field;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import mockit.internal.injection.InjectionPoint;
import mockit.internal.injection.InjectionPoint.KindOfInjectionPoint;
import mockit.internal.injection.InjectionProvider;

public final class FieldToInject extends InjectionProvider {
    @NonNull
    private final Field targetField;
    @NonNull
    private final KindOfInjectionPoint kindOfInjectionPoint;

    public FieldToInject(@NonNull Field targetField) {
        super(targetField.getGenericType(), targetField.getName());
        this.targetField = targetField;
        kindOfInjectionPoint = InjectionPoint.kindOfInjectionPoint(targetField);
    }

    @NonNull
    @Override
    public Class<?> getClassOfDeclaredType() {
        return targetField.getType();
    }

    @NonNull
    @Override
    public Annotation[] getAnnotations() {
        return targetField.getDeclaredAnnotations();
    }

    @Override
    public boolean isRequired() {
        return kindOfInjectionPoint == KindOfInjectionPoint.Required;
    }

    @Override
    public String toString() {
        return "field " + super.toString();
    }
}
