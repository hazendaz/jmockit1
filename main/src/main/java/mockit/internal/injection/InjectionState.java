/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.injection;

import static mockit.internal.injection.InjectionPoint.getQualifiedName;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import mockit.internal.reflection.FieldReflection;
import mockit.internal.reflection.GenericTypeReflection;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Holds state used throughout the injection process while it's in progress for a given set of tested objects.
 */
public final class InjectionState {
    @NonNull
    private static final Map<InjectionPoint, Object> globalObjects = new ConcurrentHashMap<>(2);

    @NonNull
    private final Map<InjectionPoint, Object> testedObjects;
    @NonNull
    private final Map<InjectionPoint, Object> instantiatedDependencies;
    @NonNull
    public final InjectionProviders injectionProviders;
    @NonNull
    public final LifecycleMethods lifecycleMethods;
    @NonNull
    final InterfaceResolution interfaceResolution;
    @Nullable
    private BeanExporter beanExporter;
    private Object currentTestClassInstance;

    InjectionState() {
        testedObjects = new LinkedHashMap<>();
        instantiatedDependencies = new LinkedHashMap<>();
        lifecycleMethods = new LifecycleMethods();
        injectionProviders = new InjectionProviders(lifecycleMethods);
        interfaceResolution = new InterfaceResolution();
    }

    void setInjectables(@NonNull Object testClassInstance, @NonNull List<? extends InjectionProvider> injectables) {
        currentTestClassInstance = testClassInstance;
        injectionProviders.setInjectables(injectables);
        lifecycleMethods.getServletConfigForInitMethodsIfAny(injectables, testClassInstance);
    }

    void addInjectables(@NonNull Object testClassInstance,
            @NonNull List<? extends InjectionProvider> injectablesToAdd) {
        currentTestClassInstance = testClassInstance;
        List<InjectionProvider> injectables = injectionProviders.addInjectables(injectablesToAdd);
        lifecycleMethods.getServletConfigForInitMethodsIfAny(injectables, testClassInstance);
    }

    Object getCurrentTestClassInstance() {
        return currentTestClassInstance;
    }

    @Nullable
    public Object getValueToInject(@NonNull InjectionProvider injectionProvider) {
        return injectionProviders.getValueToInject(injectionProvider, currentTestClassInstance);
    }

    void saveTestedObject(@NonNull InjectionPoint key, @NonNull Object testedObject, boolean global) {
        Map<InjectionPoint, Object> objects = global ? globalObjects : testedObjects;
        objects.put(key, testedObject);
    }

    @Nullable
    Object getTestedInstance(@NonNull InjectionPoint injectionPoint, boolean global) {
        Object testedInstance = instantiatedDependencies.isEmpty() ? null
                : findPreviouslyInstantiatedDependency(injectionPoint);

        if (testedInstance == null) {
            testedInstance = testedObjects.isEmpty() ? null : getValueFromExistingTestedObject(injectionPoint);
        }

        if (testedInstance == null && global) {
            testedInstance = globalObjects.get(injectionPoint);
        }

        return testedInstance;
    }

    @Nullable
    private Object findPreviouslyInstantiatedDependency(@NonNull InjectionPoint injectionPoint) {
        Object dependency = instantiatedDependencies.get(injectionPoint);

        if (dependency == null) {
            InjectionPoint injectionPointWithTypeOnly = new InjectionPoint(injectionPoint.type);
            dependency = instantiatedDependencies.get(injectionPointWithTypeOnly);

            if (dependency == null) {
                dependency = findMatchingObject(instantiatedDependencies, null, injectionPointWithTypeOnly);
            }
        }

        return dependency;
    }

    @Nullable
    private Object getValueFromExistingTestedObject(@NonNull InjectionPoint injectionPoint) {
        for (Object testedObject : testedObjects.values()) {
            Object fieldValue = getValueFromFieldOfEquivalentTypeAndName(injectionPoint, testedObject);

            if (fieldValue != null) {
                return fieldValue;
            }
        }

        return null;
    }

