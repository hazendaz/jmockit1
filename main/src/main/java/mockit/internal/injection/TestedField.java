/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.injection;

import static java.lang.reflect.Modifier.isFinal;

import static mockit.internal.reflection.FieldReflection.getFieldValue;
import static mockit.internal.reflection.FieldReflection.setFieldValue;

import java.lang.reflect.Field;

import mockit.Tested;
import mockit.internal.util.TypeConversion;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

final class TestedField extends TestedObject {
    @NonNull
    private final Field testedField;

    TestedField(@NonNull InjectionState injectionState, @NonNull Field field, @NonNull Tested metadata) {
        super(injectionState, metadata, field.getDeclaringClass(), field.getName(), field.getGenericType(),
                field.getType());
        testedField = field;
    }

    boolean isFromBaseClass(@NonNull Class<?> testClass) {
        return testedField.getDeclaringClass() != testClass;
    }

    @Override
    boolean alreadyInstantiated(@NonNull Object testClassInstance) {
        return isAvailableDuringSetup() && getFieldValue(testedField, testClassInstance) != null;
    }

    @Nullable
    @Override
    Object getExistingTestedInstanceIfApplicable(@NonNull Object testClassInstance) {
        Object testedObject = null;

        if (!createAutomatically) {
            Class<?> targetClass = testedField.getType();
            testedObject = getFieldValue(testedField, testClassInstance);

            if (testedObject == null || isNonInstantiableType(targetClass, testedObject)) {
                String providedValue = metadata.value();

                if (!providedValue.isEmpty()) {
                    testedObject = TypeConversion.convertFromString(targetClass, providedValue);
                }

                createAutomatically = testedObject == null && !isFinal(testedField.getModifiers());
            }
        }

        return testedObject;
    }

    @Override
    void setInstance(@NonNull Object testClassInstance, @Nullable Object testedInstance) {
        setFieldValue(testedField, testClassInstance, testedInstance);
    }
}
