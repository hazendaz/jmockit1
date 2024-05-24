/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.mocking;

import static mockit.internal.util.AutoBoxing.isWrapperOfPrimitiveType;
import static mockit.internal.util.GeneratedClasses.getMockedClass;
import static mockit.internal.util.GeneratedClasses.isGeneratedImplementationClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mockit.internal.expectations.MockingFilters;
import mockit.internal.state.TestRun;

import edu.umd.cs.findbugs.annotations.NonNull;

public final class PartialMocking extends BaseTypeRedefinition {
    @NonNull
    public final List<Object> targetInstances;
    @NonNull
    private final Map<Class<?>, byte[]> modifiedClassfiles;

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

    private void redefineClassHierarchy(@NonNull Object mockInstance) {
        if (mockInstance instanceof Class) {
            throw new IllegalArgumentException(
                    "Invalid Class argument for partial mocking (use a MockUp instead): " + mockInstance);
        }

        targetClass = getMockedClass(mockInstance);
        applyPartialMockingToGivenInstance(mockInstance);

        InstanceFactory instanceFactory = createInstanceFactory(targetClass);
        instanceFactory.lastInstance = mockInstance;

        TestRun.mockFixture().registerInstanceFactoryForMockedType(targetClass, instanceFactory);
        TestRun.getExecutingTest().getCascadingTypes().add(false, targetClass);
    }

    private void applyPartialMockingToGivenInstance(@NonNull Object instance) {
        validateTargetClassType();
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
        modifier.useDynamicMocking();
    }

    @Override
    void applyClassRedefinition(@NonNull Class<?> realClass, @NonNull byte[] modifiedClass) {
        modifiedClassfiles.put(realClass, modifiedClass);
    }
}
