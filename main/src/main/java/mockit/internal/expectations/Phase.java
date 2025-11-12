/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.expectations;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.Map;

abstract class Phase {
    @NonNull
    final PhasedExecutionState executionState;

    Phase(@NonNull PhasedExecutionState executionState) {
        this.executionState = executionState;
    }

    @NonNull
    public final Map<Object, Object> getInstanceMap() {
        return executionState.equivalentInstances.instanceMap;
    }

    @NonNull
    final Map<Object, Object> getReplacementMap() {
        return executionState.equivalentInstances.replacementMap;
    }

    @Nullable
    abstract Object handleInvocation(@Nullable Object mock, int mockAccess, @NonNull String mockClassDesc,
            @NonNull String mockNameAndDesc, @Nullable String genericSignature, boolean withRealImpl,
            @NonNull Object[] args) throws Throwable;
}
