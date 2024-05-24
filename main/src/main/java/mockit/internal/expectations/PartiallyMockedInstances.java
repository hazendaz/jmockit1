/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations;

import static mockit.internal.util.Utilities.containsReference;

import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;

final class PartiallyMockedInstances {
    @NonNull
    private final List<?> dynamicMockInstancesToMatch;

    PartiallyMockedInstances(@NonNull List<?> dynamicMockInstancesToMatch) {
        this.dynamicMockInstancesToMatch = dynamicMockInstancesToMatch;
    }

    boolean isToBeMatchedOnInstance(@NonNull Object mock) {
        return containsReference(dynamicMockInstancesToMatch, mock);
    }

    boolean isDynamicMockInstanceOrClass(@NonNull Object invokedInstance, @NonNull Object invocationInstance) {
        if (containsReference(dynamicMockInstancesToMatch, invokedInstance)) {
            return true;
        }

        Class<?> invokedClass = invocationInstance.getClass();

        for (Object dynamicMock : dynamicMockInstancesToMatch) {
            if (dynamicMock.getClass() == invokedClass) {
                return true;
            }
        }

        return false;
    }
}
