/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.expectations;

import static mockit.internal.util.Utilities.containsReference;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.LinkedList;
import java.util.List;

import mockit.internal.util.GeneratedClasses;

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
