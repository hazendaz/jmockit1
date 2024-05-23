/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations;

import static mockit.internal.util.Utilities.containsReference;

import java.util.LinkedList;
import java.util.List;

import mockit.internal.util.GeneratedClasses;

import org.checkerframework.checker.nullness.qual.NonNull;

final class InstanceBasedMatching {
    @NonNull
    private final List<Class<?>> mockedTypesToMatchOnInstances;

    InstanceBasedMatching() {
        mockedTypesToMatchOnInstances = new LinkedList<>();
    }

    void discoverMockedTypesToMatchOnInstances(@NonNull List<Class<?>> targetClasses) {
        int numClasses = targetClasses.size();

        if (numClasses > 1) {
            for (int i = 0; i < numClasses; i++) {
                Class<?> targetClass = targetClasses.get(i);

                if (targetClasses.lastIndexOf(targetClass) > i) {
                    addMockedTypeToMatchOnInstance(targetClass);
                }
            }
        }
    }

    private void addMockedTypeToMatchOnInstance(@NonNull Class<?> mockedType) {
        if (!containsReference(mockedTypesToMatchOnInstances, mockedType)) {
            mockedTypesToMatchOnInstances.add(mockedType);
        }
    }

    boolean isToBeMatchedOnInstance(@NonNull Object mock) {
        Class<?> mockedClass = GeneratedClasses.getMockedClass(mock);
        return containsReference(mockedTypesToMatchOnInstances, mockedClass);
    }
}
