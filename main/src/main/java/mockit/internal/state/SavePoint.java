/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.state;

import static mockit.internal.expectations.RecordAndReplayExecution.RECORD_OR_REPLAY_LOCK;

import java.util.List;
import java.util.Map;
import java.util.Set;

import mockit.internal.ClassIdentification;
import mockit.internal.faking.FakeClasses;

import org.checkerframework.checker.nullness.qual.NonNull;

public final class SavePoint {
    @NonNull
    private final Set<ClassIdentification> previousTransformedClasses;
    @NonNull
    private final Map<Class<?>, byte[]> previousRedefinedClasses;
    private final int previousCaptureTransformerCount;
    @NonNull
    private final List<Class<?>> previousMockedClasses;
    @NonNull
    private final FakeClasses.SavePoint previousFakeClasses;

    public SavePoint() {
        MockFixture mockFixture = TestRun.mockFixture();
        previousTransformedClasses = mockFixture.getTransformedClasses();
        previousRedefinedClasses = mockFixture.getRedefinedClasses();
        previousCaptureTransformerCount = mockFixture.getCaptureTransformerCount();
        previousMockedClasses = mockFixture.getMockedClasses();
        previousFakeClasses = TestRun.getFakeClasses().new SavePoint();
    }

    public synchronized void rollback() {
        RECORD_OR_REPLAY_LOCK.lock();

        try {
            MockFixture mockFixture = TestRun.mockFixture();
            mockFixture.removeCaptureTransformers(previousCaptureTransformerCount);
            mockFixture.restoreTransformedClasses(previousTransformedClasses);
            mockFixture.restoreRedefinedClasses(previousRedefinedClasses);
            mockFixture.removeMockedClasses(previousMockedClasses);
            previousFakeClasses.rollback();
        } finally {
            RECORD_OR_REPLAY_LOCK.unlock();
        }
    }
}
