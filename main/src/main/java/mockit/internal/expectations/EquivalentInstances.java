/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.expectations;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;

import mockit.internal.expectations.invocation.ExpectedInvocation;

final class EquivalentInstances {
    @NonNull
    final Map<Object, Object> instanceMap;
    @NonNull
    final Map<Object, Object> replacementMap;

    EquivalentInstances() {
        instanceMap = new IdentityHashMap<>();
        replacementMap = new IdentityHashMap<>();
    }

    void registerReplacementInstanceIfApplicable(@Nullable Object mock, @NonNull ExpectedInvocation invocation) {
        Object replacementInstance = invocation.replacementInstance;

        if (replacementInstance != null && replacementInstance != invocation.instance) {
            replacementMap.put(mock, replacementInstance);
        }
    }

    boolean isEquivalentInstance(@NonNull Object invocationInstance, @NonNull Object invokedInstance) {
        return invocationInstance == invokedInstance || invocationInstance == replacementMap.get(invokedInstance)
                || invocationInstance == instanceMap.get(invokedInstance)
                || invokedInstance == instanceMap.get(invocationInstance);
    }

    boolean areNonEquivalentInstances(@NonNull Object invocationInstance, @NonNull Object invokedInstance) {
        boolean recordedInstanceMatchingAnyInstance = !isMatchingInstance(invocationInstance);
        boolean invokedInstanceMatchingSpecificInstance = isMatchingInstance(invokedInstance);
        return recordedInstanceMatchingAnyInstance && invokedInstanceMatchingSpecificInstance;
    }

    private boolean isMatchingInstance(@NonNull Object instance) {
        return instanceMap.containsKey(instance) || instanceMap.containsValue(instance)
                || replacementMap.containsKey(instance) || replacementMap.containsValue(instance);
    }

    boolean areMatchingInstances(boolean matchInstance, @NonNull Object mock1, @NonNull Object mock2) {
        if (matchInstance) {
            return isEquivalentInstance(mock1, mock2);
        }

        return !areInDifferentEquivalenceSets(mock1, mock2);
    }

    private boolean areInDifferentEquivalenceSets(@NonNull Object mock1, @NonNull Object mock2) {
        if (mock1 == mock2 || instanceMap.isEmpty()) {
            return false;
        }

        Object mock1Equivalent = instanceMap.get(mock1);
        Object mock2Equivalent = instanceMap.get(mock2);

        if (mock1Equivalent == mock2 || mock2Equivalent == mock1) {
            return false;
        }

        // noinspection SimplifiableIfStatement
        if (mock1Equivalent != null && mock2Equivalent != null) {
            return true;
        }

        return instanceMapHasMocksInSeparateEntries(mock1, mock2);
    }

    private boolean instanceMapHasMocksInSeparateEntries(@NonNull Object mock1, @NonNull Object mock2) {
        boolean found1 = false;
        boolean found2 = false;

        for (Entry<Object, Object> entry : instanceMap.entrySet()) {
            if (!found1 && isInMapEntry(entry, mock1)) {
                found1 = true;
            }

            if (!found2 && isInMapEntry(entry, mock2)) {
                found2 = true;
            }

            if (found1 && found2) {
                return true;
            }
        }

        return false;
    }

    private static boolean isInMapEntry(@NonNull Entry<Object, Object> mapEntry, @NonNull Object mock) {
        return mapEntry.getKey() == mock || mapEntry.getValue() == mock;
    }

    @Nullable
    Object getReplacementInstanceForMethodInvocation(@NonNull Object invokedInstance,
            @NonNull String methodNameAndDesc) {
        return methodNameAndDesc.charAt(0) == '<' ? null : replacementMap.get(invokedInstance);
    }

    boolean isReplacementInstance(@NonNull Object invokedInstance, @NonNull String methodNameAndDesc) {
        return methodNameAndDesc.charAt(0) != '<'
                && (replacementMap.containsKey(invokedInstance) || replacementMap.containsValue(invokedInstance));
    }
}