    @Nullable
    private static Object getValueFromFieldOfEquivalentTypeAndName(@NonNull InjectionPoint injectionPoint,
            @NonNull Object testedObject) {
        for (Field internalField : testedObject.getClass().getDeclaredFields()) {
            Type fieldType = internalField.getGenericType();
            String qualifiedName = getQualifiedName(internalField.getDeclaredAnnotations());
            boolean qualified = qualifiedName != null;
            String fieldName = qualified ? qualifiedName : internalField.getName();
            InjectionPoint internalInjectionPoint = new InjectionPoint(fieldType, fieldName, qualified);

            if (internalInjectionPoint.equals(injectionPoint)) {
                return FieldReflection.getFieldValue(internalField, testedObject);
            }
        }

        return null;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <D> D getGlobalDependency(@NonNull InjectionPoint key) {
        return (D) globalObjects.get(key);
    }

    @Nullable
    public Object getTestedValue(@NonNull TestedClass testedClass, @NonNull InjectionPoint injectionPoint) {
        Object testedValue = testedObjects.get(injectionPoint);

        if (testedValue == null) {
            testedValue = findMatchingObject(testedObjects, testedClass, injectionPoint);
        }

        return testedValue;
    }

    @Nullable
    public Object getInstantiatedDependency(@Nullable TestedClass testedClass, @NonNull InjectionPoint dependencyKey) {
        Object dependency = testedObjects.get(dependencyKey);

        if (dependency == null) {
            dependency = findMatchingObject(testedObjects, testedClass, dependencyKey);

            if (dependency == null) {
                dependency = instantiatedDependencies.get(dependencyKey);

                if (dependency == null) {
                    dependency = findMatchingObject(instantiatedDependencies, testedClass, dependencyKey);

                    if (dependency == null) {
                        dependency = findMatchingObject(globalObjects, testedClass, dependencyKey);
                    }
                }
            }
        }

        return dependency;
    }

    @Nullable
    private static Object findMatchingObject(@NonNull Map<InjectionPoint, Object> availableObjects,
            @Nullable TestedClass testedClass, @NonNull InjectionPoint injectionPoint) {
        if (availableObjects.isEmpty()) {
            return null;
        }

        GenericTypeReflection reflection = testedClass == null ? null : testedClass.reflection;
        Type dependencyType = injectionPoint.type;
        Object found = null;

        for (Entry<InjectionPoint, Object> injectionPointAndObject : availableObjects.entrySet()) {
            InjectionPoint dependencyIP = injectionPointAndObject.getKey();
            Object dependencyObject = injectionPointAndObject.getValue();

            if (injectionPoint.equals(dependencyIP)) {
                return dependencyObject;
            }

            if (reflection != null) {
                if (!reflection.areMatchingTypes(dependencyType, dependencyIP.type)) {
                    continue;
                }
                found = dependencyObject;
            }

            if (injectionPoint.hasSameName(dependencyIP)) {
                return dependencyObject;
            }
        }

        return injectionPoint.qualified ? null : found;
    }

    public void saveInstantiatedDependency(@NonNull InjectionPoint dependencyKey, @NonNull Object dependency) {
        instantiatedDependencies.put(dependencyKey, dependency);
    }

    public static void saveGlobalDependency(@NonNull InjectionPoint dependencyKey, @NonNull Object dependency) {
        globalObjects.put(dependencyKey, dependency);
    }

    void clearTestedObjectsAndInstantiatedDependencies() {
        testedObjects.clear();
        instantiatedDependencies.clear();
    }

    @NonNull
    BeanExporter getBeanExporter() {
        if (beanExporter == null) {
            beanExporter = new BeanExporter(this);
        }

        return beanExporter;
    }

    @Nullable
    public Class<?> resolveInterface(@NonNull Class<?> anInterface) {
        return interfaceResolution.resolveInterface(anInterface, currentTestClassInstance);
    }
}
