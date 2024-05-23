/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.faking;

import java.lang.reflect.Method;

import javax.annotation.Nullable;

import mockit.internal.ClassLoadingBridge;
import mockit.internal.state.TestRun;

import org.checkerframework.checker.nullness.qual.NonNull;

public final class FakeBridge extends ClassLoadingBridge {
    @NonNull
    public static final ClassLoadingBridge MB = new FakeBridge();

    private FakeBridge() {
        super("$FB");
    }

    @NonNull
    @Override
    public Object invoke(@Nullable Object faked, Method method, @NonNull Object[] args) {
        if (TestRun.isInsideNoMockingZone()) {
            return false;
        }

        TestRun.enterNoMockingZone();

        try {
            String fakeClassDesc = (String) args[0];

            if (notToBeMocked(faked, fakeClassDesc)) {
                return false;
            }

            Integer fakeStateIndex = (Integer) args[1];
            return TestRun.updateFakeState(fakeClassDesc, faked, fakeStateIndex);
        } finally {
            TestRun.exitNoMockingZone();
        }
    }
}
