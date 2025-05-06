/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.mocking;

import static mockit.internal.util.AutoBoxing.isWrapperOfPrimitiveType;
import static mockit.internal.util.GeneratedClasses.getMockedClass;
import static mockit.internal.util.GeneratedClasses.isGeneratedImplementationClass;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mockit.internal.expectations.MockingFilters;
import mockit.internal.state.TestRun;

public final class PartialMocking extends BaseTypeRedefinition {
    @NonNull
    public final List<Object> targetInstances;
    @NonNull
    private final Map<Class<?>, byte[]> modifiedClassfiles;
    private boolean methodsOnly;

    public PartialMocking() {
        targetInstances = new ArrayList<>(2);
        modifiedClassfiles = new HashMap<>();
    }

    public void redefineTypes(@NonNull Object[] instancesToBePartiallyMocked) {
        for (Object instance : instancesToBePartiallyMocked) {
            redefineClassHierarchy(instance);
        }

        if (!modifiedClassfiles.isEmpty()) {
            TestRun.mockFixture().redefineMethods(modifiedClassfiles);
            modifiedClassfiles.clear();
        }
    }

    private void redefineClassHierarchy(@NonNull Object classOrInstance) {
        Object mockInstance;

        if (classOrInstance instanceof Class) {
            mockInstance = null;
            targetClass = (Class<?>) classOrInstance;
            CaptureOfNewInstances capture = TestRun.mockFixture().findCaptureOfImplementations(targetClass);

            if (capture != null) {
                capture.useDynamicMocking(targetClass);
                return;
            }

            applyPartialMockingToGivenClass();
        } else {
            mockInstance = classOrInstance;
            targetClass = getMockedClass(mockInstance);
            applyPartialMockingToGivenInstance(mockInstance);
        }

        InstanceFactory instanceFactory = createInstanceFactory(targetClass);
        instanceFactory.lastInstance = mockInstance;

        TestRun.mockFixture().registerInstanceFactoryForMockedType(targetClass, instanceFactory);
        TestRun.getExecutingTest().getCascadingTypes().add(false, targetClass);
    }

    private void applyPartialMockingToGivenClass() {
        validateTargetClassType();
        TestRun.ensureThatClassIsInitialized(targetClass);
        methodsOnly = false;
        redefineMethodsAndConstructorsInTargetType();
    }

    private void applyPartialMockingToGivenInstance(@NonNull Object instance) {
        validateTargetClassType();
        methodsOnly = false;
        redefineMethodsAndConstructorsInTargetType();
        targetInstances.add(instance);
    }

    private void validateTargetClassType() {
        if (targetClass.isInterface() || targetClass.isAnnotation() || targetClass.isArray()
                || targetClass.isPrimitive() || targetClass.isSynthetic()
                || MockingFilters.isSubclassOfUnmockable(targetClass) || isWrapperOfPrimitiveType(targetClass)
                || isGeneratedImplementationClass(targetClass)) {
            throw new IllegalArgumentException("Invalid type for partial mocking: " + targetClass);
        }
    }

    @Override
    void configureClassModifier(@NonNull MockedClassModifier modifier) {
        modifier.useDynamicMocking(methodsOnly);
    }

    @Override
    void applyClassRedefinition(@NonNull Class<?> realClass, @NonNull byte[] modifiedClass) {
        modifiedClassfiles.put(realClass, modifiedClass);
    }
}
