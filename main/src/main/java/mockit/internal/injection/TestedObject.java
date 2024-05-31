/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.injection;

import static mockit.internal.util.AutoBoxing.isWrapperOfPrimitiveType;
import static mockit.internal.util.DefaultValues.defaultValueForPrimitiveType;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;

import mockit.Tested;
import mockit.internal.injection.field.FieldInjection;
import mockit.internal.injection.full.FullInjection;

abstract class TestedObject {
    @NonNull
    private final InjectionState injectionState;
    @NonNull
    final Tested metadata;
    @NonNull
    private final String testedName;
    @Nullable
    private final FullInjection fullInjection;
    @NonNull
    private final TestedClass testedClass;
    @Nullable
    private final TestedObjectCreation testedObjectCreation;
    @Nullable
    private List<Field> targetFields;
    boolean createAutomatically;

    @Nullable
    static Tested getTestedAnnotationIfPresent(@NonNull Annotation annotation) {
        if (annotation instanceof Tested) {
            return (Tested) annotation;
        }

        return annotation.annotationType().getAnnotation(Tested.class);
    }

    TestedObject(@NonNull InjectionState injectionState, @NonNull Tested metadata, @NonNull Class<?> testClass,
            @NonNull String testedName, @NonNull Type testedType, @NonNull Class<?> testedClass) {
        this.injectionState = injectionState;
        this.metadata = metadata;
        this.testedName = testedName;
        fullInjection = metadata.fullyInitialized() ? new FullInjection(injectionState, testedClass, testedName) : null;

        if (testedClass.isInterface() || testedClass.isEnum() || testedClass.isPrimitive() || testedClass.isArray()) {
            testedObjectCreation = null;
            this.testedClass = new TestedClass(testedType, testedClass);
        } else {
            testedObjectCreation = new TestedObjectCreation(injectionState, fullInjection, testedType, testedClass);
            this.testedClass = testedObjectCreation.testedClass;
            injectionState.lifecycleMethods.findLifecycleMethods(testedClass);
        }

        this.testedClass.testClass = testClass;
    }

    boolean isAvailableDuringSetup() {
        return metadata.availableDuringSetup();
    }

    void instantiateWithInjectableValues(@NonNull Object testClassInstance) {
        if (alreadyInstantiated(testClassInstance)) {
            return;
        }

        Object testedObject = getExistingTestedInstanceIfApplicable(testClassInstance);
        Class<?> testedObjectClass = testedClass.targetClass;
        InjectionPoint injectionPoint = new InjectionPoint(testedClass.declaredType, testedName);

        if (isNonInstantiableType(testedObjectClass, testedObject)) {
            reusePreviouslyCreatedInstance(testClassInstance, injectionPoint);
            return;
        }

        if (testedObject == null && createAutomatically) {
            if (reusePreviouslyCreatedInstance(testClassInstance, injectionPoint)) {
                return;
            }

            testedObject = createAndRegisterNewObject(testClassInstance, injectionPoint);
        } else if (testedObject != null) {
            registerTestedObject(injectionPoint, testedObject);
            testedObjectClass = testedObject.getClass();
        }

        if (testedObject != null && testedObjectClass.getClassLoader() != null) {
            performFieldInjection(testedObjectClass, testedObject);

            if (createAutomatically) {
                injectionState.lifecycleMethods.executeInitializationMethodsIfAny(testedObjectClass, testedObject);
            }
        }
    }

    boolean alreadyInstantiated(@NonNull Object testClassInstance) {
        return false;
    }

    @Nullable
    abstract Object getExistingTestedInstanceIfApplicable(@NonNull Object testClassInstance);

    static boolean isNonInstantiableType(@NonNull Class<?> targetClass, @Nullable Object currentValue) {
        return targetClass.isPrimitive() && defaultValueForPrimitiveType(targetClass).equals(currentValue)
                || currentValue == null && (targetClass.isArray() || targetClass.isEnum() || targetClass.isAnnotation()
                        || isWrapperOfPrimitiveType(targetClass));
    }

    private boolean reusePreviouslyCreatedInstance(@NonNull Object testClassInstance,
            @NonNull InjectionPoint injectionPoint) {
        Object previousInstance = injectionState.getTestedInstance(injectionPoint, metadata.global());

        if (previousInstance != null) {
            setInstance(testClassInstance, previousInstance);
            return true;
        }

        return false;
    }

    abstract void setInstance(@NonNull Object testClassInstance, @Nullable Object testedInstance);

    @Nullable
    private Object createAndRegisterNewObject(@NonNull Object testClassInstance,
            @NonNull InjectionPoint injectionPoint) {
        Object testedInstance = null;

        if (testedObjectCreation != null) {
            testedInstance = testedObjectCreation.create(false, true);

            if (testedInstance != null) {
                setInstance(testClassInstance, testedInstance);
                registerTestedObject(injectionPoint, testedInstance);
            }
        }

        return testedInstance;
    }

    private void registerTestedObject(@NonNull InjectionPoint injectionPoint, @NonNull Object testedObject) {
        injectionState.saveTestedObject(injectionPoint, testedObject, metadata.global());
    }

    private void performFieldInjection(@NonNull Class<?> targetClass, @NonNull Object testedObject) {
        FieldInjection fieldInjection = new FieldInjection(injectionState, fullInjection);

        if (targetFields == null) {
            targetFields = Injector.findAllTargetInstanceFieldsInTestedClassHierarchy(targetClass, testedClass);
        }

        fieldInjection.injectIntoEligibleFields(targetFields, testedObject, testedClass);
    }

    void clearIfAutomaticCreation(@NonNull Object testClassInstance, boolean duringTearDown) {
        if (createAutomatically && (duringTearDown || !isAvailableDuringSetup())) {
            setInstance(testClassInstance, null);

            if (fullInjection != null) {
                fullInjection.clear();
            }
        }
    }
}
