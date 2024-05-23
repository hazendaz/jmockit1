/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.mocking;

import static mockit.internal.expectations.RecordAndReplayExecution.RECORD_OR_REPLAY_LOCK;
import static mockit.internal.expectations.RecordAndReplayExecution.recordOrReplay;

import java.lang.reflect.Method;

import mockit.internal.ClassLoadingBridge;
import mockit.internal.expectations.ExecutionMode;
import mockit.internal.state.TestRun;
import mockit.internal.util.ObjectMethods;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public final class MockedBridge extends ClassLoadingBridge {
    @NonNull
    public static final ClassLoadingBridge MB = new MockedBridge();

    private MockedBridge() {
        super("$MB");
    }

    @Nullable
    @Override
    public Object invoke(@Nullable Object mocked, Method method, @NonNull Object[] args) throws Throwable {
        String mockedClassDesc = (String) args[1];

        if (notToBeMocked(mocked, mockedClassDesc)) {
            return Void.class;
        }

        String mockName = (String) args[2];
        String mockDesc = (String) args[3];
        String mockNameAndDesc = mockName + mockDesc;
        Integer executionMode = (Integer) args[5];
        Object[] mockArgs = extractArguments(6, args);

        boolean regularExecutionWithRecordReplayLock = executionMode == ExecutionMode.Regular.ordinal()
                && RECORD_OR_REPLAY_LOCK.isHeldByCurrentThread();
        Object rv;

        if (regularExecutionWithRecordReplayLock && mocked != null) {
            rv = ObjectMethods.evaluateOverride(mocked, mockNameAndDesc, args);

            if (rv != null) {
                return rv;
            }
        }

        if (TestRun.getExecutingTest().isProceedingIntoRealImplementation() || regularExecutionWithRecordReplayLock
                || TestRun.isInsideNoMockingZone()) {
            return Void.class;
        }

        TestRun.enterNoMockingZone();

        try {
            int mockAccess = (Integer) args[0];
            String genericSignature = (String) args[4];
            rv = recordOrReplay(mocked, mockAccess, mockedClassDesc, mockNameAndDesc, genericSignature, executionMode,
                    mockArgs);
        } finally {
            TestRun.exitNoMockingZone();
        }

        return rv;
    }
}
