/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.faking;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.lang.reflect.Method;

import mockit.internal.ClassLoadingBridge;
import mockit.internal.state.TestRun;

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
