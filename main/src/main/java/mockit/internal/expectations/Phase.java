/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
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
