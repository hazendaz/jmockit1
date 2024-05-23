/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.injection;

import static mockit.internal.injection.TestedObject.getTestedAnnotationIfPresent;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import mockit.Injectable;
import mockit.Tested;
import mockit.asm.jvmConstants.Access;
import mockit.internal.expectations.mocking.MockedType;

import org.checkerframework.checker.nullness.qual.NonNull;

public final class TestedClassInstantiations {
    private static final int FIELD_ACCESS_MASK = Access.SYNTHETIC + Access.STATIC;
    private static final int METHOD_ACCESS_MASK = Access.BRIDGE + Access.VARARGS + Access.NATIVE + Access.ABSTRACT
            + Access.SYNTHETIC;

    @NonNull
    private final List<TestedField> testedFields;
    @NonNull
    private final List<InjectionProvider> injectableFields;
    @NonNull
    final InjectionState injectionState;

    public TestedClassInstantiations() {
        testedFields = new LinkedList<>();
        injectableFields = new ArrayList<>();
        injectionState = new InjectionState();
    }

    public boolean findTestedAndInjectableMembers(@NonNull Class<?> testClass) {
        findAllTestedAndInjectableMembersInTestClassHierarchy(testClass);

        return injectionState.injectionProviders.setInjectables(injectableFields) || !testedFields.isEmpty()
                || injectionState.interfaceResolution.canResolveInterfaces();
    }

    private void findAllTestedAndInjectableMembersInTestClassHierarchy(@NonNull Class<?> testClass) {
        Class<?> superclass = testClass.getSuperclass();

        if (superclass.getClassLoader() != null) {
            findAllTestedAndInjectableMembersInTestClassHierarchy(superclass);
        }

        examineInstanceFields(testClass);
        examineMethods(testClass);
    }

    private void examineInstanceFields(@NonNull Class<?> testClass) {
        for (Field candidateField : testClass.getDeclaredFields()) {
            if ((candidateField.getModifiers() & FIELD_ACCESS_MASK) == 0) {
                addAsTestedOrInjectableFieldIfApplicable(candidateField);
            }
        }
    }

    private void examineMethods(@NonNull Class<?> testClass) {
        for (Method candidateMethod : testClass.getDeclaredMethods()) {
            if ((candidateMethod.getModifiers() & METHOD_ACCESS_MASK) == 0) {
                addAsTestedMethodIfApplicable(candidateMethod);
            }
        }
    }

    private void addAsTestedOrInjectableFieldIfApplicable(@NonNull Field fieldFromTestClass) {
        for (Annotation fieldAnnotation : fieldFromTestClass.getDeclaredAnnotations()) {
            if (fieldAnnotation instanceof Injectable) {
                InjectionProvider mockedType = new MockedType(fieldFromTestClass);
                injectableFields.add(mockedType);
                break;
            }

            Tested testedMetadata = getTestedAnnotationIfPresent(fieldAnnotation);

            if (testedMetadata != null) {
                TestedField testedField = new TestedField(injectionState, fieldFromTestClass, testedMetadata);
                testedFields.add(testedField);
                break;
            }
        }
    }

    private void addAsTestedMethodIfApplicable(@NonNull Method methodFromTestClass) {
        for (Annotation methodAnnotation : methodFromTestClass.getDeclaredAnnotations()) {
            Tested testedMetadata = getTestedAnnotationIfPresent(methodAnnotation);

            if (testedMetadata != null) {
                addTestedMethodIfApplicable(methodFromTestClass);
                break;
            }
        }
    }

    private void addTestedMethodIfApplicable(@NonNull Method methodFromTestClass) {
        Class<?> returnType = methodFromTestClass.getReturnType();

        if (returnType == Class.class) {
            Type[] parameterTypes = methodFromTestClass.getGenericParameterTypes();

            if (parameterTypes.length == 1) {
                Type parameterType = parameterTypes[0];

                if (parameterType instanceof ParameterizedType) {
                    ParameterizedType interfaceType = (ParameterizedType) parameterType;

                    if (interfaceType.getRawType() == Class.class) {
                        injectionState.interfaceResolution.addInterfaceResolutionMethod(interfaceType,
                                methodFromTestClass);
                    }
                }
            }
        }
    }

    public void assignNewInstancesToTestedFieldsFromBaseClasses(@NonNull Object testClassInstance) {
        injectionState.setInjectables(testClassInstance, injectableFields);

        Class<?> testClass = testClassInstance.getClass();

        for (TestedField testedField : testedFields) {
            if (testedField.isFromBaseClass(testClass)) {
                instantiateTestedObject(testClassInstance, testedField);
            }
        }
    }

    public void assignNewInstancesToTestedFields(@NonNull Object testClassInstance, boolean beforeSetup,
            @NonNull List<? extends InjectionProvider> injectableParameters) {
        List<InjectionProvider> injectables = injectableFields;

        if (!injectableParameters.isEmpty()) {
            injectables = new ArrayList<>(injectables);
            injectables.addAll(injectableParameters);
        }

        injectionState.setInjectables(testClassInstance, injectables);

        for (TestedField testedField : testedFields) {
            if (!beforeSetup || testedField.isAvailableDuringSetup()) {
                instantiateTestedObject(testClassInstance, testedField);
            }
        }
    }

    private void instantiateTestedObject(@NonNull Object testClassInstance, @NonNull TestedObject testedObject) {
        try {
            testedObject.instantiateWithInjectableValues(testClassInstance);
        } finally {
            injectionState.injectionProviders.resetConsumedInjectionProviders();
        }
    }

    public void clearTestedObjects() {
        injectionState.lifecycleMethods.executeTerminationMethodsIfAny();
        injectionState.clearTestedObjectsAndInstantiatedDependencies();
        resetTestedFields(false);
    }

    private void resetTestedFields(boolean duringTearDown) {
        Object testClassInstance = injectionState.getCurrentTestClassInstance();

        if (testClassInstance != null) {
            for (TestedObject testedField : testedFields) {
                testedField.clearIfAutomaticCreation(testClassInstance, duringTearDown);
            }
        }
    }

    public void clearTestedObjectsCreatedDuringSetup() {
        resetTestedFields(true);
    }

    @NonNull
    public BeanExporter getBeanExporter() {
        return injectionState.getBeanExporter();
    }
}
